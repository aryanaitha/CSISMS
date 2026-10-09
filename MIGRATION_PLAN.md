# Construction Site Intelligence & Safety Management System (CSIMS)
# Architecture Migration Plan: Java Swing to Modern Web Stack

## 1. Overview & Objectives
This document establishes the formal migration specification to transition CSIMS from a Java Swing desktop application into a modern, decoupled web architecture running on `localhost`:
- **Frontend**: React 18 + Vite (JavaScript), React Router v6, plain CSS (system font stack, neutral white/grey palette, minimal text-first design), Fetch API wrapper with cookie-based session and CSRF handling.
- **Backend**: Java 17 + Spring Boot 3.3.x, Spring Security (session-based authentication with `JSESSIONID`, BCrypt password hashing, method-level `@PreAuthorize` RBAC), Spring `JdbcTemplate` (NO JPA, NO Hibernate), HikariCP, Flyway migrations (`V1__schema.sql`, `V2__seed.sql`), and OpenAPI / Swagger UI.
- **Database**: MySQL 8 relational database (preserving existing schema, constraints, and demo data).
- **Packaging**: Dual execution modes (Dev mode with Vite proxy on `:5173` & Spring Boot on `:8080`, and Single-Jar production mode serving React static assets from Spring Boot on `:8080`).

---

## 2. Analysis of Existing Swing UI Components & Service Invocations

### 2.1 Frames
| Swing Frame | Description / Role | Underlying Service Invocations | Target React Route / Page |
| :--- | :--- | :--- | :--- |
| `LandingFrame` | Initial public screen. Shows DB connection health, module list, roles, and access request modal. | `DBConnection.isAvailable()`, `AuthService.submitRegistrationRequest()` | `/` (Public Landing Page) |
| `LoginFrame` | Public authentication screen. Collects username/email & password, remember username, forgot password, lockout timer. | `AuthService.login()`, `AuthService.submitPasswordResetRequest()`, `AuthService.getRemainingLockoutMinutes()` | `/login` (Login Page) |
| `MainFrame` | Post-login application shell. Role-filtered sidebar, top bar with user profile & account menu, card switching, idle session monitoring. | `AuthService.logout()`, `AuthService.changePassword()`, `SessionContext` | `/app/*` (App Shell / `Layout.jsx` with `<Outlet/>`) |

### 2.2 Modal Dialogs
| Swing Dialog | Invoking Module | Service Invocations | Target React Component |
| :--- | :--- | :--- | :--- |
| `RegisterRequestDialog` | Landing / Login | `AuthService.submitRegistrationRequest()` | Modal inside Landing / Login |
| `ForgotPasswordDialog` | Login | `AuthService.submitPasswordResetRequest()` | Modal inside Login |
| `ChangePasswordDialog` | Main Topbar / Forced Auth | `AuthService.changePassword()` | Modal in Header + `/change-password` page |
| `UserFormDialog` | User Management | `UserService.createUser()`, `UserService.updateUser()` | Modal in `/app/users` |
| `RolePermissionsDialog` | User Management | `UserService.getAllRoles()`, `UserService.getAllPermissions()`, `UserService.getRolePermissions()`, `UserService.updateRolePermissions()` | Modal in `/app/users/roles` |
| `AccountRequestsDialog` | User Management | `UserService.getPendingRequests()`, `UserService.approveRequest()`, `UserService.rejectRequest()` | Tab / Modal in `/app/users` |
| `ProjectFormDialog` | Projects | `ProjectService.createProject()`, `ProjectService.updateProject()`, `UserService.getAllUsers()` | Modal in `/app/projects` |
| `WorkerFormDialog` | Workforce | `WorkforceService.createWorker()`, `WorkforceService.updateWorker()`, `WorkforceService.getAllContractors()` | Modal in `/app/workforce` |
| `ContractorFormDialog`| Workforce | `WorkforceService.createContractor()`, `WorkforceService.updateContractor()` | Modal in `/app/workforce` |
| `AssignWorkerDialog` | Workforce / Projects | `WorkforceService.assignWorkerToProject()` | Modal in `/app/workforce` |
| `TaskFormDialog` | Tasks | `TaskService.createTask()`, `TaskService.updateTask()`, `ProjectService.getAllProjects()`, `WorkforceService.getAllWorkers()` | Modal in `/app/tasks` |
| `TaskStatusDialog` | Tasks | `TaskService.transitionStatus()`, `TaskService.assignWorker()` | Modal in `/app/tasks` |
| `AttendanceRecordDialog` | Attendance | `AttendanceService.recordAttendance()`, `WorkforceService.getAllWorkers()`, `ProjectService.getAllProjects()` | Modal in `/app/attendance` |
| `AttendanceCorrectDialog`| Attendance | `AttendanceService.correctAttendance()` | Modal in `/app/attendance` |
| `MaterialFormDialog` | Inventory | `InventoryService.createMaterial()`, `InventoryService.updateMaterial()` | Modal in `/app/inventory` |
| `StockTransactionDialog` | Inventory | `InventoryService.recordTransaction()`, `ProjectService.getAllProjects()` | Modal in `/app/inventory` |
| `InventoryHistoryDialog` | Inventory | `InventoryService.getTransactionHistory()` | Tab / Drawer in `/app/inventory` |
| `EquipmentFormDialog` | Equipment | `EquipmentService.createEquipment()`, `EquipmentService.updateEquipment()` | Modal in `/app/equipment` |
| `AssignEquipmentDialog` | Equipment | `EquipmentService.assignToProject()`, `ProjectService.getAllProjects()` | Modal in `/app/equipment` |
| `MaintenanceScheduleDialog` | Maintenance | `MaintenanceService.scheduleMaintenance()`, `EquipmentService.getAllEquipment()` | Modal in `/app/maintenance` |
| `MaintenanceCompleteDialog` | Maintenance | `MaintenanceService.completeMaintenance()` | Modal in `/app/maintenance` |
| `InspectionFormDialog` | Safety Inspections | `SafetyService.createInspection()`, `ProjectService.getAllProjects()` | Modal in `/app/safety/inspections` |
| `ViolationFormDialog` | Safety Violations | `SafetyService.createViolation()`, `ProjectService.getAllProjects()`, `WorkforceService.getAllWorkers()` | Modal in `/app/safety/violations` |
| `ViolationResolveDialog` | Safety Violations | `SafetyService.resolveViolation()` | Modal in `/app/safety/violations` |
| `IncidentReportDialog` | Incidents | `SafetyService.reportIncident()`, `ProjectService.getAllProjects()` | Modal in `/app/safety/incidents` |
| `IncidentInvestigateDialog`| Incidents | `SafetyService.transitionIncidentStatus()`, `UserService.getAllUsers()` | Modal in `/app/safety/incidents` |

### 2.3 Main Panels
| Swing Panel | Role / Permission | Service Invocations | Target React Page |
| :--- | :--- | :--- | :--- |
| `DashboardPanel` | All Users | Role-tailored metrics from `ProjectService`, `TaskService`, `SafetyService`, `InventoryService`, `EquipmentService`, `NotificationService` | `/app/dashboard` |
| `UserManagementPanel` | Admin (`USER_VIEW`) | `UserService.*`, `RoleDAO.*`, `AccountRequestDAO.*` | `/app/users` |
| `ProjectPanel` | `PROJECT_VIEW` | `ProjectService.getAllProjects()`, `searchProjects()`, `updateProgress()`, `archiveProject()`, `deleteProject()` | `/app/projects` |
| `WorkforcePanel` | `WORKFORCE_VIEW` | `WorkforceService.getAllWorkers()`, `searchWorkers()`, `getAllContractors()`, `deleteWorker()` | `/app/workforce` |
| `TaskPanel` | `TASK_VIEW` | `TaskService.getAllTasks()`, `searchTasks()`, `getOverdueTasks()`, `deleteTask()` | `/app/tasks` |
| `AttendancePanel` | `ATTENDANCE_VIEW` | `AttendanceService.getRecords()`, `getWorkerAttendanceSummary()` | `/app/attendance` |
| `InventoryPanel` | `INVENTORY_VIEW` | `InventoryService.getAllMaterials()`, `getLowStockMaterials()`, `searchMaterials()` | `/app/inventory` |
| `EquipmentPanel` | `EQUIPMENT_VIEW` | `EquipmentService.getAllEquipment()`, `searchEquipment()`, `deleteEquipment()` | `/app/equipment` |
| `MaintenancePanel` | `MAINTENANCE_VIEW`| `MaintenanceService.getAllMaintenance()`, `getDueMaintenance()`, `updateStatus()` | `/app/maintenance` |
| `SafetyInspectionPanel`| `SAFETY_INSPECTION_VIEW` | `SafetyService.getAllInspections()`, `searchInspections()` | `/app/safety/inspections` |
| `SafetyViolationPanel` | `SAFETY_VIOLATION_VIEW` | `SafetyService.getAllViolations()`, `searchViolations()` | `/app/safety/violations` |
| `IncidentPanel` | `INCIDENT_VIEW` | `SafetyService.getAllIncidents()`, `searchIncidents()` | `/app/safety/incidents` |
| `NotificationPanel` | All Users | `NotificationService.getMyNotifications()`, `markAsRead()`, `markAllAsRead()` | `/app/notifications` |
| `ReportPanel` | `REPORT_VIEW` | `ReportService.getAvailableReports()`, `generateReport()`, `exportReportToCsv()` | `/app/reports` |
| `AuditLogPanel` | Admin (`AUDIT_VIEW`)| `AuditService.searchAuditLogs()`, `getAllAuditLogs()` | `/app/audit-logs` |

---

## 3. REST API Endpoint Mapping & RBAC Permissions

All endpoints return a standardized envelope:
- **Success**: `{ "success": true, "data": ... }`
- **Error**: `{ "success": false, "error": "...", "code": "VALIDATION|AUTH|FORBIDDEN|NOT_FOUND|SERVER", "details": [...] }`

### 3.1 Authentication & Profile (`/api/auth`)
| Method | Endpoint | Required Permission | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/health` | Public | System & database health indicator (`{ "status": "UP", "database": "CONNECTED" }`) |
| `POST` | `/api/auth/login` | Public | Credentials verify, 5-failure 15m lockout, session creation, login audit |
| `POST` | `/api/auth/logout` | Authenticated | Invalidate session, clear session context, logout audit |
| `GET` | `/api/auth/me` | Authenticated | Retrieve current session user profile, role, and permission array |
| `POST` | `/api/auth/change-password` | Authenticated | Self password change with old-pass check and policy enforcement |
| `POST` | `/api/auth/register-request`| Public | Submit registration access request (`PENDING`) |
| `POST` | `/api/auth/forgot-password`| Public | Submit password reset request (`PASSWORD_RESET`) |

### 3.2 User & Role Management (`/api/users`, `/api/roles`, `/api/account-requests`)
| Method | Endpoint | Required Permission | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/users` | `USER_VIEW` | List users with search & role filter |
| `POST` | `/api/users` | `USER_CREATE` | Create new system user |
| `GET` | `/api/users/{id}` | `USER_VIEW` | Get user details |
| `PUT` | `/api/users/{id}` | `USER_UPDATE` | Update user profile, active status, role |
| `POST` | `/api/users/{id}/reset-password` | `USER_UPDATE` | Admin password reset with temporary password & `must_change_password` |
| `POST` | `/api/users/{id}/unlock` | `USER_UPDATE` | Unlock locked account & clear failed attempts |
| `GET` | `/api/roles` | `ROLE_VIEW` | List all system roles |
| `GET` | `/api/permissions` | `ROLE_VIEW` | List all system permissions |
| `GET` | `/api/roles/{id}/permissions` | `ROLE_VIEW` | Get assigned permissions for a role |
| `PUT` | `/api/roles/{id}/permissions` | `ROLE_MANAGE` | Update role permission mappings |
| `GET` | `/api/account-requests` | `ACCOUNT_REQUEST_REVIEW` | List pending / reviewed registration & reset requests |
| `POST` | `/api/account-requests/{id}/approve` | `ACCOUNT_REQUEST_REVIEW` | Approve request (creates user or resets password) |
| `POST` | `/api/account-requests/{id}/reject` | `ACCOUNT_REQUEST_REVIEW` | Reject request with recorded note |

### 3.3 Projects (`/api/projects`)
| Method | Endpoint | Required Permission | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/projects` | `PROJECT_VIEW` | List / search projects with status filter |
| `POST` | `/api/projects` | `PROJECT_CREATE` | Create new project |
| `GET` | `/api/projects/{id}` | `PROJECT_VIEW` | Get project details |
| `PUT` | `/api/projects/{id}` | `PROJECT_UPDATE` | Update project metadata |
| `PATCH` | `/api/projects/{id}/progress` | `PROJECT_UPDATE` | Update milestone progress percentage |
| `PATCH` | `/api/projects/{id}/archive` | `PROJECT_ARCHIVE` | Archive project |
| `DELETE` | `/api/projects/{id}` | `PROJECT_DELETE` | Delete project |

### 3.4 Workforce & Contractors (`/api/workers`, `/api/contractors`)
| Method | Endpoint | Required Permission | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/workers` | `WORKFORCE_VIEW` | List workers with trade / contractor filter |
| `POST` | `/api/workers` | `WORKFORCE_CREATE` | Register worker |
| `PUT` | `/api/workers/{id}` | `WORKFORCE_UPDATE` | Update worker profile |
| `DELETE` | `/api/workers/{id}` | `WORKFORCE_DELETE` | Remove worker |
| `POST` | `/api/workers/{id}/assign` | `WORKFORCE_ASSIGN` | Assign worker to project |
| `GET` | `/api/contractors` | `WORKFORCE_VIEW` | List contractor organizations |
| `POST` | `/api/contractors` | `WORKFORCE_CREATE` | Create contractor company |
| `PUT` | `/api/contractors/{id}` | `WORKFORCE_UPDATE` | Update contractor details |

### 3.5 Tasks (`/api/tasks`)
| Method | Endpoint | Required Permission | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/tasks` | `TASK_VIEW` | List tasks with filters (project, worker, status, priority) |
| `GET` | `/api/tasks/overdue` | `TASK_VIEW` | Get overdue tasks |
| `POST` | `/api/tasks` | `TASK_CREATE` | Create task |
| `PUT` | `/api/tasks/{id}` | `TASK_UPDATE` | Update task details |
| `PATCH` | `/api/tasks/{id}/status` | `TASK_UPDATE` | State machine transition (`Created -> Assigned -> In Progress -> Completed`) |
| `PATCH` | `/api/tasks/{id}/assign` | `TASK_ASSIGN` | Assign worker to task |
| `DELETE` | `/api/tasks/{id}` | `TASK_DELETE` | Delete task |

### 3.6 Attendance (`/api/attendance`)
| Method | Endpoint | Required Permission | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/attendance` | `ATTENDANCE_VIEW` | Filter attendance records by worker/project/date range |
| `POST` | `/api/attendance` | `ATTENDANCE_RECORD` | Daily clock-in / clock-out logging |
| `PUT` | `/api/attendance/{id}/correct`| `ATTENDANCE_CORRECT` | Supervisor correction with mandatory audit note |
| `GET` | `/api/attendance/summary` | `ATTENDANCE_VIEW` | Attendance percentage summary by project |

### 3.7 Materials & Inventory (`/api/materials`, `/api/inventory/transactions`)
| Method | Endpoint | Required Permission | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/materials` | `INVENTORY_VIEW` | List materials with category & search filter |
| `GET` | `/api/materials/low-stock` | `INVENTORY_VIEW` | Materials below reorder threshold |
| `POST` | `/api/materials` | `INVENTORY_CREATE` | Register material SKU |
| `PUT` | `/api/materials/{id}` | `INVENTORY_UPDATE` | Update material specifications |
| `POST` | `/api/inventory/transactions` | `INVENTORY_ISSUE` / `INVENTORY_RECEIVE` | Atomic stock movement transaction (rollback on insufficient stock) |
| `GET` | `/api/inventory/transactions` | `INVENTORY_VIEW` | List movement history |

### 3.8 Equipment & Maintenance (`/api/equipment`, `/api/maintenance`)
| Method | Endpoint | Required Permission | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/equipment` | `EQUIPMENT_VIEW` | List equipment with status & category filter |
| `POST` | `/api/equipment` | `EQUIPMENT_CREATE` | Register machinery asset |
| `PUT` | `/api/equipment/{id}` | `EQUIPMENT_UPDATE` | Update machinery details |
| `PATCH` | `/api/equipment/{id}/assign` | `EQUIPMENT_ASSIGN` | Assign equipment to project |
| `DELETE` | `/api/equipment/{id}` | `EQUIPMENT_DELETE` | Remove equipment |
| `GET` | `/api/maintenance` | `MAINTENANCE_VIEW` | List maintenance log entries |
| `POST` | `/api/maintenance` | `MAINTENANCE_SCHEDULE` | Schedule preventive servicing |
| `PATCH` | `/api/maintenance/{id}/status` | `MAINTENANCE_RECORD` | Lifecycle transition (`Due -> Scheduled -> Serviced -> Completed`) |

### 3.9 Safety Inspections, Violations & Incidents (`/api/inspections`, `/api/violations`, `/api/incidents`)
| Method | Endpoint | Required Permission | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/inspections` | `SAFETY_INSPECTION_VIEW` | List inspections |
| `POST` | `/api/inspections` | `SAFETY_INSPECTION_CREATE` | Record inspection with 0-100 score |
| `GET` | `/api/violations` | `SAFETY_VIOLATION_VIEW` | List safety violations |
| `POST` | `/api/violations` | `SAFETY_VIOLATION_CREATE` | Log violation with severity level |
| `PATCH` | `/api/violations/{id}/resolve`| `SAFETY_VIOLATION_UPDATE` | Resolve violation with corrective action note |
| `GET` | `/api/incidents` | `INCIDENT_VIEW` | List incidents |
| `POST` | `/api/incidents` | `INCIDENT_CREATE` | Report incident |
| `PATCH` | `/api/incidents/{id}/workflow`| `INCIDENT_UPDATE` | Transition status (`Reported -> Under Investigation -> Action Taken -> Resolved`) |

### 3.10 Dashboards, Notifications, Reports & Audit Trail
| Method | Endpoint | Required Permission | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/dashboard` | Authenticated | Live role-tailored KPI cards and summary tables |
| `GET` | `/api/notifications` | Authenticated | User notifications with unread filter |
| `GET` | `/api/notifications/unread-count` | Authenticated | Unread alerts badge count |
| `PATCH` | `/api/notifications/{id}/read`| Authenticated | Mark notification read |
| `POST` | `/api/notifications/mark-all-read` | Authenticated | Mark all notifications read |
| `GET` | `/api/reports` | `REPORT_VIEW` | List 7 available operational report types |
| `GET` | `/api/reports/{name}` | `REPORT_VIEW` | Generate report dataset preview |
| `GET` | `/api/reports/{name}/csv` | `REPORT_VIEW` | Download report as RFC-4180 CSV attachment |
| `GET` | `/api/audit-logs` | `AUDIT_VIEW` | Strictly read-only audit trail query with diffs |

---

## 4. Entity & Table Mapping

| Database Table | Model Entity POJO | Primary Key | Key Relationships / Indices |
| :--- | :--- | :--- | :--- |
| `roles` | `Role` | `role_id` | Role definitions (1..8) |
| `permissions` | `Permission` | `permission_id` | Permission codes (46 items in `MODULE_ACTION` format) |
| `role_permissions` | `RolePermission` | (`role_id`, `permission_id`) | Composite PK, M:N mapping |
| `users` | `User` | `user_id` | `role_id` &rarr; `roles`, unique username & email, locked_until, failed_attempts |
| `account_requests` | `AccountRequest` | `request_id` | `requested_role_id` &rarr; `roles`, `reviewed_by` &rarr; `users` |
| `projects` | `Project` | `project_id` | Project codes, budget, dates, status |
| `contractors` | `Contractor` | `contractor_id` | Subcontractor organizations |
| `workers` | `Worker` | `worker_id` | `contractor_id` &rarr; `contractors`, trades |
| `project_workers` | `ProjectWorker` | `id` | (`project_id`, `worker_id`) unique assignment |
| `tasks` | `TaskItem` | `task_id` | `project_id` &rarr; `projects`, `assigned_worker_id` &rarr; `workers` |
| `attendance` | `Attendance` | `attendance_id` | `worker_id` &rarr; `workers`, `project_id` &rarr; `projects`, date unique |
| `materials` | `Material` | `material_id` | Unique `material_code`, stock levels, unit price |
| `inventory_transactions` | `InventoryTransaction` | `transaction_id` | `material_id` &rarr; `materials`, `project_id` &rarr; `projects`, `created_by` &rarr; `users` |
| `equipment` | `Equipment` | `equipment_id` | Unique `equipment_code`, `project_id` &rarr; `projects`, maintenance dates |
| `maintenance` | `Maintenance` | `maintenance_id` | `equipment_id` &rarr; `equipment`, workflow status |
| `safety_inspections` | `SafetyInspection`| `inspection_id` | `project_id` &rarr; `projects`, `inspector_id` &rarr; `users`, score |
| `safety_violations` | `SafetyViolation` | `violation_id` | `project_id` &rarr; `projects`, `worker_id` &rarr; `workers`, severity |
| `incidents` | `Incident` | `incident_id` | `project_id` &rarr; `projects`, `investigator_id` &rarr; `users`, status |
| `notifications` | `Notification` | `notification_id` | `user_id` &rarr; `users`, type, is_read |
| `audit_logs` | `AuditLog` | `audit_id` | Append-only audit entries, before/after diffs |

---

## 5. Security & Architectural Migration Details

### 5.1 Password Hashing Transition (PBKDF2 &rarr; BCrypt)
- The legacy desktop app used custom PBKDF2 with HMAC-SHA256 and separate per-user salts.
- The new Spring Security backend utilizes Spring's standard `BCryptPasswordEncoder` (strength 10).
- The Flyway migration `V2__seed.sql` seeds the 9 demo users with valid BCrypt hashes (e.g., `Admin@123` &rarr; `$2a$10$...`, `Pm@12345` &rarr; `$2a$10$...`).

### 5.2 Spring Security & Session Management
- **Stateless API vs Session**: Standard Spring Security session management (`SecurityContextHolder` with `HttpSession`) storing the authenticated `UserDetails`.
- **Session Cookie**: `JSESSIONID` configured with `HttpOnly`, `SameSite=Strict`, and 15-minute server idle timeout (`server.servlet.session.timeout=15m`).
- **CSRF Token**: `CookieCsrfTokenRepository.withHttpOnlyFalse()` providing the standard `XSRF-TOKEN` cookie, sent back by the frontend as `X-XSRF-TOKEN` header on mutation requests.
- **Lockout Mechanism**: 5 consecutive failed login attempts locks `users.locked_until` for 15 minutes. Custom `AuthenticationFailureHandler` / `AuthService` handles counter increment, lockout notification, and audit.

### 5.3 Flyway Versioned Migrations
- `src/main/resources/db/migration/V1__schema.sql`: Contains the complete, clean DDL for all 20 tables with indexes, constraints, and checks.
- `src/main/resources/db/migration/V2__seed.sql`: Populates the 8 roles, 46 permissions, role-permission links, 9 demo accounts with BCrypt hashes, sample projects, workforce, inventory, and machinery.

### 5.4 Dual Execution Modes
1. **Development Mode**:
   - Backend on `http://localhost:8080`
   - Frontend Vite dev server on `http://localhost:5173` with Vite proxy forwarding `/api` to `http://localhost:8080`.
2. **Single Runnable JAR**:
   - Frontend built with `npm run build` outputting to `backend/src/main/resources/static` (or via `frontend-maven-plugin`).
   - Running `java -jar backend/target/csims.jar` serves the complete application at `http://localhost:8080`.

---

## 6. Phased Implementation Roadmap

- **Phase 1**: Analysis & scaffolding (Spring Boot 3 + Flyway + HikariCP + React 18 Vite setup + proxy verification).
- **Phase 2**: Authentication & Security (Spring Security session, BCrypt, Lockout, CSRF, AuthController, AuthContext, ProtectedRoute, Landing, Login, modals).
- **Phase 3**: App Shell & User/Role Management (Sidebar by permission, Topbar, Users, Roles Matrix, Account Requests).
- **Phase 4**: Projects, Workforce, Tasks & Attendance (State machines, attendance corrections, audits).
- **Phase 5**: Inventory, Equipment & Maintenance (Transactional stock issues, fleet tracking, maintenance workflows).
- **Phase 6**: Safety Inspections, Violations & Incidents (OSHA metrics, resolution workflows, immutable incident tracking).
- **Phase 7**: Dashboards, Notifications, Reports/CSV & Audit Log (Role KPI cards, polling alerts, RFC-4180 CSV export, read-only audit log).
- **Phase 8**: Verification, automated tests, Swing code cleanup, docker-compose, and polished README.
