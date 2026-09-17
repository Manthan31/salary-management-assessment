# Architecture Diagram

Visual companion to [`architecture.md`](architecture.md). Diagrams use Mermaid so
they render directly on GitHub.

## System overview

```mermaid
flowchart TB
    subgraph Browser["Browser (HR Manager)"]
        UI["Angular 20 SPA<br/>Employee table · Analytics dashboard"]
    end

    subgraph Backend["Spring Boot service (modular monolith)"]
        direction TB
        SEC["Security filter<br/>API-token gate + CORS"]
        subgraph Web["Web layer (controllers)"]
            EC["EmployeeController"]
            AC["AnalyticsController"]
            RC["ReferenceController"]
        end
        subgraph Service["Service layer"]
            ES["EmployeeService"]
            AS["AnalyticsService"]
            CC["CurrencyConverter"]
            ST["SalaryStatistics<br/>(pure functions)"]
        end
        subgraph Data["Data layer (Spring Data JPA)"]
            ER["EmployeeRepository"]
            AR["AnalyticsRepository"]
            RR["Reference / FxRate repos"]
        end
        SEED["DatabaseSeeder<br/>(10k, deterministic)"]
    end

    DB[("SQLite<br/>salary.db")]

    UI -->|"HTTPS + X-API-Token<br/>JSON"| SEC
    SEC --> Web
    EC --> ES
    AC --> AS
    RC --> RR
    ES --> CC
    AS --> CC
    AS --> ST
    ES --> ER
    AS --> AR
    ES --> RR
    ER --> DB
    AR --> DB
    RR --> DB
    SEED --> DB
```

## Data model

```mermaid
erDiagram
    COUNTRY ||--o{ EMPLOYEE : "employs"
    DEPARTMENT ||--o{ EMPLOYEE : "contains"
    JOB_ROLE ||--o{ EMPLOYEE : "assigned"

    COUNTRY {
        long id PK
        string name
        string iso_code
        string currency_code
    }
    DEPARTMENT {
        long id PK
        string name
    }
    JOB_ROLE {
        long id PK
        string title
    }
    EMPLOYEE {
        long id PK
        string first_name
        string last_name
        string email UK
        long country_id FK
        long department_id FK
        long job_role_id FK
        date hire_date
        string status
        decimal base_salary
        string currency_code
    }
    FX_RATE {
        string currency_code PK
        decimal rate_to_base
    }
```

`FX_RATE` has no foreign key to employees by design: it is a small fixed lookup
table used by the service layer to normalize `base_salary` (in the employee's
local `currency_code`) to the base currency (USD) for analytics.

## Request flow: paginated employee list

```mermaid
sequenceDiagram
    participant U as Angular UI
    participant F as Security filter
    participant C as EmployeeController
    participant S as EmployeeService
    participant R as EmployeeRepository
    participant DB as SQLite

    U->>F: GET /api/employees?page&size&search&country (X-API-Token)
    F->>F: validate token
    F->>C: authorized request
    C->>S: list(filters, pageable)
    S->>R: findAll(Specification, Pageable)
    R->>DB: single indexed SQL (WHERE + LIMIT/OFFSET)
    DB-->>R: page of rows
    R-->>S: Page<Employee>
    S->>S: normalize salaries to USD (rates loaded once)
    S-->>C: PageResponse<EmployeeResponse>
    C-->>U: JSON (content + pagination metadata)
```

## Analytics flow: normalize-then-aggregate

```mermaid
flowchart LR
    Q["Query: active employees<br/>(+ optional filters)"] --> P["Fetch lean projection<br/>(group, salary, currency)"]
    P --> N["Normalize each salary<br/>to USD via FX rates"]
    N --> G["Group + aggregate<br/>count / avg / median / min / max"]
    G --> R["GroupSalaryStat[] · SalarySummary · SalaryBand[]"]
```

Salaries are stored per local currency, so normalization to USD happens **before**
aggregation. A plain SQL `AVG(base_salary)` would incorrectly mix currencies.
