# Architecture & Design Notes — Employee Salary Management

**Version:** 1.0
**Companion to:** `docs/requirements.md`

---

## 1. Architectural Style: Modular Monolith (not microservices)

The stack options mentioned microservices "if needed." **We deliberately choose
a single, well-layered Spring Boot service (a modular monolith), not
microservices.**

**Reasoning (trade-off):**
- The domain is small and cohesive: employees, salaries, and analytics over a
  single dataset. There is no independent scaling boundary, no separate team
  ownership, and no differing deployment cadence that would justify splitting.
- Microservices would add network hops, distributed-transaction complexity,
  multiple deployments, and inter-service contracts — cost with no benefit at
  this scale. That would be *complexity for its own sake*, which the assessment
  explicitly warns against ("not the most complex system, but good judgment").
- A clean internal layering (controller → service → repository) keeps modules
  decoupled and would let us extract a service later *if* a real boundary
  emerged. We keep the seams; we don't pay the distribution tax now.

```
+-------------------------------------------------------------+
|                     Angular SPA (browser)                   |
|   Employee table | Employee detail/edit | Analytics dash    |
+---------------------------+---------------------------------+
                            | HTTPS / JSON (REST)
                            v
+-------------------------------------------------------------+
|                 Spring Boot backend (single service)        |
|                                                             |
|  Web layer      -> Controllers (REST, DTOs, validation)     |
|  Service layer  -> EmployeeService, AnalyticsService,       |
|                    CurrencyConverter (business logic)       |
|  Data layer     -> Spring Data JPA repositories             |
+---------------------------+---------------------------------+
                            | JDBC
                            v
                    +----------------+
                    |     SQLite     |  (single file DB)
                    +----------------+
```

## 2. Security: Lightweight, not enterprise SSO

One persona (HR Manager). We include **basic authentication protecting the API**
(a single HR login issuing a token / session), enough to demonstrate the API
isn't wide open, without building full RBAC/SSO.

**Reasoning:** The requirement is a single trusted internal user. A simple auth
gate satisfies "use security if needed" while avoiding the complexity of
multi-role permission systems that the persona doesn't need. Decision: implement
a minimal token-based guard on the API; keep it swappable.

> Note: if credit budget is tight, auth can be toggled to a simple static
> API-key/header check and still demonstrate the concept. Final choice recorded
> at implementation time in Step 6.

## 3. Data Model

Normalized reference tables for country / department / role keep the 10k
employee rows clean and make grouped analytics fast and consistent.

```
country (id, name, iso_code, currency_code)
department (id, name)
job_role (id, title)

employee (
  id            PK
  first_name
  last_name
  email         UNIQUE
  country_id    FK -> country
  department_id FK -> department
  job_role_id   FK -> job_role
  hire_date
  status        ENUM(ACTIVE, INACTIVE)
  base_salary   DECIMAL      -- amount in the employee's local currency
  currency_code               -- denormalized for convenience/integrity
)

fx_rate (currency_code PK, rate_to_base)  -- fixed, seeded; base = USD
```

**Key decisions:**
- **Current salary only** lives on `employee` (no salary history table) — matches
  the "current pay" scope. History is a documented future enhancement.
- **`fx_rate` is a seeded, fixed table** (base = USD). Analytics normalize every
  salary to USD for cross-country comparison. This keeps tests deterministic and
  avoids an external FX API dependency (see requirements §4).
- **Reference tables** (country/department/role) avoid free-text inconsistency
  and make `GROUP BY` analytics reliable and indexable.
- **Indexes** on `employee(country_id)`, `(department_id)`, `(job_role_id)`,
  `(last_name)`, and `status` to keep filtering/grouping fast at 10k rows.

## 4. API Contract (REST, JSON)

Base path: `/api`

### Employees
| Method | Path | Purpose |
|--------|------|---------|
| GET | `/employees` | Paged list. Query: `page`, `size`, `search`, `country`, `department`, `role`, `sort`. **Server-side pagination + filtering.** |
| GET | `/employees/{id}` | Single employee detail. |
| POST | `/employees` | Create employee. |
| PUT | `/employees/{id}` | Update employee (incl. salary). |
| PATCH | `/employees/{id}/status` | Activate/deactivate. |

### Analytics (the "how do we pay people" questions)
| Method | Path | Purpose |
|--------|------|---------|
| GET | `/analytics/summary` | Org headcount, total/avg/median salary (base currency). |
| GET | `/analytics/by-country` | Count, avg, median salary per country. |
| GET | `/analytics/by-department` | Same, grouped by department. |
| GET | `/analytics/by-role` | Same, grouped by role. |
| GET | `/analytics/distribution` | Employee count per salary band. |

All analytics accept optional filters (`country`, `department`, `role`) and
return amounts normalized to the base currency. Aggregations run **in the DB**,
never by loading 10k rows into memory.

### Reference data
| Method | Path | Purpose |
|--------|------|---------|
| GET | `/meta/countries` \| `/departments` \| `/roles` | Populate filter dropdowns. |

### Conventions
- DTOs at the web boundary (never expose JPA entities directly).
- Bean Validation (`@NotNull`, `@Email`, `@Positive`) on write requests.
- Consistent error shape: `{ timestamp, status, error, message, path }`.
- Median computed deterministically (documented algorithm; unit-tested).

## 5. Layering & Package Structure (backend)

```
com.acme.salary
├── config          # security, CORS, app config
├── employee        # controller, service, repository, entity, dto
├── analytics       # controller, service, dto, projections
├── reference       # country/department/role read endpoints
├── currency        # CurrencyConverter + fx rates
├── seed            # 10k data seeder (profile-guarded)
└── common          # error handling, pagination, shared dto
```

## 6. Frontend Structure (Angular)

```
src/app
├── core            # http services, auth interceptor, models
├── employees       # list (table + filters + pagination), detail/edit
├── analytics       # dashboard: summary cards + grouped tables/charts
└── shared          # UI components, formatting pipes (currency)
```
Component library: **Angular Material** (mature, accessible, good table +
paginator + form controls out of the box).

## 7. Performance Considerations (10k employees)

- Server-side pagination for the list (never ship 10k rows to the browser).
- DB-side aggregation for analytics (SQL `AVG`, `COUNT`, `GROUP BY`; median via
  a deterministic query/algorithm).
- Indexes on all filter/group columns.
- The seed uses a **fixed random seed** so the dataset — and therefore tests and
  demos — are reproducible.

## 8. Testing Strategy

- **Unit tests (fast, deterministic):** `CurrencyConverter` normalization,
  median/average logic, salary validation, and service-layer behavior with a
  repository test double.
- **Slice/integration (a few):** repository queries against an in-memory/SQLite
  test DB to verify pagination and grouped aggregation.
- No reliance on wall-clock time, network, or random without a fixed seed.

## 9. Key Trade-offs Summary

| Decision | Chosen | Rejected | Why |
|----------|--------|----------|-----|
| Service topology | Modular monolith | Microservices | No real boundaries at this scale; avoid distribution cost. |
| FX rates | Fixed seeded table | Live FX API | Determinism + testability; no external dependency. |
| Salary model | Current salary only | Full history/audit | Matches scope; history noted as future work. |
| Auth | Minimal token/API-key gate | Full RBAC/SSO | Single persona; right-sized security. |
| DB | SQLite | Postgres/MySQL | Zero-setup, file-based, sufficient for 10k rows + assessment. |
| UI kit | Angular Material | Custom CSS | Accessible, batteries-included table/forms; saves time. |
