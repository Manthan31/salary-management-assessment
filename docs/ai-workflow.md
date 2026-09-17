# AI Workflow & Prompt Log

The assessment asks candidates to use AI tools and to show *how* they were used.
This project was built with an agentic AI coding assistant. This document
describes the workflow, the kinds of prompts used, and - importantly - where
human engineering judgment directed and corrected the AI.

## Working method

The work was structured as an **11-step plan**, executed and committed
incrementally so the Git history tells the story of how the solution evolved:

1. Requirements document
2. Architecture & design notes
3. Project scaffold (backend + frontend)
4. Database layer (entities + repositories)
5. Deterministic 10,000-employee seed
6. Backend core (CRUD + analytics + security)
7. Backend unit tests
8. Angular UI
9. Frontend unit tests
10. Deployment (Docker) + README
11. Artifacts (this document, diagrams, trade-offs)

Each step was verified before moving on (compiles, tests pass, endpoints respond,
the app runs) and committed separately. A `PROGRESS.md` tracker was kept so work
could be paused and resumed across sessions without losing context.

## How AI was used intentionally

- **Plan first, then build.** The requirements and architecture documents were
  written and committed *before* any code, so the AI generated code against an
  explicit design rather than improvising.
- **Small, verifiable increments.** Each step produced a working, tested slice,
  not a big-bang dump. This kept quality high and made review easy.
- **AI for breadth, human for judgment.** The AI accelerated boilerplate
  (entities, DTOs, controllers, Material wiring, test scaffolding). The key
  decisions - modular monolith, normalize-before-aggregate, fixed FX rates,
  scope boundaries - were engineering choices that directed the AI.

## Representative prompts

Paraphrased examples of the kinds of instructions given:

- *"Write a one-page requirements document: goal, scope, deliberate exclusions
  with reasoning, success criteria."*
- *"Design the data model and REST API for employee management + salary analytics;
  choose modular monolith vs microservices and justify it."*
- *"Scaffold a Spring Boot 3 (Java 17) backend with SQLite and an Angular 20 +
  Material frontend; use the Maven wrapper."*
- *"Write a deterministic seeder for 10,000 employees across countries with fixed
  FX rates; make it idempotent."*
- *"Implement employee CRUD with server-side pagination/filtering via JPA
  Specifications, and analytics endpoints (summary, by-country/department/role,
  distribution) normalized to USD."*
- *"Write fast, deterministic unit tests for the median/average math, currency
  conversion, and the employee service using mocks."*
- *"Build the Angular UI: employee table with filters + pagination, an edit
  dialog, and an analytics dashboard; wire an auth interceptor for the API token."*
- *"Add Docker + docker-compose and a README with run/build/test instructions."*

## Where human judgment corrected or steered the AI

Real engineering is as much about catching issues as generating code. Examples
from this build:

- **Environment constraints.** The build machine had JDK 8 and no Maven. Rather
  than accept a fragile Spring Boot 2.7 + hand-written SQLite dialect, the
  decision was made to install JDK 17 and use Spring Boot 3 with the official
  Hibernate community SQLite dialect - a cleaner, more current stack. The AI's
  first pass targeted the installed JDK 8; this was overridden deliberately.
- **Correctness over the easy path.** The naive analytics approach (SQL `AVG`)
  was rejected in favour of normalizing per-currency salaries to USD before
  aggregating.
- **Scope discipline.** Salary history, full payroll, live FX, and bulk import
  were deliberately excluded and documented, resisting scope creep.
- **Verification, not assumption.** Results were checked against reality - e.g.
  querying the SQLite file directly to confirm exactly 10,000 rows and unique
  emails, and running the live API to confirm currency conversion and that
  distribution bands summed to the active headcount, rather than trusting logs.
- **Dead-code and dependency hygiene.** Unused imports/methods were cleaned up;
  a peer-dependency conflict when adding `@angular/animations` was resolved by
  matching the exact installed Angular core version.

## Tooling notes

- Backend: Java 17 (Temurin), Spring Boot 3.3, Maven wrapper.
- Frontend: Angular 20, Angular Material, npm.
- Tests: JUnit 5 + Mockito + AssertJ; Jasmine + Karma (headless Chrome).
- Version control: incremental Git commits, one per step.
