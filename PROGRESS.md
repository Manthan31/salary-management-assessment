# Build Progress Tracker

This file tracks the 11-step build plan so work can be paused and resumed
across sessions. Each step is committed to Git when complete.

**Tech stack:** Angular (frontend) + Java/Spring Boot (backend) + SQLite (DB).
Security and microservices to be added only if genuinely needed.

## Steps

- [x] **Step 1 — Requirements document** (`docs/requirements.md`) + repo init
- [x] **Step 2 — Architecture & design notes** (`docs/architecture.md`)
- [x] **Step 3 — Project scaffold** — frontend (Angular 20 + Material 20.2.14,
      azure-blue theme) + backend (Spring Boot 3.3.5 on Java 17, SQLite via
      Hibernate 6 community dialect, Maven wrapper). Verified compile.
- [x] **Step 4 — Database layer** — JPA entities (Country, Department, JobRole,
      FxRate, Employee + EmploymentStatus enum) with indexes on FK/search
      columns, and Spring Data repositories. Schema auto-created via Hibernate
      ddl-auto=update. Verified compile.
- [ ] **Step 5 — Seed script** (10,000 employees, deterministic)
- [ ] **Step 6 — Backend core** (employee CRUD + salary analytics endpoints)
- [ ] **Step 7 — Backend unit tests** (analytics math, validation, handlers)
- [ ] **Step 8 — Frontend UI** (employee table, detail/edit, analytics dashboard)
- [ ] **Step 9 — Frontend tests** (key components + data logic)
- [ ] **Step 10 — Deployment + README** (env config, run instructions)
- [ ] **Step 11 — Final artifacts** (AI prompt log, trade-offs, arch diagram, demo script)

## Notes / Decisions Log

- 2026-09-16: Stack confirmed as Angular + Spring Boot + SQLite.
- 2026-09-16: Salary history, full payroll, live FX rates, and bulk import
  deliberately out of scope (see requirements.md §4).
- Project lives in its own Git repo, separate from the parent workspace.
- 2026-09-16: Architecture = modular monolith (NOT microservices) — no real
  service boundaries at this scale. Security = minimal token/API-key gate for
  the single HR persona. DB = SQLite. UI kit = Angular Material. FX = fixed
  seeded rate table, base USD. See docs/architecture.md.
- 2026-09-16: ENV FINDINGS: JDK 8 (1.8.0_362) installed, NO global Maven (using
  mvn wrapper), Node v24 + Angular CLI present (npm must run via cmd due to PS
  execution policy). Angular analytics disabled globally.
- 2026-09-16: Step 4 schema strategy: entities are the source of truth via
  Hibernate ddl-auto=update against a fresh SQLite file. No separate migration
  tool (Flyway/Liquibase) - unnecessary for a single-DB assessment; noted as a
  future enhancement if the schema needed versioned production migrations.
- 2026-09-16: RESOLVED: Installed JDK 17 (Temurin at
  "C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot") WITHOUT changing
  system JAVA_HOME/PATH, so existing JDK 8 projects are untouched. Backend now
  Spring Boot 3.3.5 + Java 17. Builds must set JAVA_HOME to the JDK 17 path
  explicitly (mvnw). Frontend npm/ng must run via `cmd /c` (PS exec policy).
