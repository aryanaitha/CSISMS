# Role Consolidation Analysis & Decision (CSIMS)

> [!WARNING]
> **STATUS: REJECTED / SUPERSEDED**
> The proposal to consolidate roles to 4 was evaluated and rejected. The CSIMS project strictly maintains the original **8 enterprise roles**:
> 1. `Administrator`
> 2. `Project Manager`
> 3. `Site Engineer`
> 4. `Safety Officer`
> 5. `Inventory Manager`
> 6. `Equipment Manager`
> 7. `Contractor`
> 8. `Worker`
>
> All database mappings, Flyway migrations (`V4__restore_roles.sql`), automated integration tests, UI navigation, and security configurations enforce the 8-role segregation of duties model.

---
The original Java Swing architecture of CSIMS defined 8 distinct roles:
1. `Administrator`
2. `Project Manager`
3. `Site Engineer`
4. `Safety Officer`
5. `Inventory Manager`
6. `Equipment Manager`
7. `Contractor`
8. `Worker`

While having 8 roles is common in massive multi-tenant industrial enterprise applications, in a construction site intelligence mini-project it creates unnecessary friction:
- High role fragmentation: `Inventory Manager` only handled material stock; `Equipment Manager` only handled machinery service schedules. In reality, on-site logistics are coordinated by a single operations lead.
- Heavy overlap: `Site Engineer` and `Project Manager` shared task updates and worker assignments with nearly identical permission sets.
- Subcontractor redundancy: `Contractor` was essentially an external worker account with task view permissions.

---

## 2. Analysis of the Original 8 Roles & Permission Overlaps

| Original Role | Core Permissions | Overlap / Redundancy Identified |
| :--- | :--- | :--- |
| **Administrator** | Users, Roles, Requests, Audit, Reports, All | No overlap. Necessary for system governance and access control. |
| **Project Manager** | Projects (CRUD), Workforce (CRUD), Tasks (CRUD), Attendance (View) | Strong synergy with site execution. |
| **Site Engineer** | Tasks (Update), Attendance (Record/Correct), Safety View | 85% overlap with Project Manager field coordination. |
| **Safety Officer** | Inspections, Violations, Incidents (CRUD) | Field-level safety monitoring. |
| **Inventory Manager**| Inventory (View, Create, Receipt, Issue) | Warehouse & material movement. |
| **Equipment Manager**| Equipment (CRUD), Maintenance (CRUD) | Machinery & vehicle maintenance. |
| **Contractor** | Subcontract workforce & assigned tasks | Redundant with Worker role linked to a contractor entity. |
| **Worker** | Assigned tasks, Personal attendance, Report safety | Pure end-user tradesman/laborer. |

---

## 3. Evaluated Consolidation Options

### Option A: 5 Roles (Retaining Dedicated Safety Officer)
- Roles: Administrator, Project Manager, Site Operations Lead, Safety Officer, Worker.
- *Pros*: Extreme segregation of duties (safety inspector cannot alter material inventory).
- *Cons*: Still leaves inventory and equipment under-utilized on small sites; safety officer role has only 2 screens.

### Option B: 4 Roles (Clean Operational Hierarchy — Selected)
1. **Administrator** (`role_id: 1`):
   - Scope: User administration, RBAC permissions matrix, account request approval/rejection, system audit trail, system configuration, all reports.
2. **Project Manager** (`role_id: 2`):
   - Scope: Absorbs Site Engineer and Contractor management. Responsible for project creation, budgets, milestone progress tracking, workforce assignments, task delegation, and daily attendance logging.
3. **Site Supervisor** (`role_id: 3`):
   - Scope: Field operations lead. Absorbs Safety Officer, Inventory Manager, and Equipment Manager. Manages physical warehouse inventory (receipts and project issues), equipment fleet maintenance workflows, on-site safety checklist inspections, hazard violation tracking, and emergency incident investigations.
4. **Worker** (`role_id: 4`):
   - Scope: Field tradesmen & partner subcontractors. Views personal assigned tasks, checks daily presence records, and reports hazard observations.

---

## 4. Segregation of Duties Justification for 4 Roles

- **Governance vs. Operations**: The `Administrator` has no operational write access to projects or materials, preventing administrative conflict of interest.
- **Planning vs. Execution**: The `Project Manager` sets project budgets and defines tasks, but does NOT physically disburse warehouse inventory or complete maintenance orders.
- **Logistics & Safety in One Field Supervisor**: On commercial job sites, the Site Supervisor is legally accountable for site safety and physical equipment/material utilization. Violations and incidents are tied to immutable audit logs, preventing concealment.
- **Labor Compliance**: The `Worker` has strictly scoped read access to their own assignments and attendance, with safety reporting access.

---

## 5. Transition Mapping: Old Roles to New Consolidated Roles

| Old Role Name (8 Roles) | New Consolidated Role (4 Roles) | New Role ID |
| :--- | :--- | :---: |
| Administrator | **Administrator** | 1 |
| Project Manager | **Project Manager** | 2 |
| Site Engineer | **Project Manager** | 2 |
| Contractor | **Worker** (linked to contractor) | 4 |
| Inventory Manager | **Site Supervisor** | 3 |
| Equipment Manager | **Site Supervisor** | 3 |
| Safety Officer | **Site Supervisor** | 3 |
| Worker | **Worker** | 4 |

---

## 6. Demo Accounts for the 4 Roles

| Username | Password | Role | Description |
| :--- | :--- | :--- | :--- |
| **`admin`** | `Admin@123` | **Administrator** | User CRUD, permissions matrix, immutable audit log, reports |
| **`pm_sarah`** | `Pm@12345` | **Project Manager** | Projects, tasks, budgets, workforce allocation, attendance |
| **`supervisor_dan`**| `Super@123` | **Site Supervisor** | Inventory, equipment, maintenance, safety inspections & incidents |
| **`worker_bill`** | `Worker@12345` | **Worker** | Assigned tasks, personal attendance log, report safety issues |
