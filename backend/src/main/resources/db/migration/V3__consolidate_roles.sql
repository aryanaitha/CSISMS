-- =====================================================================
-- CSIMS V3 Migration: Consolidate 8 Roles into 4 Focused Roles
-- Roles: Administrator (1), Project Manager (2), Site Supervisor (3), Worker (4)
-- =====================================================================

-- Step 1: Temporarily disable foreign key checks to safely remap IDs
SET FOREIGN_KEY_CHECKS = 0;

-- 1. Migrate Account Requests & Audit Logs referencing old roles/users
UPDATE account_requests SET requested_role_id = 2 WHERE requested_role_id = 3;
UPDATE account_requests SET requested_role_id = 3 WHERE requested_role_id IN (4, 5, 6);
UPDATE account_requests SET requested_role_id = 4 WHERE requested_role_id IN (7, 8);
UPDATE account_requests SET reviewed_by = 1 WHERE reviewed_by IS NOT NULL AND reviewed_by NOT IN (1, 2, 3, 4);

UPDATE audit_logs SET user_id = 1 WHERE user_id NOT IN (1, 2, 3, 4);
UPDATE notifications SET user_id = 1 WHERE user_id NOT IN (1, 2, 3, 4);
UPDATE inventory_transactions SET performed_by = 3 WHERE performed_by NOT IN (1, 2, 3, 4);
UPDATE safety_inspections SET inspector_id = 3 WHERE inspector_id NOT IN (1, 2, 3, 4);
UPDATE safety_violations SET reported_by = 3 WHERE reported_by NOT IN (1, 2, 3, 4);
UPDATE incidents SET investigator_id = 3 WHERE investigator_id NOT IN (1, 2, 3, 4);
UPDATE incidents SET reported_by = 3 WHERE reported_by NOT IN (1, 2, 3, 4);

-- 2. Consolidate and redefine Roles table
DELETE FROM role_permissions;
DELETE FROM roles;

INSERT INTO roles (role_id, role_name, description) VALUES
(1, 'Administrator', 'Full system access, user management, audit logs, and security configuration'),
(2, 'Project Manager', 'Project oversight, workforce allocation, task management, and attendance'),
(3, 'Site Supervisor', 'Warehouse inventory, machinery maintenance, site safety inspections, and incident logs'),
(4, 'Worker', 'Field worker viewing assigned tasks, personal attendance, and reporting safety concerns');

-- 3. Remap Role Permissions for the 4 Roles
-- Role 1: Administrator (All 46 permissions)
INSERT INTO role_permissions (role_id, permission_id)
SELECT 1, permission_id FROM permissions;

-- Role 2: Project Manager
-- Projects (10, 11, 12, 13), Workforce (14, 15, 16, 17), Tasks (18, 19, 20, 21), Attendance (22, 23, 24),
-- Read-only views: Inventory (25), Equipment (30), Safety (36, 38, 41), Reports (45), Notifications (46)
INSERT INTO role_permissions (role_id, permission_id) VALUES
(2, 10), (2, 11), (2, 12), (2, 13), -- PROJECT
(2, 14), (2, 15), (2, 16), (2, 17), -- WORKFORCE
(2, 18), (2, 19), (2, 20), (2, 21), -- TASK
(2, 22), (2, 23), (2, 24),         -- ATTENDANCE
(2, 25),                            -- INVENTORY_VIEW
(2, 30),                            -- EQUIPMENT_VIEW
(2, 36), (2, 38), (2, 41),          -- SAFETY & INCIDENT VIEWS
(2, 45), (2, 46);                   -- REPORT_VIEW, NOTIFICATION_VIEW

-- Role 3: Site Supervisor
-- Inventory (25, 26, 27, 28, 29), Equipment (30, 31, 32), Maintenance (33, 34, 35),
-- Safety (36, 37, 38, 39, 40), Incidents (41, 42, 43),
-- Read-only views: Project (10), Workforce (14), Task (18), Reports (45), Notifications (46)
INSERT INTO role_permissions (role_id, permission_id) VALUES
(3, 10),                            -- PROJECT_VIEW
(3, 14),                            -- WORKFORCE_VIEW
(3, 18),                            -- TASK_VIEW
(3, 25), (3, 26), (3, 27), (3, 28), (3, 29), -- INVENTORY
(3, 30), (3, 31), (3, 32),                   -- EQUIPMENT
(3, 33), (3, 34), (3, 35),                   -- MAINTENANCE
(3, 36), (3, 37), (3, 38), (3, 39), (3, 40), -- SAFETY
(3, 41), (3, 42), (3, 43),                   -- INCIDENTS
(3, 45), (3, 46);                            -- REPORT_VIEW, NOTIFICATION_VIEW

-- Role 4: Worker
-- Task View & Update (18, 20), Attendance View (22), Report Safety Violation/Incident (39, 42), Notifications (46)
INSERT INTO role_permissions (role_id, permission_id) VALUES
(4, 18), (4, 20),                   -- TASK_VIEW, TASK_UPDATE
(4, 22),                            -- ATTENDANCE_VIEW
(4, 39),                            -- SAFETY_VIOLATION_CREATE
(4, 42),                            -- INCIDENT_CREATE
(4, 46);                            -- NOTIFICATION_VIEW

-- 4. Clean Demo Users: Exactly 1 demo user per role
DELETE FROM users;

INSERT INTO users (user_id, username, email, password_hash, salt, full_name, role_id, is_active, must_change_password) VALUES
(1, 'admin', 'admin@csims.local', '$2a$10$8bthJdCV1Yz9nn4xRe1wfuMaNLZT0Wa.Gtp1Q/eVwEXgKeTRI.qta', '', 'System Administrator', 1, 1, 0),
(2, 'pm_sarah', 'sarah.pm@csims.local', '$2a$10$vKgJPJ8ezcFNmrvzv4Yci.BeAJmBipnY.7/NJ0tMjZXC1Lz0KLUhq', '', 'Sarah Jenkins', 2, 1, 0),
(3, 'supervisor_dan', 'dan.supervisor@csims.local', '$2a$10$hdTyMeGYHHdoVkcOG3eXAexb722rBCzgqvNjA.r//G4dhR1Od9bPS', '', 'Dan Mitchell', 3, 1, 0),
(4, 'worker_bill', 'bill.worker@csims.local', '$2a$10$.qcHUqgBP8RYOWTYozJYluuZx5ETpTn2m5zEMxGb.g/.bM8ybZ1/W', '', 'Bill Miller', 4, 1, 0);

-- 5. Seed 3 Realistic Sample Projects
INSERT INTO projects (project_id, project_name, description, location, start_date, end_date, budget, status, progress_percent) VALUES
(1, 'Riverside Apartments – Block A', 'Construction of 12-storey residential tower including basement parking and structural framing.', '42 Riverside Blvd, Sector 4', '2026-01-15', '2026-12-30', 2450000.00, 'In Progress', 62),
(2, 'City Mall Foundation Work', 'Substructure excavation, sheet piling, reinforced concrete raft slab, and drainage utility network.', '108 Commercial Ring Road', '2026-03-01', '2026-11-15', 4800000.00, 'In Progress', 35),
(3, 'Highway Bridge Repair', 'Expansion joint refurbishment, seismic retrofitting, and concrete deck re-surfacing.', 'North Expressway Interchange 9', '2026-11-01', '2027-04-30', 1200000.00, 'Planning', 0)
ON DUPLICATE KEY UPDATE
    project_name = VALUES(project_name),
    description = VALUES(description),
    location = VALUES(location),
    start_date = VALUES(start_date),
    end_date = VALUES(end_date),
    status = VALUES(status),
    progress_percent = VALUES(progress_percent),
    budget = VALUES(budget);

-- 6. Contractors & Workers
INSERT INTO contractors (contractor_id, company_name, contact_person, email, phone, address) VALUES
(1, 'Apex Structural Builders', 'David Ross', 'dross@apexbuilders.com', '555-0199', '100 Industrial Pkwy, Sector 4')
ON DUPLICATE KEY UPDATE company_name = VALUES(company_name);

INSERT INTO workers (worker_id, full_name, email, phone, trade, contractor_id, status, joined_date) VALUES
(1, 'Bill Miller', 'bill.worker@csims.local', '555-0101', 'Structural Carpentry', 1, 'ACTIVE', '2026-01-20'),
(2, 'Carlos Santos', 'carlos.s@apexbuilders.com', '555-0102', 'Masonry & Concrete', 1, 'ACTIVE', '2026-01-22'),
(3, 'David Chen', 'david.c@apexbuilders.com', '555-0103', 'Heavy Equipment Operator', 1, 'ACTIVE', '2026-02-01')
ON DUPLICATE KEY UPDATE full_name = VALUES(full_name), email = VALUES(email);

-- Assign Workers to Projects
INSERT IGNORE INTO project_workers (project_id, worker_id, role_in_project, assigned_date) VALUES
(1, 1, 'Lead Carpenter', '2026-01-25'),
(1, 2, 'Senior Mason', '2026-01-25'),
(2, 3, 'Excavator Operator', '2026-03-05');

-- 7. Tasks with diverse workflow states (Created, Assigned, In Progress, Completed, Overdue)
DELETE FROM tasks;
INSERT INTO tasks (task_id, project_id, title, description, priority, status, completion_percent, assigned_worker_id, start_date, due_date) VALUES
(1, 1, 'Level 4 Slab Formwork', 'Assemble timber and aluminium formwork deck for fourth-floor concrete pour.', 'HIGH', 'Completed', 100, 1, '2026-10-01', '2026-10-10'),
(2, 1, 'Rebar Cage Tying Section B', 'Tie vertical reinforcement steel rebars as per structural engineering drawings.', 'URGENT', 'In Progress', 70, 1, '2026-10-12', '2026-10-25'),
(3, 1, 'Perimeter Safety Netting Inspection', 'Inspect safety guardrails and catch netting on exposed building perimeter.', 'HIGH', 'In Progress', 50, 1, '2026-10-05', '2026-10-20'),
(4, 2, 'Deep Pit Excavation Zone 3', 'Bulk excavation for lift core shaft down to -8.5m foundation datum.', 'URGENT', 'In Progress', 40, 3, '2026-10-10', '2026-10-28'),
(5, 2, 'Raft Slab Reinforcement Layout', 'Position bottom rebar mat on blinding concrete.', 'MEDIUM', 'Created', 0, NULL, '2026-10-25', '2026-11-05'),
(6, 3, 'Site Mobilization & Traffic Diversion', 'Set up warning barricades, high-visibility signage, and temporary detour lane.', 'LOW', 'Created', 0, NULL, '2026-11-01', '2026-11-10');

-- 8. Attendance Records
DELETE FROM attendance;
INSERT INTO attendance (worker_id, project_id, attendance_date, status, remarks, recorded_by) VALUES
(1, 1, '2026-10-18', 'Present', 'Shift 07:00 - 16:00 on Level 4', 2),
(1, 1, '2026-10-19', 'Present', 'Shift 07:00 - 16:00', 2),
(1, 1, '2026-10-20', 'Present', 'Formwork assembly', 2),
(1, 1, '2026-10-21', 'Present', 'Full day worked', 2),
(1, 1, '2026-10-22', 'Present', 'Morning check-in OK', 2),
(2, 1, '2026-10-22', 'Present', 'Concrete pump pouring', 2),
(3, 2, '2026-10-22', 'Present', 'Operating CAT Excavator', 2);

-- 9. Materials (including one below minimum stock)
DELETE FROM materials;
INSERT INTO materials (material_id, material_code, name, category, unit, quantity_on_hand, min_stock, max_stock, unit_price, location) VALUES
(1, 'MAT-CEM-01', 'Portland Cement Type I', 'Binding Materials', 'Bags (50kg)', 45.00, 100.00, 1000.00, 12.50, 'Warehouse Bay A-1'),
(2, 'MAT-STE-02', 'Grade 60 Steel Rebar 16mm', 'Reinforcement', 'Tons', 140.00, 30.00, 300.00, 850.00, 'Open Yard B'),
(3, 'MAT-AGG-03', 'Coarse Crushed Aggregate 20mm', 'Aggregates', 'Cubic Metres', 280.00, 80.00, 600.00, 45.00, 'Aggregate Stockpile 1'),
(4, 'MAT-BRK-04', 'Modular Solid Clay Bricks', 'Masonry', 'Thousand Units', 25.00, 10.00, 50.00, 320.00, 'Pallet Storage C');

-- 10. Equipment (including one in Maintenance)
DELETE FROM equipment;
INSERT INTO equipment (equipment_id, equipment_code, name, category, status, project_id, purchase_date, last_service_date, next_service_due) VALUES
(1, 'EQ-CRN-01', 'Potain Tower Crane 50m Jib', 'Cranes', 'In Use', 1, '2024-01-10', '2026-08-10', '2026-12-15'),
(2, 'EQ-EXC-02', 'CAT 320D Hydraulic Excavator', 'Earthmoving', 'Under Maintenance', 2, '2024-03-15', '2026-05-15', '2026-10-22'),
(3, 'EQ-MIX-03', 'Schwing Stetter Concrete Transit Mixer', 'Concrete', 'Available', 1, '2024-06-20', '2026-09-01', '2026-11-20');

-- 11. Maintenance Records
DELETE FROM maintenance;
INSERT INTO maintenance (maintenance_id, equipment_id, maintenance_type, status, scheduled_date, completed_date, cost, technician, notes) VALUES
(1, 2, '500-Hour Hydraulic Fluid & Filter Service', 'Due', '2026-10-22', NULL, 650.00, 'CAT Field Technician', '500-hour hydraulic fluid change, filter replacement, and track tensioning.');

-- 12. Safety Inspection with calculated score
DELETE FROM safety_inspections;
INSERT INTO safety_inspections (inspection_id, project_id, inspector_id, inspection_date, ppe_score, worker_safety_score, equipment_score, work_area_score, emergency_score, overall_score, remarks) VALUES
(1, 1, 3, '2026-10-20', 90, 85, 80, 85, 90, 86.00, 'Overall good compliance. Scaffolding tag inspection verified.');

-- 13. Safety Violation
DELETE FROM safety_violations;
INSERT INTO safety_violations (violation_id, inspection_id, project_id, worker_id, severity, description, corrective_action, status, resolution_date, reported_by) VALUES
(1, 1, 1, 2, 'Major', 'Perimeter edge guardrail removed without temporary harness tie-off point on Level 4.', 'Reinstall double-height guardrails and inspect lanyard anchor rings before work resumes.', 'OPEN', NULL, 3);

-- 14. Site Incident
DELETE FROM incidents;
INSERT INTO incidents (incident_id, project_id, incident_date, severity, title, description, investigator_id, actions_taken, status, reported_by) VALUES
(1, 2, '2026-10-21 14:30:00', 'Moderate', 'Hydraulic hose rupture on CAT 320D during trench digging', 'High-pressure hydraulic return line burst. Equipment safely turned off, spill containment pads applied.', 3, 'Replaced hydraulic line with heavy-duty reinforced hose. Machine scheduled for full inspection.', 'Investigating', 3);

-- 15. Notifications
DELETE FROM notifications;
INSERT INTO notifications (notification_id, user_id, title, message, type, is_read) VALUES
(1, 3, 'Low Stock Alert: Portland Cement', 'Portland Cement stock is at 45 Bags, below minimum threshold of 100 Bags.', 'LOW_STOCK', 0),
(2, 3, 'Maintenance Due: CAT 320D Excavator', 'Scheduled 500-hour preventative maintenance is due today.', 'MAINTENANCE_DUE', 0),
(3, 2, 'Task Updated: Level 4 Slab Formwork', 'Task marked completed by Bill Miller.', 'TASK_UPDATE', 0),
(4, 4, 'New Task Assigned: Perimeter Safety Netting', 'You have been assigned to inspect perimeter safety netting on Project Riverside.', 'TASK_ASSIGNMENT', 0);

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;
