# Requirements Document — Employee Salary Management

**Version:** 1.0
**Author:** Engineering
**Status:** Draft for build
**Persona:** HR Manager at ACME (a 10,000-employee, multi-country organization)

---

## 1. Goal

Replace ACME HR's error-prone, Excel-based salary tracking with a web-based
system that lets an HR Manager **manage salary data for ~10,000 employees** and
**answer questions about how the organization pays people** (by country,
department, role, and pay band) quickly and reliably.

## 2. Problem Context

Today salary data lives in spreadsheets spread across countries. This is tedious
to maintain, hard to keep consistent, error-prone, and impossible to query at
scale. The HR Manager needs a single source of truth with fast search and
built-in analytics.

## 3. Scope & Features (In Scope)

### 3.1 Employee & Salary Management
- List all employees with **server-side pagination, search, and filtering**
  (by name, country, department, role) — must stay responsive at 10,000 rows.
- View a single employee's detail, including their current salary.
- Create, update, and deactivate an employee record.
- Update an employee's salary (amount + currency), with basic validation.

### 3.2 Compensation Analytics (the "how do we pay people" questions)
- Organization-wide summary: headcount, total/average/median salary.
- Average and median pay **grouped by country, department, and role**.
- Salary distribution / pay bands (count of employees per band).
- Filterable analytics (e.g., average pay for Engineering in Germany).
- All monetary aggregates normalized to a **single base currency** for
  comparability across countries.

### 3.3 Data
- SQLite relational database with a clean, normalized schema.
- Seed script that generates **10,000 realistic employees** spread across
  multiple countries, departments, and roles.

### 3.4 Quality & Delivery
- Backend: Java + Spring Boot (REST API).
- Frontend: Angular with a component library.
- Meaningful, fast, deterministic unit tests for core logic.
- Incremental Git commits documenting the evolution.
- Deployable build + demo.

## 4. Out of Scope (Deliberately Excluded) — with Reasoning

| Excluded | Reasoning |
|----------|-----------|
| **Full payroll processing** (tax, deductions, payslip generation, bank disbursement) | This is a *salary data management + analytics* tool, not a payroll engine. Payroll is a large regulated domain that would dwarf the assessment and add little to demonstrating engineering judgment. |
| **Multi-user auth, roles & permissions beyond a single HR login** | There is one persona (HR Manager). A lightweight auth guard is enough; a full RBAC/SSO system is unnecessary complexity for the stated need. |
| **Live currency exchange rates via external API** | Introduces an external dependency and non-determinism that hurts testability. We use a fixed, seeded rate table instead — accurate enough to demonstrate cross-country normalization and keeps tests deterministic. |
| **Salary history / audit trail / approval workflows** | Valuable in production but not required to answer "how do we pay people today." Noted as a future enhancement. |
| **Bulk import/export (Excel/CSV)** | The seed script covers data volume. Import UX is a nice-to-have that doesn't change the core architecture. |
| **Real-time collaboration / notifications** | No stated need; adds infrastructure cost without demonstrating core judgment. |
| **Mobile-native apps** | Web is the stated delivery channel. Responsive web is sufficient. |

## 5. Success Criteria

- HR Manager can find any employee among 10,000 in under a second via
  search/filter/pagination.
- HR Manager can answer "what's our average/median pay by country / department /
  role?" from a dashboard without touching a spreadsheet.
- The system seeds exactly 10,000 employees reproducibly.
- Core salary/analytics logic is covered by fast, deterministic unit tests.
- The app builds and runs end-to-end (API + UI + DB).

## 6. Non-Functional Requirements

- **Performance:** list and analytics queries must handle 10k rows using DB-side
  pagination/aggregation (no loading all rows into memory).
- **Correctness:** currency normalization and median/average calculations must
  be verified by tests.
- **Maintainability:** clear layering (controller → service → repository),
  typed DTOs, and a documented API contract.
- **Testability:** deterministic seed (fixed random seed) and fixed FX rates.

## 7. Assumptions

- "Current salary" per employee is sufficient; historical salary tracking is out
  of scope.
- A small fixed set of currencies and a base currency (USD) is acceptable for
  analytics normalization.
- One HR Manager user; authentication is a simple gate, not a multi-tenant system.
