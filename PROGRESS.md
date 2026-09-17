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
- [x] **Step 5 — Seed script** — DatabaseSeeder (CommandLineRunner) + SeedData.
      Deterministic (fixed seed 42), idempotent (skips if data exists), batched
      inserts. VERIFIED against SQLite: employee=10000, country=10, department=10,
      job_role=15, fx_rate=9, distinct_emails=10000, ACTIVE=9178/INACTIVE=822.
- [x] **Step 6 — Backend core** — Employee CRUD (/api/employees: paged+filtered
      list, get, create, update, PATCH status) via Specifications; Analytics
      (/api/analytics: summary, by-country, by-department, by-role, distribution)
      with USD normalization; meta endpoints (/api/meta); API-token security +
      CORS; global error handler. VERIFIED live: 10000 total, Germany filter=1009,
      summary headcount 9178 (active), FX conversion correct, distribution sums to
      9178, 404 on missing. NOTE: AnalyticsService has unused groupBy()+Function
      import - user rejected removal, leave as-is.
- [x] **Step 7 — Backend unit tests** — 25 tests, all passing, ~6s, deterministic.
      SalaryStatisticsTest (10: median odd/even/order-independent/empty, avg, sum,
      min/max), CurrencyConverterTest (6: load/convert/round/errors, Mockito),
      AnalyticsServiceTest (4: mixed-currency normalization, grouping+sort,
      distribution partition), EmployeeServiceTest (5: create, dup email, missing
      country, not-found, status). Pure unit tests with mocks - no DB, no context.
- [x] **Step 8 — Frontend UI** — Angular 20 standalone + Material. Core: models,
      authInterceptor (X-API-Token), employee.service, analytics.service, env
      config. Shell with toolbar nav. EmployeeList (Material table, search debounce,
      country/dept/role/status filters, server-side paginator + sort, status toggle),
      EmployeeEditDialog (create/edit, validation), AnalyticsDashboard (summary
      cards, distribution bars, by-country/dept/role toggle table, filters).
      Installed @angular/animations@20.3.31 (matched core). angular.json prod
      fileReplacements added. VERIFIED: prod build OK; live run frontend 200 +
      API reachable (10000 employees, headcount 9178) through the UI token.
- [x] **Step 9 — Frontend tests** — 17 specs, all passing (~0.3s, ChromeHeadless).
      app.spec (shell/nav, rewritten from default), employee.service.spec (URL/
      params/token via HttpTestingController), analytics.service.spec (filters/
      endpoints), analytics-dashboard.spec (load, render, bandPercent, dimension
      switch with mocked services), employee-list.spec (load, paginate, toggle
      status, clear filters). Added `npm run test:ci` (headless single-run).
- [x] **Step 10 — Deployment + README** — Root README (stack, structure, local
      run, Docker, tests, API table, config). backend/Dockerfile (multi-stage
      temurin build via mvnw + JRE runtime), frontend/Dockerfile (node build +
      nginx serve, /api proxied to backend), frontend/nginx.conf, docker-compose.yml
      (backend:8080, frontend:4200, persisted SQLite volume), .dockerignore files.
      VERIFIED: production ng build succeeds -> dist/frontend/browser (matches
      Dockerfile copy). NOT verified: `docker compose up` (Docker not installed on
      this machine) - config is standard; both underlying builds pass.
- [x] **Step 11 — Final artifacts** — docs/architecture-diagram.md (Mermaid:
      system, ER, sequence, analytics flow), docs/trade-offs.md (10 decisions +
      summary table), docs/ai-workflow.md (method, prompts, where human judgment
      steered/corrected AI), docs/demo-script.md (3-5 min walkthrough + checklist).

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
