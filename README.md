# Employee Salary Management

Web-based salary management for an organization with ~10,000 employees across
multiple countries. Lets an HR Manager manage employee salary data and answer
questions about how the organization pays people (by country, department, role,
and pay band), replacing an error-prone spreadsheet workflow.

> Built for the "Assessment with Product Framing" exercise. See `docs/` for the
> requirements document, architecture and design notes, and AI/trade-off artifacts.

---

## Tech stack

| Layer     | Technology |
|-----------|------------|
| Backend   | Java 17, Spring Boot 3.3, Spring Data JPA, Spring Security |
| Database  | SQLite (file-based, zero-setup) |
| Frontend  | Angular 20 (standalone components + signals), Angular Material |
| Build     | Maven (via wrapper), npm / Angular CLI |
| Tests     | JUnit 5 + Mockito + AssertJ (backend), Jasmine + Karma (frontend) |

Architecture is a **modular monolith** (not microservices) - the domain is small
and cohesive, so a single well-layered service is the right call. Full reasoning
in [`docs/architecture.md`](docs/architecture.md).

---

## Project structure

```
.
├── backend/            Spring Boot API + SQLite + 10k seed
│   └── src/main/java/com/acme/salary
│       ├── employee/    CRUD + paginated/filtered listing
│       ├── analytics/   summary, by-country/department/role, distribution
│       ├── currency/    USD normalization (fixed FX rates)
│       ├── reference/   country/department/role lookups
│       ├── seed/        deterministic 10,000-employee seeder
│       ├── config/      API-token security + CORS
│       └── common/      DTOs, error handling, pagination
├── frontend/           Angular UI (employee table + analytics dashboard)
│   └── src/app
│       ├── core/        API services, models, auth interceptor
│       ├── employees/   list (filters/pagination) + edit dialog
│       └── analytics/   dashboard (summary, distribution, grouped pay)
├── docs/               requirements, architecture, AI prompts, trade-offs
└── docker-compose.yml  run the whole stack with one command
```

---

## Prerequisites

- **JDK 17** (Eclipse Temurin recommended). Maven is not required - the project
  ships a Maven wrapper (`mvnw`).
- **Node.js 20+** and npm.
- (Optional) **Docker** + Docker Compose to run without local JDK/Node.

---

## Running locally

The app has two parts that run together: the backend API (port **8080**) and the
Angular dev server (port **4200**).

### 1. Backend (port 8080)

```bash
cd backend
# macOS / Linux
./mvnw spring-boot:run
# Windows
mvnw.cmd spring-boot:run
```

On first startup the database is empty, so the seeder generates **10,000
employees** deterministically. Subsequent starts detect existing data and skip
seeding. The SQLite file (`backend/salary.db`) is created automatically.

### 2. Frontend (port 4200)

```bash
cd frontend
npm install      # first time only
npm start        # ng serve
```

Open **http://localhost:4200**. The UI calls the backend at
`http://localhost:8080/api` and authenticates with a static API token
(`dev-hr-token`) configured in `frontend/src/environments/environment.ts`.

---

## Running with Docker (whole stack)

```bash
docker compose up --build
```

- Backend API: http://localhost:8080
- Frontend (served by nginx): http://localhost:4200

Stop with `docker compose down`.

---

## Running the tests

### Backend (25 unit tests)

```bash
cd backend
./mvnw test          # or mvnw.cmd test on Windows
```

Covers the core logic: salary statistics (average/median), currency
normalization, analytics aggregation, and employee-service business rules.
Fast and deterministic - all repositories are mocked, no database needed.

### Frontend (17 specs)

```bash
cd frontend
npm run test:ci      # headless, single run
# or `npm test` for interactive watch mode
```

Covers the HTTP services (URLs, params, auth token), the employee list, and the
analytics dashboard, with mocked services.

---

## Key API endpoints

All under `/api`, and require the header `X-API-Token: dev-hr-token`.

| Method | Path | Description |
|--------|------|-------------|
| GET | `/employees` | Paged, filterable list (`page`, `size`, `sort`, `search`, `country`, `department`, `role`, `status`) |
| GET | `/employees/{id}` | Single employee |
| POST | `/employees` | Create |
| PUT | `/employees/{id}` | Update |
| PATCH | `/employees/{id}/status` | Activate / deactivate |
| GET | `/analytics/summary` | Headcount, total/avg/median/min/max (USD) |
| GET | `/analytics/by-country` \| `by-department` \| `by-role` | Grouped pay stats |
| GET | `/analytics/distribution` | Employee count per salary band |
| GET | `/meta/countries` \| `departments` \| `roles` | Filter dropdown data |

Example:

```bash
curl -H "X-API-Token: dev-hr-token" \
  "http://localhost:8080/api/analytics/summary"
```

---

## Configuration

Backend (`backend/src/main/resources/application.properties`, override via env vars):

| Property | Env var | Default |
|----------|---------|---------|
| `app.security.api-token` | `APP_API_TOKEN` | `dev-hr-token` |
| `app.cors.allowed-origins` | `APP_CORS_ORIGINS` | `http://localhost:4200` |
| `app.seed.employee-count` | - | `10000` |
| `app.seed.random-seed` | - | `42` |

---

## Notes & scope

- Salaries are stored in each employee's local currency and normalized to a base
  currency (**USD**) for analytics using a fixed, seeded FX-rate table (keeps
  results deterministic and avoids an external dependency).
- Current salary only (no salary history), single HR persona with lightweight
  token auth, and no full payroll processing - these are deliberate scope
  decisions explained in [`docs/requirements.md`](docs/requirements.md).
```
