# Construction Site Intelligence & Safety Management System (CSIMS)

A modern, enterprise-grade full-stack web application for construction site intelligence, workforce coordination, equipment fleet management, material inventory, OSHA safety inspections, and incident tracking.

Built with **React 18 + Vite** on the frontend, **Spring Boot 3 + Spring JdbcTemplate** (strictly NO JPA, NO Hibernate) on the backend, and **MySQL 8** with automated **Flyway** migrations.

---

## 1. System Architecture & Tech Stack

```
+---------------------------------------------------------------------------------------+
|                                    React 18 Frontend                                  |
|   - Vite Build Tooling, React Router v6, Plain CSS (Design System Variables)          |
|   - Fetch API Client (api.js with Cookie Credentials)                                 |
|   - AuthContext + useIdleTimer (15-min auto session timeout) + usePermission (RBAC)   |
|   - Pages: Landing, Login, Dashboard, Projects, Workforce, Tasks, Attendance,        |
|            Inventory, Equipment, Maintenance, Safety, Incidents, Reports, Users,      |
|            Role Matrix, Account Requests, Audit Trail                                 |
+---------------------------------------------------------------------------------------+
                                           |
                              HTTP / REST (/api/*) & SPA Routing
                                           |
+---------------------------------------------------------------------------------------+
|                               Spring Boot 3.3.4 Backend                               |
|   - Web MVC + Spring Security (Session Cookie JSESSIONID, HttpOnly, SameSite=Strict)  |
|   - Method-level Server RBAC: @PreAuthorize backed by database permissions            |
|   - Password Security: BCryptPasswordEncoder (cost 10)                                |
|   - Lockout & Audit: 5 failed attempts = 15m lockout; append-only audit trail         |
|   - REST Controllers -> Services -> Repositories -> Spring JdbcTemplate               |
|   - Transaction Management (@Transactional rollback on stock depletion)              |
|   - OpenAPI / Swagger UI: /swagger-ui.html                                            |
+---------------------------------------------------------------------------------------+
                                           |
                               Spring JdbcTemplate (HikariCP)
                                           |
+---------------------------------------------------------------------------------------+
|                                MySQL 8 Relational DB                                  |
|   - 20 Relational Tables, Indexes, Constraints                                        |
|   - Flyway Versioned Migrations: V1__schema.sql & V2__seed.sql                        |
+---------------------------------------------------------------------------------------+
```

### Key Architectural Standards
1. **Strictly NO JPA / Hibernate**: Data access uses Spring `JdbcTemplate` with parameterized PreparedStatement binding (`?`) preserving clean DAO/Repository boundaries.
2. **Method-Level Server RBAC**: Every endpoint enforces permissions (e.g. `@PreAuthorize("hasAuthority('PROJECT_CREATE')")`).
3. **Session Cookie Security**: Uses `JSESSIONID` cookies with `changeSessionId()` session fixation protection, `HttpOnly`, and `SameSite=Strict`.
4. **Resilient SPA Serving**: Spring Boot serves the precompiled React single-page application at `/` and forwards client-side HTML5 routes to `/index.html`.

---

## 2. Seeded Demo Accounts & Credentials

The system features **8 enterprise roles** with exactly one demo user per role:

| Username | Password | Role | Responsibilities & Primary Modules |
| :--- | :--- | :--- | :--- |
| **`admin`** | `Admin@123` | Administrator | Full unrestricted access, User CRUD, Role Matrix, System Audit Trail |
| **`pm_sarah`** | `Pm@12345` | Project Manager | Project timelines, Budgets, Workforce allocation, Tasks, Project Reports |
| **`site_eng`** | `Eng@12345` | Site Engineer | Site tasks updates, Daily attendance recording, Material issue requests |
| **`safety_dan`** | `Safe@12345` | Safety Officer | Safety inspections (5-factor score), Violations, Incident investigation |
| **`inv_maria`** | `Inv@12345` | Inventory Manager | Material catalog, Reorder thresholds, Receipts & issues, Stock alerts |
| **`equip_sam`** | `Equip@12345` | Equipment Manager | Machinery fleet tracking, Preventative maintenance scheduling |
| **`contractor_apex`** | `Cont@12345` | Contractor | Subcontractor workforce overview, Assigned subcontractor tasks |
| **`worker_bill`** | `Worker@12345` | Worker | My assigned tasks, Personal monthly attendance %, Safety issue reporting |

*All passwords meet strict enterprise security standards (hashed using BCrypt with cost factor 10).*

---

## 3. Database Setup

### Option 1: Using Docker (Recommended for instant setup)
Start a MySQL 8 container with volume and health check:
```bash
docker compose up -d
```
The database will be automatically available at `localhost:3306` with database `csims_db`, user `root`, and password `Aryan@2007`. Flyway will run schema migrations on backend launch.

### Option 2: Using Local MySQL Server
Ensure MySQL 8.x is installed and running locally on port 3306.
Create the database in MySQL:
```sql
CREATE DATABASE IF NOT EXISTS csims_db;
```
If your MySQL credentials differ, set environment variables or edit `backend/src/main/resources/application.properties`:
- `DB_URL` (default: `jdbc:mysql://localhost:3306/csims_db`)
- `DB_USER` (default: `root`)
- `DB_PASSWORD` (default: `Aryan@2007`)
- `SERVER_PORT` (default: `8080`)

---

## 4. How to Run the Application

### Option A: Development Mode (Vite Dev Server + Spring Boot API)
Run the backend and frontend concurrently for hot module replacement (HMR).

**Terminal 1 — Backend:**
```bash
cd backend
mvn spring-boot:run
```
Backend API will be available at: **http://localhost:8080**

**Terminal 2 — Frontend:**
```bash
cd frontend
npm install
npm run dev
```
Frontend UI will be live at: **http://localhost:5173**  
*(Vite proxies all `/api` calls directly to `http://localhost:8080`)*

---

### Option B: Single-Command Production Deployment (Self-Contained JAR)
Build the frontend, bundle static assets into the Spring Boot JAR, and run everything on a single port:

**Step 1: Build Frontend Assets**
```bash
cd frontend
npm install
npm run build
```
*(Build output compiles into `backend/src/main/resources/static/`)*

**Step 2: Package Backend JAR**
```bash
cd ../backend
mvn clean package -DskipTests
```

**Step 3: Run Single JAR**
```bash
java -jar target/csims.jar
```
Open your browser at: **http://localhost:8080**  
The full application (React UI + REST API + Static Assets) will be served directly at `http://localhost:8080`.

---

## 5. API Documentation & OpenAPI Swagger

When the backend is running, explore interactive API documentation with Swagger UI:
- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## 6. Automated Integration Tests

Run the complete JUnit 5 + Spring Boot Test suite:
```bash
cd backend
mvn test
```
The test suite covers:
1. **`AuthIntegrationTest`**: Unauthenticated 401 challenge, successful credential authentication, invalid password rejection, and session validation.
2. **`RbacIntegrationTest`**: Method security `@PreAuthorize` verification, allowing permitted actions while returning 403 Forbidden for unauthorized roles.
3. **`InventoryTransactionTest`**: Atomic transaction validation, verifying automatic `@Transactional` rollback when requested issue quantity exceeds available stock.

---

## 7. Core Modules & Features

1. **Authentication & Session Security**:
   - Generic "Invalid username or password" messages to prevent account enumeration.
   - 5 consecutive failed attempts locks user account for 15 minutes.
   - Forced password change on first login (`must_change_password`).
   - 15-minute idle inactivity auto-logout (`useIdleTimer`).
   - Public account access requests and password reset workflows.
2. **User & Role Matrix Management**:
   - Full CRUD for users with instant status toggle (Active/Inactive).
   - Dynamic permission matrix editor for all 8 enterprise roles across 46 permissions.
   - Approval/rejection review workflow for registration requests.
   - Comprehensive backend and database architectural guide in `docs/BACKEND_AND_DATABASE_GUIDE.md`.
3. **Core Construction Operations**:
   - **Projects**: Budgets, milestone dates, progress tracking, and status transitions.
   - **Workforce**: Tradesmen directory, contractor assignments, contact information.
   - **Tasks**: Priority breakdown, worker assignments, due dates, completion percentages.
   - **Attendance**: Daily site check-in, presence recording, supervisory remarks.
4. **Inventory & Machinery Management**:
   - **Materials**: Reorder thresholds, low stock alerts, atomic receipts and project issues.
   - **Equipment**: Heavy machinery inventory, operational statuses.
   - **Maintenance**: Preventative service logging, automatic next service due calculation.
5. **Safety Compliance & Incidents**:
   - **Inspections**: 5-category scoring checklist (PPE, Worker Safety, Equipment, Housekeeping, Emergency). Automatic alert when score < 70%.
   - **Violations**: Hazard tracking with severity ratings (Critical, Major, Medium, Minor) and corrective action resolution.
   - **Incidents**: Accident reporting (Fatality, Injury, Property Damage, Near Miss) with emergency broadcast alerts and root cause investigation workflows.
6. **Intelligence & Reports Engine**:
   - Real-time tabular reports across all operational modules.
   - Live data preview tables with record counts.
   - One-click RFC-4180 compliant CSV exports with proper escaping of quotes, commas, and newlines.
7. **Immutable Audit Trail**:
   - Append-only compliance log capturing user ID, username, IP/session, action, module, entity ID, previous/new values, timestamp, and status.
