# CSIMS Backend & Database Guide (For Beginners)

> **Welcome!** If you are a 2nd-year engineering student who knows basic Java (classes, methods, loops) and simple SQL (`SELECT * FROM table`), this guide is written especially for you. We will break down how real-world backend applications work using real code from this project.

---

## 1. Big Picture: How a Web Request Travels

When you click a button in the browser (like "Issue 50 bags of cement"), your computer doesn't talk directly to the database. Instead, the request travels across layers like a relay race:

```
[ Browser / React UI ]
        |  HTTP POST /api/inventory/issue { materialId: 1, quantity: 50 }
        v
[ 1. Controller ]   --> Translates HTTP JSON into Java objects. Checks URL & permissions.
        |
        v
[ 2. Service ]      --> Business logic rules! (Is stock available? Deduct? Save audit log).
        |
        v
[ 3. Repository ]   --> SQL queries live here (DAO layer). Prepares SQL statements.
        |
        v
[ 4. JdbcTemplate ] --> Spring's helper that runs queries safely using JDBC driver.
        |
        v
[ 5. MySQL DB ]     --> Executes the query, updates disk storage, commits transaction.
```

### End-to-End Trace: "Issue Material from Inventory"

Let's trace what actually happens when you issue material on the site:

1. **Browser**: Calls `POST /api/inventory/issue` with body:
   ```json
   {
     "materialId": 1,
     "quantity": 50,
     "projectId": 1,
     "referenceNo": "ISSUE-101",
     "remarks": "Cement for slab casting"
   }
   ```
2. **Controller** (`MaterialController.java`):
   ```java
   @PostMapping("/issue")
   @PreAuthorize("hasAuthority('INVENTORY_ISSUE')")
   public ResponseEntity<ApiResponse<Void>> issueMaterial(@RequestBody StockMovementDto req,
                                                           @AuthenticationPrincipal CustomUserDetails user) {
       inventoryService.recordIssue(req.getMaterialId(), req.getQuantity(), req.getProjectId(),
                                    req.getReferenceNo(), req.getRemarks(), user.getId(), user.getUsername());
       return ResponseEntity.ok(ApiResponse.ok("Material issued successfully", null));
   }
   ```
   - Checks if the logged-in user has permission `INVENTORY_ISSUE`.
   - Passes the parameters to `InventoryService`.

3. **Service** (`InventoryService.java`):
   ```java
   @Transactional
   public void recordIssue(Integer materialId, BigDecimal quantity, Integer projectId,
                           String referenceNo, String remarks, Long operatorId, String operatorUsername) {
       Material m = getMaterialById(materialId);
       if (m.getQuantityOnHand().compareTo(quantity) < 0) {
           throw new ApiException("Insufficient stock for " + m.getName(), HttpStatus.BAD_REQUEST);
       }
       materialRepository.adjustStock(materialId, quantity.negate());
       // ... creates transaction record and audit log ...
   }
   ```
   - Enforces business logic: If you ask for 50 bags but only 20 are in stock, it immediately stops and rejects the request.

4. **Repository** (`MaterialRepository.java`):
   ```java
   public void adjustStock(Integer id, BigDecimal delta) {
       String sql = "UPDATE materials SET quantity_on_hand = quantity_on_hand + ? WHERE material_id = ?";
       jdbcTemplate.update(sql, delta, id);
   }
   ```
   - Sends the parameter `delta = -50` and `materialId = 1` into MySQL safely.

5. **MySQL Database**:
   - Executes `UPDATE materials SET quantity_on_hand = quantity_on_hand + (-50) WHERE material_id = 1;`
   - Returns the updated rows to Spring. The browser receives a neat JSON response:
     ```json
     { "success": true, "message": "Material issued successfully", "data": null }
     ```

---

## 2. Java Backend Basics Used in CSIMS

Think of a backend project like an organized office department:

| Concept | What it is (Analogy) | Where it is in this project |
| :--- | :--- | :--- |
| **Entity / Model** | The Java representation of a database row (data holder with getters/setters). | `com.csims.model.Material`, `Project`, `Task` |
| **DTO (Data Transfer Object)** | Custom object designed purely to receive data from or send data to the frontend. | `com.csims.dto.LoginRequest`, `StockMovementDto` |
| **Controller** | The receptionist who listens for requests at the door (URLs) and replies with JSON. | `com.csims.controller.MaterialController` |
| **Service** | The manager who makes decisions, verifies business rules, and starts transactions. | `com.csims.service.InventoryService` |
| **Repository (DAO)** | The filing clerk who knows the exact SQL statements to read/write records. | `com.csims.repository.MaterialRepository` |

### Key Spring Annotations Explained

1. **`@RestController`**:
   - Tells Spring: "This class answers HTTP requests (GET, POST, PUT, DELETE) and automatically serializes return values into JSON format."
   - *Example in `AuthController.java`*:
     ```java
     @RestController
     @RequestMapping("/api/auth")
     public class AuthController { ... }
     ```

2. **`@Service`**:
   - Marks a class as business logic layer. Spring automatically manages its lifecycle (Dependency Injection).
   - *Example in `ProjectService.java`*:
     ```java
     @Service
     public class ProjectService { ... }
     ```

3. **`@Transactional`**:
   - Wraps methods in an atomic database transaction. If any error or exception occurs halfway through, every change made inside the method is automatically **rolled back**!
   - *Example in `InventoryService.java`*:
     ```java
     @Transactional
     public void recordIssue(...) { ... }
     ```

4. **`@PreAuthorize`**:
   - Server-side guard. Blocks unauthorized users before the code runs and returns `403 Forbidden`.
   - *Example in `ProjectController.java`*:
     ```java
     @PreAuthorize("hasAuthority('PROJECT_CREATE')")
     ```

5. **`@Valid` / `@RequestBody`**:
   - `@RequestBody` reads the incoming JSON payload into a Java object.
   - `@Valid` triggers validation constraints (like `@NotBlank`, `@Size(min=8)`).

### Global Exception Handler & Standard JSON Envelope

Instead of letting errors crash the server or output messy stack traces, `GlobalExceptionHandler.java` catches any exception and returns a consistent response envelope (`ApiResponse.java`):

```java
// Standard Response Envelope
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private String error;
}
```

When an error happens:
```json
{
  "success": false,
  "message": "Insufficient stock for Cement Portland. Available: 20, Requested: 50",
  "data": null,
  "error": "Insufficient stock for Cement Portland. Available: 20, Requested: 50"
}
```

---

## 3. JDBC Explained From Scratch

### What is JDBC?
**JDBC (Java Database Connectivity)** is the standard Java API that allows Java programs to talk to relational databases like MySQL.
At the raw Java level, JDBC requires three core objects:
1. **`Connection`**: The open network socket pipe between Java and MySQL.
2. **`PreparedStatement`**: The precompiled SQL template that accepts typed parameters (`?`).
3. **`ResultSet`**: The cursor pointing to rows returned by a `SELECT` query.

### The Danger: SQL String Concatenation & SQL Injection

Imagine writing SQL by gluing strings together:
```java
// DANGEROUS! NEVER DO THIS:
String sql = "SELECT * FROM users WHERE username = '" + userInput + "' AND password = '" + pass + "'";
```
If an attacker types `' OR '1'='1` into the username box, the query becomes:
```sql
SELECT * FROM users WHERE username = '' OR '1'='1' AND password = '...'
```
The condition `'1'='1'` is always true, so the database logs the attacker in as the first user (often Admin) without any password!

### How `PreparedStatement` Stops SQL Injection

With `PreparedStatement`, you put question marks `?` as placeholders:
```java
String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
PreparedStatement stmt = conn.prepareStatement(sql);
stmt.setString(1, userInput);
stmt.setString(2, pass);
```
The database compiles the SQL structure **first**. The parameter is treated strictly as literal data, never as executable code. Even if `userInput` contains `' OR '1'='1`, MySQL simply searches for a user whose literal username is that exact string.

### Connection Pooling with HikariCP

Opening a database connection takes time (TCP handshake, SSL exchange, credential authentication ~50-100ms). If 500 users visit your site simultaneously and each opens a new connection, MySQL runs out of memory.

**HikariCP** maintains a pool of pre-opened connections in memory (e.g. 10 connections). When a request arrives, it borrows an existing connection, executes the query, and immediately returns the connection back to the pool in less than 1ms.

### Raw JDBC vs Spring `JdbcTemplate`

In plain old JDBC, you had to write 20 lines of repetitive boilerplate (`try-with-resources`, `rs.next()`, `catch SQLException`). Spring's `JdbcTemplate` cuts this down to 3 lines while keeping pure JDBC performance:

#### Old Plain-JDBC Style:
```java
List<Material> list = new ArrayList<>();
try (Connection conn = dataSource.getConnection();
     PreparedStatement ps = conn.prepareStatement("SELECT * FROM materials WHERE category = ?")) {
    ps.setString(1, "Structural");
    try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
            Material m = new Material();
            m.setId(rs.getInt("material_id"));
            m.setName(rs.getString("name"));
            list.add(m);
        }
    }
} catch (SQLException e) {
    throw new RuntimeException(e);
}
```

#### Modern `JdbcTemplate` Style (from `MaterialRepository.java`):
```java
String sql = "SELECT * FROM materials WHERE category = ?";
return jdbcTemplate.query(sql, (rs, rowNum) -> {
    Material m = new Material();
    m.setId(rs.getInt("material_id"));
    m.setName(rs.getString("name"));
    return m;
}, "Structural");
```
`RowMapper` is a functional interface: for each row in the `ResultSet`, you tell Java how to convert columns into your object.

---

## 4. Transactions (ACID)

A **Transaction** is a group of database actions that must succeed together or fail together as one single unit.

### ACID in Simple Words

- **Atomicity (All or Nothing)**: If you update stock, insert a transaction log, and write an audit record, either ALL 3 happen or NONE happen.
- **Consistency**: Database rules (foreign keys, `quantity >= 0`, NOT NULL) are never violated.
- **Isolation**: Two people buying the last item at the exact same millisecond won't corrupt the count.
- **Durability**: Once committed, data is written to disk and survives server crashes or power cuts.

### Real Walkthrough: `recordIssue` in `InventoryService.java`

Look at what happens during an issue transaction:

```java
@Transactional
public void recordIssue(Integer materialId, BigDecimal quantity, Integer projectId, ...) {
    // Step 1: Check existing stock
    Material m = getMaterialById(materialId);

    // Step 2: Validate
    if (m.getQuantityOnHand().compareTo(quantity) < 0) {
        throw new ApiException("Insufficient stock...", HttpStatus.BAD_REQUEST);
        // THROWING AN EXCEPTION ROLLS BACK ANY PREVIOUS CHANGES AUTOMATICALLY!
    }

    // Step 3: Deduct stock from materials table
    materialRepository.adjustStock(materialId, quantity.negate());

    // Step 4: Record history in inventory_transactions table
    materialRepository.recordTransaction(tx);

    // Step 5: Append to audit_logs table
    auditService.log(...);

    // Method ends successfully -> Spring commits the transaction to MySQL!
}
```

---

## 5. Database Design & Entity-Relationship (ER) Overview

Here is how the 20 tables in `csims_db` connect with each other:

```
  [roles] 1-------* [users] 1-------* [audit_logs]
     |                 |
     | *               | 1
 [role_permissions]    *
     |             [account_requests]
     | *
[permissions]

  [contractors] 1-------* [workers] 1-------* [attendance]
                             |                       |
                             *                       *
 [projects] 1----------------* [project_workers]    |
     |      1----------------------------------------+
     |
     +--- 1-------* [tasks]
     +--- 1-------* [safety_inspections]
     +--- 1-------* [safety_violations]
     +--- 1-------* [incidents]
     +--- 1-------* [inventory_transactions] *-------1 [materials]

  [equipment] 1-------* [maintenance_records]
  [users] 1-----------* [notifications]
```

### Table Summary & Design Reasons

| Table Name | Primary Key | Foreign Keys | Key Constraints | Purpose & Index Reason |
| :--- | :--- | :--- | :--- | :--- |
| `roles` | `role_id` | None | `UNIQUE(role_name)` | Defines user authority level. |
| `permissions` | `permission_id` | None | `UNIQUE(permission_code)` | Granular actions like `PROJECT_CREATE`. |
| `role_permissions` | Composite `(role_id, permission_id)` | `role_id`, `permission_id` | Cascade delete | Many-to-many lookup matrix for RBAC. |
| `users` | `user_id` | `role_id` | `UNIQUE(username)`, `UNIQUE(email)` | System user accounts; indexes on username & email for fast login lookup. |
| `account_requests`| `request_id` | `requested_role_id`, `reviewed_by` | `status IN ('PENDING', ...)` | Self-service registration & password reset queue. |
| `projects` | `project_id` | None | `budget >= 0` | Construction sites and milestone progress. |
| `contractors` | `contractor_id` | None | `name NOT NULL` | Third-party subcontract companies. |
| `workers` | `worker_id` | `contractor_id` | `status NOT NULL` | Tradesmen directory (carpenters, welders, electricians). |
| `project_workers` | Composite `(project_id, worker_id)` | `project_id`, `worker_id` | Cascade delete | Many-to-many allocation of workers to sites. |
| `tasks` | `task_id` | `project_id`, `assigned_worker_id` | `priority`, `status` | Work breakdown tasks. Index on `(project_id, status)` for fast board queries. |
| `attendance` | `attendance_id`| `worker_id`, `project_id`, `recorded_by` | `UNIQUE(worker_id, project_id, attendance_date)` | Prevents duplicate daily check-ins for the same worker. |
| `materials` | `material_id` | None | `UNIQUE(material_code)` | Catalog with on-hand stock and minimum reorder limits. |
| `inventory_transactions` | `transaction_id` | `material_id`, `project_id`, `performed_by` | `transaction_type IN ('RECEIPT', 'ISSUE')` | Immutable ledger of every stock movement. |
| `equipment` | `equipment_id` | None | `UNIQUE(equipment_code)` | Heavy machinery (Cranes, Excavators, Concrete Mixers). |
| `maintenance_records` | `maintenance_id` | `equipment_id`, `performed_by` | `status IN ('Scheduled', 'Completed')` | Preventative service schedule and cost history. |
| `safety_inspections` | `inspection_id` | `project_id`, `inspector_id` | `overall_score DECIMAL(5,2)` | 5-part checklist audits (PPE, hazards, equipment). |
| `safety_violations` | `violation_id` | `project_id`, `worker_id`, `reported_by` | `severity IN ('CRITICAL', ...)` | Non-compliance hazard tracking. |
| `incidents` | `incident_id` | `project_id`, `reported_by`, `investigator_id` | `severity IN ('FATALITY', ...)` | OSHA accident logs and corrective actions. |
| `notifications` | `notification_id` | `user_id` | `read_status BOOLEAN` | Alerts dispatched to user inboxes. |
| `audit_logs` | `audit_id` | `user_id` | Append-only (no update/delete) | Security compliance audit trail with before/after state. |

---

## 6. SQL Used in the Project

Here are the exact SQL commands used across repositories and what concepts they teach:

### 1. `SELECT` with `WHERE` and `ORDER BY`
- **File**: `TaskRepository.java`
- **Query**:
  ```sql
  SELECT * FROM tasks WHERE project_id = ? ORDER BY due_date ASC;
  ```
- **What it teaches**: Filters tasks for a specific project and sorts them so the earliest deadline appears first.

### 2. `INNER JOIN` vs `LEFT JOIN`
- **File**: `AttendanceRepository.java`
- **Query**:
  ```sql
  SELECT a.*, w.full_name AS worker_name, p.project_name, u.full_name AS recorder_name
  FROM attendance a
  JOIN workers w ON a.worker_id = w.worker_id
  JOIN projects p ON a.project_id = p.project_id
  LEFT JOIN users u ON a.recorded_by = u.user_id
  WHERE a.attendance_date = ?;
  ```
- **What it teaches**:
  - `JOIN workers` is an `INNER JOIN`: Every attendance row MUST have a valid worker.
  - `LEFT JOIN users`: If `recorded_by` is NULL (e.g. automated biometric sync), the attendance record still displays instead of being discarded.

### 3. `GROUP BY` and Aggregations (`COUNT`, `AVG`, `SUM`)
- **File**: `DashboardService.java`
- **Queries**:
  ```sql
  -- Count active projects
  SELECT COUNT(*) FROM projects WHERE status NOT IN ('Completed', 'Archived');

  -- Average inspection score
  SELECT ROUND(AVG(overall_score), 1) FROM safety_inspections;

  -- Total valuation of issued materials
  SELECT SUM(it.quantity * m.unit_price)
  FROM inventory_transactions it
  JOIN materials m ON it.material_id = m.material_id
  WHERE it.transaction_type = 'ISSUE';
  ```
- **What it teaches**: Summarizes thousands of individual rows into single business KPIs.

### 4. `INSERT` with `ON DUPLICATE KEY UPDATE` (Upsert)
- **File**: `AttendanceRepository.java`
- **Query**:
  ```sql
  INSERT INTO attendance (worker_id, project_id, attendance_date, status, remarks, recorded_by)
  VALUES (?, ?, ?, ?, ?, ?)
  ON DUPLICATE KEY UPDATE status = VALUES(status), remarks = VALUES(remarks), recorded_by = VALUES(recorded_by);
  ```
- **What it teaches**: If attendance for that worker on that date doesn't exist, insert it. If it already exists (hits the unique key), update it in place without throwing an error!

### 5. `LIMIT` and `OFFSET` (Pagination)
- **File**: `AuditLogRepository.java`
- **Query**:
  ```sql
  SELECT * FROM audit_logs ORDER BY audit_id DESC LIMIT ? OFFSET ?;
  ```
- **What it teaches**: Prevents downloading 100,000 logs into memory. `LIMIT 20 OFFSET 40` returns page 3 (records 41-60).

---

## 7. Security Explained Simply

### 1. Password Hashing with BCrypt
- **Why never plain text?** If an attacker steals a database backup, plain text reveals everyone's passwords.
- **Why BCrypt?** BCrypt includes a cryptographic **salt** (random bits mixed into every password) and a **cost factor** (makes brute-force guessing computationally slow).
- *Example*: Password `Admin@123` becomes `$2a$10$ojsb9VdkzLfWpqDDM93XoOnwZkxK/mQB47kzPv9B6U5QarJGGVAiS`.

### 2. Sessions & HttpOnly Cookies
When you log in:
1. Spring creates a session in memory with unique ID `JSESSIONID`.
2. Spring sets a cookie in your browser header: `Set-Cookie: JSESSIONID=...; HttpOnly; SameSite=Strict`.
3. `HttpOnly` means malicious JavaScript on a webpage cannot steal the cookie via `document.cookie`.
4. Subsequent fetch requests automatically send this cookie, identifying the user.

### 3. Account Lockout
- In `AuthService.java`, every failed login increments `users.failed_attempts`.
- Upon 5 consecutive failed attempts, `users.locked_until = NOW() + INTERVAL 15 MINUTE`.
- A 6th attempt is rejected before password checking, stopping dictionary attacks.

### 4. How RBAC Permissions are Loaded
When authenticating in `CustomUserDetailsService.java`:
```sql
SELECT p.permission_code
FROM permissions p
JOIN role_permissions rp ON p.permission_id = rp.permission_id
WHERE rp.role_id = ?;
```
These codes (e.g. `['PROJECT_CREATE', 'INVENTORY_ISSUE']`) become Spring `GrantedAuthority` objects checked by `@PreAuthorize`.

---

## 8. Flyway Database Migrations

Instead of manually running SQL scripts in MySQL Workbench every time code changes, **Flyway** manages database versioning automatically:

1. **`V1__schema.sql`**: Creates all tables, foreign keys, and indexes on first startup.
2. **`V2__seed.sql`**: Inserts initial roles, permissions, demo users with BCrypt hashes, and sample construction projects.
3. On application startup, Flyway checks a table called `flyway_schema_history`. If migration version 1 and 2 have already run, it skips them. If a new migration file (e.g. `V4__restore_roles.sql`) is added, Flyway runs only the new script in order!

---

## 9. Viva / Interview Questions & Model Answers

1. **Q: Why did you use Spring JdbcTemplate instead of Hibernate/JPA?**  
   *A:* JdbcTemplate provides full control over SQL queries, zero hidden ORM overhead (like N+1 queries or lazy loading exceptions), and directly demonstrates how JDBC PreparedStatement parameter binding and transaction management work.

2. **Q: How does PreparedStatement prevent SQL injection?**  
   *A:* It precompiles the SQL query template on the database engine. Parameters passed into `?` are sent separately as pure data values, so special characters cannot alter the SQL grammar.

3. **Q: What does `@Transactional` do under the hood?**  
   *A:* Spring creates an AOP proxy around the method that calls `connection.setAutoCommit(false)`. If the method completes normally, it executes `commit()`. If an unhandled `RuntimeException` is thrown, it executes `rollback()`.

4. **Q: What is connection pooling?**  
   *A:* Instead of creating and destroying expensive TCP database connections per HTTP request, connection pools like HikariCP keep a set of reusable connections alive in memory.

5. **Q: Explain the difference between `INNER JOIN` and `LEFT JOIN`.**  
   *A:* `INNER JOIN` returns only records that have matches in both tables. `LEFT JOIN` returns all rows from the left table, filling with `NULL` if no match exists in the right table.

6. **Q: What is a Unique Constraint and where did you use it?**  
   *A:* It ensures no two rows have the same value in specified columns. Used on `users(username)`, `materials(material_code)`, and compound `attendance(worker_id, project_id, attendance_date)`.

7. **Q: Why use BCrypt instead of SHA-256 or MD5 for passwords?**  
   *A:* MD5 and SHA-256 are fast calculation algorithms, making them vulnerable to GPU cracking. BCrypt is adaptive and includes built-in salts to resist rainbow table attacks.

8. **Q: What does `HttpOnly` mean on a session cookie?**  
   *A:* It instructs the browser that the cookie cannot be read via client-side JavaScript (`document.cookie`), protecting against Cross-Site Scripting (XSS) session theft.

9. **Q: What is the purpose of the DTO pattern?**  
   *A:* It decouples the internal database schema from the external API contract, preventing over-fetching and protecting internal entity fields from unauthorized mass-assignment.

10. **Q: How does the system enforce 15-minute idle timeouts?**  
    *A:* On the client side, `useIdleTimer` monitors keyboard/mouse events. On the server side, Spring Session invalidates the session after 15 minutes of inactivity (`server.servlet.session.timeout=15m`).

11. **Q: How is Database Normalization applied here?**  
    *A:* The schema is in 3rd Normal Form (3NF). Data is broken down into specific tables (`contractors`, `workers`, `projects`, `materials`) without transitive dependencies or repeating groups.

12. **Q: What is a Composite Primary Key?**  
    *A:* A primary key made of two or more columns together, such as `project_workers(project_id, worker_id)`.

13. **Q: How does method-level RBAC work in Spring Security?**  
    *A:* Annotating service or controller methods with `@PreAuthorize("hasAuthority('...')")` uses Spring Security expression evaluation to check user granted authorities before method execution.

14. **Q: What is an append-only audit trail?**  
    *A:* An `audit_logs` table where rows are strictly `INSERT`ed. No `UPDATE` or `DELETE` endpoints exist, guaranteeing an immutable historical record for compliance.

15. **Q: What is the purpose of Flyway?**  
    *A:* Flyway provides version-controlled database migrations so all team members and production servers can reliably reproduce the exact schema.

16. **Q: What happens if two workers mark attendance at the same time?**  
    *A:* Each record is an independent row. The `ON DUPLICATE KEY UPDATE` clause handles concurrent updates safely without crashing.

17. **Q: Why is pagination (`LIMIT` / `OFFSET`) important?**  
    *A:* Loading 100,000 records at once causes OutOfMemoryErrors and high network latency. Pagination transfers only what is visible on the current screen.

18. **Q: What is REST and why return `ResponseEntity`?**  
    *A:* REST uses standard HTTP verbs (`GET`, `POST`, `PUT`, `DELETE`). `ResponseEntity` allows setting exact HTTP status codes (like 200 OK, 401 Unauthorized, 403 Forbidden, 404 Not Found) alongside JSON data.

19. **Q: How are foreign keys with `ON DELETE SET NULL` vs `CASCADE` used?**  
    *A:* `ON DELETE CASCADE` deletes child tasks when a project is deleted. `ON DELETE SET NULL` keeps worker records even if their subcontractor company is deleted.

20. **Q: How is inventory stock deduction prevented from going negative?**  
    *A:* Business logic in `InventoryService` checks `quantityOnHand < requestedQuantity` before issuing. If violated, an exception triggers an atomic transaction rollback.

---

## 10. Glossary of Technical Terms

- **API (Application Programming Interface)**: The set of rules and URLs through which the frontend communicates with the backend.
- **BCrypt**: A key-derivation password-hashing function with adaptive salt and cost factors.
- **CORS (Cross-Origin Resource Sharing)**: Browser security header controlling whether a frontend on port 5173 can talk to a backend on port 8080.
- **DAO (Data Access Object)**: A design pattern that isolates data persistence code (SQL queries) from business logic.
- **DTO (Data Transfer Object)**: A lightweight object used solely to carry data between processes or over the network.
- **Flyway**: An open-source database migration tool that applies versioned SQL scripts.
- **HikariCP**: A high-performance, lightweight JDBC connection pool.
- **Idempotent**: An operation that produces the same result no matter how many times it is repeated.
- **JDBC (Java Database Connectivity)**: The core Java API that executes SQL against relational databases.
- **JdbcTemplate**: Spring's core JDBC helper class that eliminates boilerplate code and handles exception translation.
- **JSON (JavaScript Object Notation)**: Standard lightweight data-interchange text format used for API communication.
- **PreparedStatement**: A precompiled SQL statement that safely binds parameters to avoid SQL injection.
- **RBAC (Role-Based Access Control)**: Restricting system access to authorized users based on roles and permissions.
- **RowMapper**: A Spring JDBC interface used to map a single SQL `ResultSet` row to a Java domain object.
- **SPA (Single-Page Application)**: A web application (like React) that dynamically rewrites the current page rather than loading entire new HTML pages from the server.
