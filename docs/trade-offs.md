# Trade-offs & Engineering Decisions

This document captures the notable decisions made while building the system, the
alternatives considered, and the reasoning. The guiding principle throughout was
the assessment's own note: *not the most complex system, but good engineering
judgment.*

---

## 1. Modular monolith, not microservices

**Decision:** A single, well-layered Spring Boot service.

**Alternatives:** Separate employee, analytics, and auth microservices.

**Reasoning:** The domain is small and cohesive - employees, salaries, and
analytics over one dataset. There is no independent scaling boundary, no separate
team ownership, and no differing deployment cadence to justify a split.
Microservices would add network hops, distributed-transaction complexity, and
multiple deployments for zero benefit at this scale. Clean internal layering
(controller → service → repository) keeps the seams so a service could be
extracted later *if* a real boundary emerged. We keep the seams; we don't pay the
distribution tax now.

---

## 2. Normalize currencies before aggregating (the key correctness decision)

**Decision:** Fetch a lean `(group, salary, currency)` projection, convert each
salary to USD in the service, then aggregate.

**Alternatives:** `SELECT AVG(base_salary) ... GROUP BY country` directly in SQL.

**Reasoning:** Salaries are stored in each employee's local currency. A plain SQL
average would add euros to yen to rupees - meaningless. Correctness required
normalizing to a common currency (USD) *before* aggregating. The cost is pulling
a few columns into memory for analytics, which is acceptable for an analytics
operation (not a per-request hot path) and keeps the math in pure, unit-tested
functions (`SalaryStatistics`). This was a deliberate correctness-over-cleverness
choice.

---

## 3. Fixed, seeded FX rates instead of a live rate API

**Decision:** A small `fx_rate` table seeded with fixed rates (base = USD).

**Alternatives:** Call an external FX API at runtime.

**Reasoning:** An external API introduces a network dependency, failure modes,
rate limits, and - most importantly - non-determinism that would make tests and
demos flaky. Fixed rates keep the whole system deterministic and testable while
still fully demonstrating cross-country normalization. Swapping in a live
provider later is localized to `CurrencyConverter`.

---

## 4. Current salary only, no salary history

**Decision:** Store one current salary per employee.

**Alternatives:** A `salary_history` table with effective-dated records.

**Reasoning:** The stated need is "manage salary data" and "answer how we pay
people *today*." History, audit trails, and approval workflows are valuable in a
real system but out of scope here; adding them would expand the surface area
without demonstrating additional judgment. Noted as a future enhancement.

---

## 5. Lightweight API-token security, not full auth

**Decision:** A static token gate on `/api/**` for the single HR persona.

**Alternatives:** Username/password login, JWT sessions, RBAC, SSO.

**Reasoning:** There is exactly one persona (HR Manager). A token gate shows the
API isn't wide open and is trivially swappable for real auth, without building a
user store and role system the product doesn't need. Right-sized security.

---

## 6. SQLite, not Postgres/MySQL

**Decision:** File-based SQLite.

**Alternatives:** A client-server RDBMS.

**Reasoning:** Zero setup, no separate server, and more than sufficient for 10,000
rows and the assessment's needs. The code uses standard JPA/Hibernate, so moving
to Postgres is a dependency + dialect change, not a rewrite. For a real
multi-user production deployment, Postgres would be the choice - called out as a
future step.

---

## 7. Schema from entities (`ddl-auto=update`), not migration scripts

**Decision:** Hibernate generates the schema from JPA entities.

**Alternatives:** Flyway/Liquibase versioned migrations.

**Reasoning:** For a fresh single-database app, entities as the single source of
truth is simplest and avoids drift between two definitions. A production system
needing controlled, reversible schema changes would adopt Flyway - noted as a
future enhancement, and easy to add.

---

## 8. Server-side pagination, filtering, and aggregation

**Decision:** All list paging/filtering and all analytics aggregation happen in
the database (or over lean projections), never by loading 10k rows into the UI.

**Reasoning:** This is the core performance requirement at 10,000 employees. The
list uses composable JPA Specifications → a single indexed query with
`LIMIT/OFFSET`. Indexes exist on every filter/group column. The UI only ever
holds one page.

---

## 9. Deterministic seed with a fixed random seed

**Decision:** Seed 10,000 employees using `Random(42)`.

**Reasoning:** Reproducibility. The same dataset is generated on every machine
and every run, which makes demos consistent and any data-dependent behavior
predictable. The seeder is also idempotent (skips if data exists).

---

## 10. Angular Material + standalone components/signals

**Decision:** Angular 20 standalone components with signals, Angular Material UI.

**Alternatives:** NgModules, a custom component library.

**Reasoning:** Material provides an accessible, batteries-included table,
paginator, forms, and dialogs - saving time while looking professional.
Standalone + signals is the current idiomatic Angular style and keeps the app
lean (lazy-loaded feature routes).

---

## Summary table

| Area | Chosen | Rejected | Driver |
|------|--------|----------|--------|
| Topology | Modular monolith | Microservices | Right-sizing |
| Analytics | Normalize→aggregate | SQL AVG on mixed currency | Correctness |
| FX | Fixed seeded rates | Live API | Determinism/testability |
| Salary | Current only | Full history | Scope |
| Security | Token gate | RBAC/SSO | Single persona |
| DB | SQLite | Postgres/MySQL | Zero-setup, sufficient |
| Schema | ddl-auto | Flyway/Liquibase | Simplicity |
| Scale | Server-side paging/agg | Client-side | 10k performance |
| Seed | Fixed seed 42 | Random each run | Reproducibility |
| UI | Material + signals | Custom / NgModules | Speed + idiom |
