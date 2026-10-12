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

## 7. Roadmap
- [x] **Phase 1**: Spring Boot REST Backend, Hibernate/PostgreSQL persistence, validation, tests.
- [ ] **Phase 2**: Angular frontend (dashboard summary cards in ₹ INR, transaction table, interactive add/edit modal, filters).
- [ ] **Phase 3**: End-to-end full-stack integration verification.
- [ ] **Phase 4**: Resume & interview preparation notes.
