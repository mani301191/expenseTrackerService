# expenseTrackerService — Current Functionality

A Spring Boot REST backend for a personal expense/income tracking application, backed by **MongoDB**, with a bundled **Angular SPA** served from `static/`. Single-user oriented (profile is a singleton record).

## Tech Stack & Build

- **Spring Boot 2.5.0** (Java 8 target), Maven (`mvnw` wrapper included)
- **spring-boot-starter-web** — REST API
- **spring-boot-starter-data-mongodb** — persistence via `MongoTemplate`
- **spring-boot-starter-validation** — `@Valid`/`@Validated` request-body checks
- **Apache POI 5.2.3 (poi-ooxml)** — Excel bank-statement import
- Entry point: `MyExpenseApplication` (`src/main/java/com/audit/myexpense/MyExpenseApplication.java`)
- Build output: the Spring Boot plugin copies the jar to `D://src/application/`

## Architecture Notes

- **No service/repository layer.** Every controller injects Spring Data's `MongoTemplate` directly and runs queries/aggregations inline. Controllers own both business logic and data access.
- **CORS:** all REST controllers allow origin `http://localhost:4200` (Angular dev server).
- **Frontend hosting:** `ExpenseViewController` (`@Controller`) maps `/` and `/dashboard` to the compiled Angular app in `static/index.html`.

## Configuration (`application.properties`)

| Property | Value |
|---|---|
| Server port | `8003` |
| MongoDB URI | `mongodb://localhost:27018/personal` |
| Auto index creation | enabled (creates unique compound indexes) |
| MVC view suffix | `.html` |

## Feature Areas & API Endpoints

### Expense Tracking — `ExpenseTrackerController` (`/api/expenseTracker`)
- `POST /expenseDetail` — create expense; auto-generates sequential `expenseId`, derives month/year from `expenseDate`. Duplicate protection via unique index on `(expenseDate, amount, description)`.
- `GET /expenseDetails?year&month&expenseOf&expenseType` — list with optional filters, sorted newest first.
- `PATCH /expenseDetail` — update amount/type/category/description; stamps `updatedDate`.
- `DELETE /expenseDetail/{expenseId}` — delete by id.

### Bank Statement Upload — `ExpenseUploadController` (`/api/expenseTracker`)
- `POST /uploadStatement` (multipart field `file`) — parses an `.xlsx` statement (Apache POI, sheet 0):
  - **DR rows → expenses**: auto-categorized against that month's planned targets (case-insensitive contains match); unmatched become `"Uncategorized"` + `"UnPlanned"`. Descriptions containing keywords like `nps`, `ssa`, `zerodha`, `coin`, `sip`, `mutual fund` are typed `"Investment"`.
  - **CR rows → income**: imported into income collection.
  - Per-row duplicates are skipped so the import continues.
  - Sample template shipped at `static/assets/templates/expense/statement-template.xlsx`.

### Income Tracking — `IncomeTrackerController` (`/api/incomeTracker`)
- `POST /incomeDetail` — create income (sequential `incomeId`, month/year derived from date).
- `GET /incomeDetails?year&month` — list incomes.
- `DELETE /incomeDetail/{incomeId}` — delete.

### Budget Planning — `MonthlyTargetController` (`/api/monthlyTarget`)
- `POST /monthlyTarget` — save a batch of planned budget lines (month/year derived per item).
- `GET /monthlyTarget?year&month` — list targets for a month.
- `DELETE /monthlyTarget/{id}` — delete a target.
- `GET /monthlyTarget/clone?year&month` — clone a month's targets into new records (new UUID ids, date reset to now).

### Target vs Actual — `MonthlyStatusController`
- `GET /api/monthlyStatus?year&month` — for each target line, sums matching expenses (`expenseOf == target description`); returns estimated vs actual amounts.

### Summaries & Dashboard
- `SummaryController`: `GET /api/summary` — dashboard payload with:
  - current-month income vs expense vs estimate,
  - per-person fitness weight summaries (min/max/current),
  - active insurances (end date in future, soonest first).
- `MonthlySummaryController`:
  - `GET /api/monthlySummary?year[&month]` — monthly roll-up (expense/income/savings vs estimates) via Mongo aggregation.
  - `GET /api/dailySummary?year&month` — day-wise expense totals for every day of the month (zero-filled).
- `YearlySummaryController`:
  - `GET /api/yearlySummary[?year]` — yearly totals incl. savings and per-`expenseType` breakdown.
  - `GET /api/monthlyExpByCatagory?year` — pivot of spending per category across January–December (note the endpoint spelling).

- `PlannedExpenseController`: `GET /api/plannedExpense?year&month` — dropdown of planned-expense names for a month.

### Investments — `InvestmentController` & `FixedDepositController` (`/api/investment`)
Investments: create / list / delete / patch (status + additional details), UUID ids.
Fixed deposits: create / list / delete / full patch (bank, account, dates, rate, nominee, amounts), UUID ids.

### Insurance — `InsuranceController` (`/api/insurance`)
Create policy, list (sorted by end date ascending), delete, patch additional details only.

### Fitness & Medical — `FitnessDetailController` (`/api/fitness`)
- Persons: register person (`_id` = name), list (enriched with weight trend history), delete.
- Weight log: add/list/delete/update weight+height entries per person; dropdown of person names.
- Medical records: add/list (per patient, newest first)/delete/update visits (problem, hospital, doctor, diagnosis).

### Assets & Appliances
- `AssetDetailController` (`/api/asset`) — CRUD-ish: create, list, delete, patch (name/status/comments/image/type).
- `AppliancesController` (`/api/appliances`) — appliance records with AMC info: create, list, delete, patch.

### Career Timeline — `CareerDetailController` (`/api/career`)
Work/education records: create, list (sorted by end date), delete, patch endDate/comments.

### Events — `EventDetailController` (`/api/event`)
Calendar events (birthday/anniversary/festival/payment due/etc.) with recurrence: create, list (by event date), delete.

### Profile — `ProfileDetailController` (`/api/profile`)
Get/save the user profile; always stored under hard-coded `profileId = 1` (singleton).

### App Config & Dropdowns — `AppConfigController` (`/api/config`)
- `POST /appConfig` / `GET /appConfig` / `DELETE /{key}` — key/value config entries (key = `_id`).
- `GET /dropDown?key=...` — splits a comma-separated config value into a dropdown list.
- `POST /default` — seeds default option sets (asset types/statuses, investment types/statuses, insurance types, event types). Must be called once before dropdown-driven features work.

## Data Model (MongoDB collections)

| Domain | Collection(s) |
|---|---|
| Expenses | `myExpenseDetail` (unique index on date+amount+description) |
| Income | `myIncomeDetail` (unique index on date+amount+source) |
| Budget targets | `myMonthlyTarget` |
| Investments | `myInvestments`, `fixedDeposits` |
| Insurance | `myInsuranceDetails` |
| Fitness/Medical | `fitnessDetails`, `fitnessWeightData`, `medicalDetails` |
| Assets/Appliances | `myAssetDetails`, `myAppliances` |
| Career/Events | `myCareerDetail`, `myEventDetail` |
| Profile/Config | `profileDetail`, `appConfig` |

Response DTOs (not persisted): `DashboardData`, `ExpenseTrackingSummary`, `MonthlySummary`, `YearlySummary`, `Category`, `DayWiseExpense`, `FitnessSummary`, `InsuranceSummary`, `MonthlyStatus`, `Dropdown`, category-pivot models. `PersonalDetails` exists but is currently unused by any controller.

## Cross-Cutting Behavior

- **Duplicate prevention:** enforced by Mongo unique compound indexes; violations surface as HTTP 400 `"Record already Exists"`.
- **Validation:** create endpoints validate bodies (`@Valid`); failures return HTTP 400 with a list of `field - message` errors (global handler: `MyExpenseExceptionHandler`). PATCH endpoints do not validate.
- **ID generation:** expenses/income use app-side sequential integers (max + 1); everything else uses UUIDs.
- **Dates:** JSON dates formatted `dd/MM/yyyy` (targets use ISO format); months stored as English month names; `updatedDate` audit stamp set server-side and hidden from responses (`ExpenseCommonUtil.formattedDate`).
- **Error handling:** only two handlers exist (validation errors, duplicate keys). Missing-id deletes/updates return HTTP 200 with a message body rather than error codes.

## Running

```bash
# prerequisites: Java 8+, Maven, MongoDB on localhost:27018
mvnw clean install
mvnw spring-boot:run
```

Then open `http://localhost:8003` (serves the bundled Angular UI) or call the APIs directly. CORS permits the Angular dev server at `http://localhost:4200`.
