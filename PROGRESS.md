# Build Progress Tracker

This file tracks the 11-step build plan so work can be paused and resumed
across sessions. Each step is committed to Git when complete.

**Tech stack:** Angular (frontend) + Java/Spring Boot (backend) + SQLite (DB).
Security and microservices to be added only if genuinely needed.

## Steps

- [x] **Step 1 — Requirements document** (`docs/requirements.md`) + repo init
- [x] **Step 2 — Architecture & design notes** (`docs/architecture.md`)
- [ ] **Step 3 — Project scaffold** (backend + frontend skeletons, tooling, test runners)
- [ ] **Step 4 — Database layer** (schema, migrations)
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
