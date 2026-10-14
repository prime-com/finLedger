# FinLedger — Personal Finance Management System

FinLedger is a full-stack personal finance management system designed with enterprise software architecture principles. It enables users to record income and expenses in Indian Rupees (₹), categorize transactions, filter records dynamically, and inspect financial summaries (total income, total expenses, net balance).

---

## 1. System Architecture

The application adopts a **layered enterprise architecture** to ensure separation of concerns, testability, and maintainability:

```
                  ┌───────────────────────────────┐
                  │      Angular Frontend         │  (Phase 2)
                  │   (http://localhost:4200)     │
                  └──────────────┬────────────────┘
                                 │ HTTP / JSON (CORS enabled)
                                 ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Spring Boot 3.x Backend                      │
│                                                                 │
│  [ Controller Layer ]                                           │
│  • TransactionController (/api/v1/transactions)                 │
│  • GlobalExceptionHandler (HTTP 400, 404, 500 mapping)          │
│                                │                                │
│                                ▼                                │
│  [ DTO Layer ]                                                  │
│  • TransactionRequestDTO (Bean Validation: @NotBlank, @Positive)│
│  • TransactionResponseDTO & FinancialSummaryDTO                 │
│                                │                                │
│                                ▼                                │
│  [ Service Layer ]                                              │
│  • TransactionService (Business logic, financial calculations)  │
│                                │                                │
│                                ▼                                │
│  [ Repository / Persistence Layer ]                             │
│  • TransactionRepository (Spring Data JPA)                      │
│  • TransactionSpecification (Type-safe Criteria API filtering)  │
│                                │                                │
│                                ▼                                │
│  [ Entity Layer ]                                               │
│  • Transaction (@Entity mapped to PostgreSQL `transactions`)    │
└────────────────────────────────┬────────────────────────────────┘
                                 │ JDBC (HikariCP)
                                 ▼
                  ┌───────────────────────────────┐
                  │      PostgreSQL Database      │
                  │        (finledger_db)         │
                  └───────────────────────────────┘
```

### Key Architectural Decisions for Interviews

1. **`BigDecimal` for Currency**:
   - `double` and `float` use binary floating-point representation (IEEE 754), leading to precision inaccuracies (e.g., `0.1 + 0.2 = 0.30000000000000004`).
   - `BigDecimal` guarantees exact decimal arithmetic essential for financial applications. Database column is `NUMERIC(14,2)`.
2. **DTO Layer Separation**:
   - Internal JPA entities are decoupled from external API request/response payloads to prevent over-posting vulnerabilities and keep domain logic isolated.
3. **Spring Data JPA Specifications**:
   - Dynamic query filters (by type, category, and date range) are built using JPA `Specification` and `CriteriaBuilder`. This avoids fragile SQL fragments (such as `(? is null or ...)` which causes PostgreSQL type casting errors with untyped null parameters).
4. **Centralized Exception Handling**:
   - `@RestControllerAdvice` captures `MethodArgumentNotValidException`, `ResourceNotFoundException`, and type mismatches, returning uniform JSON error responses with HTTP status codes (400, 404, 500).
5. **Dual Database Strategy for Testing**:
   - Development & Production target PostgreSQL.
   - Unit and integration tests run with in-memory H2 (PostgreSQL compatibility mode), allowing self-contained builds without external database dependencies.

---

## 2. Prerequisites & Environment Status

| Prerequisite | Required Version | Status in Current Environment |
|---|---|---|
| **Java (JDK)** | Java 17, 21, or 25 | **Installed**: OpenJDK 25.0.2 / Temurin |
| **Maven** | 3.9+ | **Installed**: Apache Maven 3.9.13 |
| **PostgreSQL** | 14+ | **Installed & Running**: PostgreSQL 15.18 on port 5432 |
| **Database** | `finledger_db` | **Created**: `finledger_db` |

---

## 3. Database Configuration

Spring Boot connects to PostgreSQL using environment variables with sensible defaults:

```properties
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/finledger_db}
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD:}
```

### Creating the PostgreSQL Database Manually (if setting up on a new machine)

```bash
# Log in to PostgreSQL CLI
psql -U postgres

# Create the database
CREATE DATABASE finledger_db;

# Exit psql
\q
```

Or using macOS Homebrew CLI:
```bash
createdb finledger_db
```

### Configuring Environment Variables

Create a `.env` file or export credentials in your shell before starting:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/finledger_db
export DB_USERNAME=tanmaydeepakshinde   # Replace with your local PostgreSQL username
export DB_PASSWORD=your_password        # Leave blank if local user has trust auth
```

An example configuration file is provided in [`backend/.env.example`](backend/.env.example).

---

## 4. Building and Running the Backend

### Run Automated Tests (Unit & Integration)
```bash
cd backend
mvn clean test
```
*Current test suite: 18 tests passing (0 failures, 0 errors).*

### Package Executable JAR
```bash
cd backend
mvn clean package
```

### Start the Spring Boot Backend

**Option 1: Using Maven Spring Boot Plugin**
```bash
cd backend
DB_USERNAME=$(whoami) mvn spring-boot:run
```

**Option 2: Running the Packaged JAR**
```bash
cd backend
DB_USERNAME=$(whoami) java -jar target/finledger-backend-1.0.0.jar
```

The server will start at: `http://localhost:8080`.

---

## 5. REST API Documentation

Base URL: `http://localhost:8080/api/v1/transactions`

### 1. Create a Transaction
- **Method**: `POST`
- **URL**: `/api/v1/transactions`
- **Request Body**:
```json
{
  "title": "Software Engineering Salary",
  "amount": 75000.00,
  "category": "Salary",
  "type": "INCOME",
  "transactionDate": "2026-03-01",
  "notes": "Monthly payroll"
}
```
- **Response**: `201 Created`
```json
{
  "id": 1,
  "title": "Software Engineering Salary",
  "amount": 75000.00,
  "category": "Salary",
  "type": "INCOME",
  "transactionDate": "2026-03-01",
  "notes": "Monthly payroll",
  "createdAt": "2026-10-12T22:51:38.976773"
}
```

### 2. Get All Transactions (with Optional Filters)
- **Method**: `GET`
- **URL**: `/api/v1/transactions`
- **Query Parameters (all optional)**:
  - `type`: `INCOME` or `EXPENSE`
  - `category`: String (case-insensitive, e.g. `Food`, `Housing`)
  - `startDate`: `YYYY-MM-DD` (e.g. `2026-03-01`)
  - `endDate`: `YYYY-MM-DD` (e.g. `2026-03-31`)
- **Example**: `GET /api/v1/transactions?type=EXPENSE&category=Food`
- **Response**: `200 OK`

### 3. Get Financial Summary
- **Method**: `GET`
- **URL**: `/api/v1/transactions/summary`
- **Query Parameters (optional)**:
  - `startDate`: `YYYY-MM-DD`
  - `endDate`: `YYYY-MM-DD`
- **Response**: `200 OK`
```json
{
  "totalIncome": 75000.00,
  "totalExpense": 26500.00,
  "netBalance": 48500.00,
  "totalTransactions": 3,
  "incomeCount": 1,
  "expenseCount": 2
}
```

### 4. Get Transaction by ID
- **Method**: `GET`
- **URL**: `/api/v1/transactions/{id}`
- **Response**: `200 OK` (or `404 Not Found` if nonexistent)

### 5. Update an Existing Transaction
- **Method**: `PUT`
- **URL**: `/api/v1/transactions/{id}`
- **Request Body**: Full `TransactionRequestDTO` JSON
- **Response**: `200 OK`

### 6. Delete a Transaction
- **Method**: `DELETE`
- **URL**: `/api/v1/transactions/{id}`
- **Response**: `204 No Content`

---

## 6. Input Validation & Error Handling

When client input fails validation, the API returns a structured HTTP 400 Bad Request:

```json
{
  "timestamp": "2026-10-12T22:54:32.640018",
  "status": 400,
  "error": "Validation Failed",
  "message": "Input validation failed for one or more fields",
  "path": "/api/v1/transactions",
  "validationErrors": {
    "amount": "Amount must be greater than zero",
    "title": "Title is required",
    "type": "Transaction type must be either INCOME or EXPENSE"
  }
}
```

---

---

## 7. Angular Frontend Setup (Phase 2)

The frontend is built with Angular 19 using standalone components, Reactive Forms, and HttpClient communicating directly with the Spring Boot backend (`http://localhost:8080/api/v1/transactions`).

### Frontend Architecture
- **Summary Cards**: Displays Total Income, Total Expenses, and Net Balance in Indian Rupees (₹) with `en-IN` locale formatting.
- **Transaction Table**: Color-coded amounts (emerald for Income, rose for Expenses), category badges, notes preview, and responsive horizontal scrolling.
- **Dynamic Filters**: Instant filtering by Type (All / Income / Expense), Category, and Date range.
- **Reactive Forms Modal**: Unified create/edit modal with client-side Bean Validation parity (amounts $\ge$ ₹0.01, required title/category/date, duplicate click protection).
- **Confirmation Dialog**: Modal prompt with transaction details before deletion.
- **Toast Notifications**: Auto-dismissing alerts for create, update, delete, and API error states.

### Running Frontend Tests
```bash
cd frontend
npm test -- --watch=false --browsers=ChromeHeadless
```
*(14 tests passing)*

### Building Frontend for Production
```bash
cd frontend
npm run build
```

### Starting the Angular Development Server
```bash
cd frontend
npm start
```
Runs at: `http://localhost:4200`

---

---

## 8. Free Cloud Deployment Guide (Neon + Render + Vercel)

This application is ready for 100% free cloud deployment using:
1. **Neon** — Managed Serverless PostgreSQL (Database)
2. **Render** — Spring Boot 3 Containerized Web Service (Backend)
3. **Vercel** — Angular 19 Edge Static Hosting (Frontend)

```
┌────────────────────────┐         HTTPS / JSON         ┌─────────────────────────┐
│     Vercel (Free)      │ ───────────────────────────> │      Render (Free)      │
│   Angular 19 Frontend  │   CORS: restricted to Vercel │   Spring Boot 3 Docker  │
│ finledger.vercel.app   │ <─────────────────────────── │  finledger.onrender.com │
└────────────────────────┘                              └────────────┬────────────┘
                                                                     │ JDBC + TLS (sslmode=require)
                                                                     ▼
                                                        ┌─────────────────────────┐
                                                        │       Neon (Free)       │
                                                        │  Serverless PostgreSQL  │
                                                        │      finledger_db       │
                                                        └─────────────────────────┘
```

---

### Step 1: Managed Database Setup on Neon

1. Navigate to **[neon.tech](https://neon.tech)** and sign up / log in with GitHub.
2. Click **Create Project**:
   - **Project Name**: `finledger-db`
   - **Database Name**: `finledger_db`
   - **Region**: Select the region closest to you (e.g., `AWS us-east-2` or `eu-central-1`).
3. In the Neon Project Dashboard, locate the **Connection Details** widget:
   - Select the dropdown for language/driver: **Java / JDBC** (or look at the connection string).
   - Note the connection components:
     - **Host**: `ep-xxxx-xxxx.us-east-2.aws.neon.tech`
     - **Database**: `finledger_db`
     - **Username**: e.g., `neondb_owner`
     - **Password**: `[Your Neon generated password]`
   - Your complete JDBC URL format is:
     ```
     jdbc:postgresql://<neon-host>/finledger_db?sslmode=require
     ```

---

### Step 2: Backend Deployment on Render

1. Navigate to **[render.com](https://render.com)** and log in.
2. Click **New +** > **Web Service**.
3. Connect your GitHub repository: `https://github.com/prime-com/finLedger`.
4. Configure the Web Service settings:
   - **Name**: `finledger-backend` (will give `https://finledger-backend.onrender.com`)
   - **Region**: Select the same or closest region to your Neon DB.
   - **Branch**: `main`
   - **Root Directory**: `backend`
   - **Runtime**: **Docker**
   - **Instance Type**: **Free**
5. In **Environment Variables**, add the following key-value pairs:
   | Key | Value | Description |
   |---|---|---|
   | `DB_URL` | `jdbc:postgresql://<neon-host>/finledger_db?sslmode=require` | Neon JDBC connection string with TLS enabled |
   | `DB_USERNAME` | `<your-neon-username>` | Database user from Neon |
   | `DB_PASSWORD` | `<your-neon-password>` | Database password from Neon |
   | `FRONTEND_URL` | `https://your-finledger-app.vercel.app` | Your Vercel frontend URL (update after Step 3) |
   | `SPRING_PROFILES_ACTIVE` | `default` | Active Spring profile |
6. Click **Deploy Web Service**.
   - Render will build the multi-stage [`backend/Dockerfile`](backend/Dockerfile), extract the lightweight JRE layer, allocate a dynamic `$PORT`, and run the service.
   - Copy your public service URL: `https://<your-service-name>.onrender.com`.

> [!NOTE]
> **Free Tier Cold Starts**: Render's free tier spins down web services after 15 minutes of inactivity. When accessed again, the first request may take 30–50 seconds to boot the container. Subsequent requests run instantaneously.

---

### Step 3: Frontend Deployment on Vercel

1. Open [`frontend/src/environments/environment.production.ts`](frontend/src/environments/environment.production.ts) and set `apiUrl` to your deployed Render URL:
   ```typescript
   export const environment = {
     production: true,
     apiUrl: 'https://<your-render-service-name>.onrender.com/api/v1/transactions'
   };
   ```
2. Navigate to **[vercel.com](https://vercel.com)** and log in.
3. Click **Add New...** > **Project** and import your GitHub repository (`finLedger`).
4. In the Project Configuration:
   - **Framework Preset**: `Angular`
   - **Root Directory**: Click "Edit" and choose **`frontend`**
   - **Build and Output Settings**:
     - **Build Command**: `npm run build`
     - **Output Directory**: `dist/frontend/browser`
5. Click **Deploy**.
   - Vercel builds the standalone Angular 19 app and applies [`vercel.json`](frontend/vercel.json) client-side routing rewrites.
6. Copy your public Vercel domain (e.g. `https://finledger.vercel.app`).
7. **Complete the CORS Link**:
   - Go back to your Render Dashboard > `finledger-backend` > **Environment**.
   - Update `FRONTEND_URL` with your exact Vercel URL (e.g. `https://finledger.vercel.app`).
   - Click **Save Changes** (Render will automatically redeploy with the updated CORS policy).

---

## 9. Running the Complete Application Locally

Open two terminal tabs:

**Terminal 1 — Spring Boot Backend:**
```bash
cd backend
DB_USERNAME=$(whoami) mvn spring-boot:run
```

**Terminal 2 — Angular Frontend:**
```bash
cd frontend
npm start
```

Visit **`http://localhost:4200`** in your browser.

---

## 10. Roadmap
- [x] **Phase 1**: Spring Boot REST Backend, Hibernate/PostgreSQL persistence, validation, JUnit/MockMvc tests.
- [x] **Phase 2**: Angular 19 frontend (dashboard cards in ₹ INR, reactive forms, transaction table, dynamic filters, dark theme, karma tests).
- [x] **Phase 3**: Free cloud deployment preparation (Neon PostgreSQL TLS, Render Docker container, Vercel SPA rewrites).
- [ ] **Phase 4**: Resume & interview preparation notes.
