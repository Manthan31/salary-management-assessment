# Demo Video Script & Checklist

A short, structured walkthrough (aim for **3-5 minutes**) that shows the software
working and communicates the thinking behind it.

## Before recording

1. Start the backend:
   ```bash
   cd backend && ./mvnw spring-boot:run     # mvnw.cmd on Windows
   ```
   Wait for `Started SalaryManagementApplication` and, on first run,
   `Seeding complete: 10000 employees.`
2. Start the frontend:
   ```bash
   cd frontend && npm start
   ```
3. Open **http://localhost:4200** in the browser.
4. Have a terminal ready to optionally run the test suites on camera.

## Script

### 1. Intro (~20s)
- "This is a salary management tool for an HR manager at a 10,000-employee,
  multi-country org. It replaces spreadsheets with a web app that also answers
  how the organization pays people."
- One line on the stack: Angular + Spring Boot + SQLite, modular monolith.

### 2. Employee directory (~60s)
- Show the table loading a page of employees (not all 10,000 - pagination is
  server-side).
- Type in the **search** box → results filter (debounced, backend query).
- Use the **country / department / role / status** filters → show the total
  count change.
- Change the **page size** and page through; sort by a column.
- Point out both the **local salary** and the **USD-normalized** salary columns.

### 3. Create / edit (~40s)
- Click **Add Employee**, fill the form, show validation (e.g. invalid email,
  missing field), then create successfully.
- Edit an employee; toggle a status **Active → Inactive**.

### 4. Analytics dashboard (~70s)
- Switch to **Analytics**.
- Summary cards: headcount, average, **median**, total payroll, range - all in
  USD.
- Note the headcount matches active employees.
- Salary **distribution** bars.
- Toggle the grouped table between **Country / Department / Role**.
- Apply a filter (e.g. country = Germany) and show every panel update together.
- Mention: salaries are normalized to USD *before* aggregating, so cross-country
  comparisons are correct.

### 5. Engineering & tests (~40s)
- Briefly show the commit history (incremental, one per step).
- Run the backend tests: `cd backend && ./mvnw test` → 25 pass.
- Run the frontend tests: `cd frontend && npm run test:ci` → 17 pass.
- Mention the artifacts in `docs/` (requirements, architecture, trade-offs,
  AI workflow).

### 6. Close (~15s)
- One or two deliberate trade-offs (modular monolith over microservices; fixed
  FX rates for determinism) and one future enhancement (salary history / Postgres
  for production).

## Things to explicitly call out (scoring signals)

- Server-side pagination/filtering → responsive at 10k rows.
- Median + USD normalization → correctness of the "how we pay people" answer.
- Deterministic seed (10,000 employees, seed 42).
- Meaningful, fast, deterministic tests on both sides.
- Clear scope decisions and documented trade-offs.
```
