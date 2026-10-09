# Construction Site Intelligence & Safety Management System (CSIMS)

A comprehensive, runnable desktop application for intelligent construction site operations and safety compliance management, built with pure **Java 17 (Swing)** on the frontend and **MySQL 8 via JDBC** on the backend.

---

## 1. System Overview & Architecture

CSIMS is designed for construction general contractors, safety officers, site engineers, and project stakeholders to oversee site workforce, machinery, material inventory, tasks, safety inspections, and regulatory compliance.

### Architectural Principles
- **No Heavy Frameworks**: Pure Java SE (Swing UI, Standard Library, JDBC). No Spring, No Hibernate.
- **Layered Architecture**: `UI View / Dialogs` &rarr; `Service Layer` &rarr; `DAO Layer` &rarr; `Singleton DBConnection` &rarr; `MySQL 8`.
- **Security First**: 
  - PBKDF2 with HMAC-SHA256 password hashing (65,536 iterations, cryptographically random per-user salt, constant-time verification).
  - Rate limiting & lockout: 5 consecutive failed login attempts locks account for 15 minutes (`users.locked_until`).
  - Inactivity timeout: 15 minutes of user idle time across keyboard/mouse triggers automatic session termination.
  - Strict RBAC: 8 granular roles, 46 system permissions enforced at both UI (component filtering) and Service Layer (`PermissionChecker.require(...)`).
- **Data Integrity & Compliance**:
  - ACID transactions with auto-commit disabled and explicit rollback on stock depletion.
  - Append-only audit trail logging all state transitions, user logins, and entity changes with before/after diffs.
  - Immutable incident compliance tracking.

```
+-------------------------------------------------------------------------+
|                              Java Swing UI                              |
|  (LandingFrame, LoginFrame, MainFrame, 12 Modular Panels, Modal Dialogs)|
+-------------------------------------------------------------------------+
                                     |
                                     v
+-------------------------------------------------------------------------+
|                              Service Layer                              |
|  - RBAC Enforcement (PermissionChecker)                                |
|  - Input Validation (Validator)                                         |
|  - Audit Trail Generation (AuditService)                                |
|  - Real-time Notifications (NotificationService)                       |
|  - Business Workflows (State Machines & Inventory Transactions)         |
+-------------------------------------------------------------------------+
                                     |
                                     v
+-------------------------------------------------------------------------+
|                                DAO Layer                                |
|  - 19 Entity DAOs + JDBC Implementations                                |
|  - Parameterized PreparedStatement Everywhere (No String Concatenation) |
+-------------------------------------------------------------------------+
                                     |
                                     v
+-------------------------------------------------------------------------+
|                    Database Utility (DBConnection)                      |
|  - Thread-Safe Singleton with db.properties connection pooling check    |
+-------------------------------------------------------------------------+
                                     |
                                     v
+-------------------------------------------------------------------------+
|                         MySQL 8 Relational DB                           |
|  - 20 Relational Tables, Constraints, Indexes, Foreign Keys             |
+-------------------------------------------------------------------------+
```

---

## 2. Seeded Demo Accounts & Role Matrix

The system comes pre-configured in [seed.sql](file:///c:/Users/Aryan/Desktop/construction-site-intelligence/seed.sql) with 8 roles, 46 granular permissions, and 9 test accounts.

| Username | Password | Role | Description & Primary Access |
| :--- | :--- | :--- | :--- |
| **`admin`** | `Admin@1234` | Administrator | Full unrestricted system access, User CRUD, Role-Permission Matrix, Audit Trail viewer. |
| **`pm_sarah`** | `Pm@12345` | Project Manager | Project CRUD, Budget tracking, Task assignments, Milestone progress, Workforce allocation. |
| **`site_eng`** | `Eng@12345` | Site Engineer | Task workflow updates, Attendance logging & corrections, Equipment assignment, Daily logs. |
| **`safety_dan`** | `Safe@12345` | Safety Officer | Safety Inspections, Violation logging & resolution, Incident investigations, OSHA metrics. |
| **`inv_maria`** | `Inv@12345` | Inventory Manager | Material stock tracking, Issue/Receipt transactions, Low-stock alerts, Reorder levels. |
| **`equip_sam`** | `Equip@12345` | Equipment Manager | Heavy machinery fleet, Equipment checkout, Scheduled maintenance workflows. |
| **`contractor_apex`** | `Cont@12345` | Contractor | Subcontractor workforce overview, Assigned subcontract tasks. |
| **`worker_bill`** | `Worker@12345` | Worker | Personal assigned task execution, Personal monthly attendance log. |
| **`reset_test`** | `Reset@12345` | Site Engineer | Special test account with `must_change_password = true` to verify forced password change. |

*Note: All passwords meet the security policy (8+ characters, letters, and numbers).*

---

## 3. Database Setup Instructions

### Prerequisites
- MySQL 8.0+ server installed and running.
- MySQL client or command-line utility.

### Step 1: Initialize Database & Schema
Open a terminal or MySQL workbench and execute the schema script:
```sql
mysql -u root -p < schema.sql
```
*(Alternatively, copy and run the contents of [schema.sql](file:///c:/Users/Aryan/Desktop/construction-site-intelligence/schema.sql) in your MySQL console).*

### Step 2: Seed Initial Roles, Permissions & Demo Data
Execute the seed script:
```sql
mysql -u root -p < seed.sql
```
*(Runs [seed.sql](file:///c:/Users/Aryan/Desktop/construction-site-intelligence/seed.sql) which populates roles, permissions, 9 hashed users with salts, projects, inventory materials, equipment, and audit logs).*

### Step 3: Configure Database Connection
Edit [src/main/resources/db.properties](file:///c:/Users/Aryan/Desktop/construction-site-intelligence/src/main/resources/db.properties):
```properties
db.url=jdbc:mysql://localhost:3306/construction_site_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.user=root
db.password=your_mysql_password
```

---

## 4. Build & Execution

### Prerequisites
- **Java 17+** (JDK 17 LTS recommended)
- **Maven 3.8+**

### 1. Compile & Run Automated Unit Tests
```bash
mvn test
```
*Executes all 19 unit tests covering password security, lockout rate limiting, RBAC enforcement, task state machine, inventory stock deduction rules, validation, and CSV export.*

### 2. Build Fat JAR (Executable with All Dependencies)
```bash
mvn clean package
```
This produces the standalone JAR file:
```
target/construction-site-intelligence-1.0.0-jar-with-dependencies.jar
```

### 3. Launch the Application
```bash
java -jar target/construction-site-intelligence-1.0.0-jar-with-dependencies.jar
```

---

## 5. System Features & Module Walkthrough

### 1. Landing Screen & Health Check
- Displays application banner and description.
- Performs an instantaneous live database ping to MySQL on startup.
- If MySQL is offline, the **Login** button is gracefully disabled with a clear diagnostic notice.
- Includes **Request Access** dialog for new user registrations without requiring pre-existing credentials.

### 2. Authentication & Account Security
- **PBKDF2 Password Hashing**: Passwords stored as 256-bit PBKDF2 hashes using HMAC-SHA256 with individual 128-bit cryptographic salts.
- **Account Lockout**: 5 failed login attempts locks the account for 15 minutes, with remaining lockout minutes rendered in the UI.
- **Forced Password Change**: Users flagged with `must_change_password` (e.g. `reset_test`) are blocked from proceeding until a compliant new password is set.
- **Remember Username**: Persists username via `java.util.prefs.Preferences`.
- **Idle Timeout**: Automatically monitors global mouse and keyboard events; logs out the user after 15 minutes of inactivity.

### 3. Role-Based Access Control (RBAC)
- Sidebar navigation dynamically filters module buttons based on the user's role and granted permission codes.
- Administrator has access to the full **User Management** panel, enabling:
  - User creation, deactivation, and manual unlocking.
  - Interactive **Role-Permissions Matrix** to grant or revoke specific module permissions.
  - Review and approval/rejection of self-service account requests.

### 4. Projects & Milestones
- Complete CRUD management for construction projects.
- Tracks project code, name, client, location, budget, start/end dates, status, and completion progress.

### 5. Workforce & Subcontractor Directory
- Worker directory filtered by trade (Carpentry, Electrical, Masonry, Plumbing, etc.).
- Subcontractor tracking and association.
- Project assignment modal.

### 6. Tasks Workflow State Machine
- Strict 4-stage task lifecycle:
  $$\text{Created} \longrightarrow \text{Assigned} \longrightarrow \text{In Progress} \longrightarrow \text{Completed}$$
- Enforces worker assignment before task transition to "In Progress".
- Overdue task identification and priority categorization.

### 7. Worker Attendance & Audit Corrections
- Daily clock-in/clock-out tracking with overtime calculation.
- Attendance correction dialog with required justification note and automatic before/after audit logging.

### 8. Material Inventory & ACID Transactions
- Real-time stock tracking with category, unit, unit price, and reorder levels.
- Stock issue transactions run with `setAutoCommit(false)`:
  - If requested quantity exceeds on-hand stock, the transaction throws a validation exception and rolls back immediately.
  - On success, stock is deducted, movement is logged, and low-stock notifications are dispatched if stock drops below threshold.

### 9. Equipment & Maintenance Lifecycle
- Fleet asset tracking (Excavators, Cranes, Bulldozers, Concrete Mixers, Generators).
- 4-stage maintenance lifecycle:
  $$\text{Due} \longrightarrow \text{Scheduled} \longrightarrow \text{Serviced} \longrightarrow \text{Completed}$$
- Completing a maintenance task automatically updates equipment service dates and resets equipment status to "Available".

### 10. Safety Inspections, Violations & Incidents
- Safety inspections with numerical scoring (0-100), inspector notes, and pass/fail rating.
- Violation logging with severity ratings (Low, Medium, High, Critical) and corrective action tracking.
- Immutable incident investigation workflow (`Reported` &rarr; `Under Investigation` &rarr; `Action Taken` &rarr; `Resolved`).

### 11. Role-Specific KPI Dashboards
- Tailored KPI metric cards dynamically customized to the active role:
  - **Administrator / PM**: Total Projects, Active Projects, Workforce Size, Overdue Tasks, Low Stock Items.
  - **Safety Officer**: Avg Safety Score, Open Violations, Critical Violations, Total Inspections, Total Incidents.
  - **Inventory Manager**: Material SKUs, Low-Stock Count, Inventory Valuation ($), Healthy Items.
  - **Worker**: My Assigned Tasks, In-Progress, Completed, Overdue Tasks.
- Live active tasks and recent system alerts tables.

### 12. Reports & RFC-4180 CSV Export
- Generates 7 distinct operational reports:
  1. Project Summary Report
  2. Workforce & Personnel Report
  3. Task Execution & Progress Report
  4. Inventory & Stock Valuation Report
  5. Equipment & Fleet Status Report
  6. Safety Violations & Incidents Report
  7. System Audit Trail Report
- Preview in table with a single-click **Export to CSV** file chooser.

### 13. System Audit Trail
- Strictly read-only and append-only (no update or delete capabilities).
- Captures User, Action, Module, Entity ID, Status, Timestamp, and before/after JSON diffs.
- Interactive **View Diffs / Details** dialog and CSV export.

---

## 6. Verification & Automated Test Summary

The automated test suite verifies all critical business logic and security requirements:

| Test Class | Coverage Area | Status |
| :--- | :--- | :---: |
| [`PasswordSecurityTest`](file:///c:/Users/Aryan/Desktop/construction-site-intelligence/src/test/java/com/csims/PasswordSecurityTest.java) | PBKDF2 hashing, unique salts, constant-time verification, policy enforcement | **PASSED** |
| [`AuthLockoutLogicTest`](file:///c:/Users/Aryan/Desktop/construction-site-intelligence/src/test/java/com/csims/AuthLockoutLogicTest.java) | 5 failed attempts lockout, 15-minute timer, administrator unlock | **PASSED** |
| [`RbacPermissionTest`](file:///c:/Users/Aryan/Desktop/construction-site-intelligence/src/test/java/com/csims/RbacPermissionTest.java) | Session permission caching, UnauthorizedException on denial, Admin bypass | **PASSED** |
| [`TaskWorkflowStateMachineTest`](file:///c:/Users/Aryan/Desktop/construction-site-intelligence/src/test/java/com/csims/TaskWorkflowStateMachineTest.java) | Created &rarr; Assigned &rarr; In Progress &rarr; Completed, invalid jumps rejected | **PASSED** |
| [`InventoryStockLogicTest`](file:///c:/Users/Aryan/Desktop/construction-site-intelligence/src/test/java/com/csims/InventoryStockLogicTest.java) | Rollback on insufficient stock, stock deduction, low-stock threshold | **PASSED** |
| [`ValidatorAndDateUtilTest`](file:///c:/Users/Aryan/Desktop/construction-site-intelligence/src/test/java/com/csims/ValidatorAndDateUtilTest.java) | Email, positive number, range validation, date formatting & parsing | **PASSED** |
| [`CsvExporterTest`](file:///c:/Users/Aryan/Desktop/construction-site-intelligence/src/test/java/com/csims/CsvExporterTest.java) | RFC-4180 CSV compliance, comma/quote/newline escaping, null safety | **PASSED** |

---

## 7. License & Compliance
This software was engineered as a robust, production-grade mini project demonstrating secure Java desktop software architecture, clean separation of concerns, and enterprise security patterns.
