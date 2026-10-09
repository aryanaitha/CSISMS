-- =====================================================================
-- Construction Site Intelligence & Safety Management System (CSIMS)
-- Seed Data (Roles, Permissions, Users, Demo Master Data)
-- =====================================================================

USE csims_db;

-- 1. Roles
INSERT INTO roles (role_id, role_name, description) VALUES
(1, 'Administrator', 'Full system access, user management, audit logs, and security configuration'),
(2, 'Project Manager', 'Project oversight, task assignment, workforce allocation, and progress tracking'),
(3, 'Site Engineer', 'On-site execution, task updates, attendance recording, and material requests'),
(4, 'Safety Officer', 'Site safety inspections, violation logging, and incident management'),
(5, 'Inventory Manager', 'Warehouse inventory, material receipts, issues, and stock level control'),
(6, 'Equipment Manager', 'Heavy machinery and equipment tracking, service scheduling, and maintenance'),
(7, 'Contractor', 'Subcontractor partner managing assigned workforce and tasks'),
(8, 'Worker', 'Field worker viewing personal assignments, attendance, and reporting safety concerns')
ON DUPLICATE KEY UPDATE role_name = VALUES(role_name);

-- 2. Permissions (MODULE_ACTION)
INSERT INTO permissions (permission_id, permission_code, description, module) VALUES
-- User & Role
(1, 'USER_VIEW', 'View users and profile information', 'USER'),
(2, 'USER_CREATE', 'Create new system users', 'USER'),
(3, 'USER_UPDATE', 'Modify existing user details', 'USER'),
(4, 'USER_STATUS', 'Activate or deactivate user accounts', 'USER'),
(5, 'USER_PASSWORD_RESET', 'Reset passwords for users', 'USER'),
(6, 'ROLE_VIEW', 'View roles and system permissions', 'ROLE'),
(7, 'ROLE_UPDATE', 'Modify role permissions', 'ROLE'),
(8, 'ACCOUNT_REQUEST_VIEW', 'View registration and reset requests', 'ACCOUNT_REQUEST'),
(9, 'ACCOUNT_REQUEST_REVIEW', 'Approve or reject account requests', 'ACCOUNT_REQUEST'),

-- Project
(10, 'PROJECT_VIEW', 'View all projects and project details', 'PROJECT'),
(11, 'PROJECT_CREATE', 'Create new construction projects', 'PROJECT'),
(12, 'PROJECT_UPDATE', 'Update project progress, status, or details', 'PROJECT'),
(13, 'PROJECT_ARCHIVE', 'Archive finished projects', 'PROJECT'),

-- Workforce & Contractor
(14, 'WORKFORCE_VIEW', 'View all workers and contractors', 'WORKFORCE'),
(15, 'WORKFORCE_CREATE', 'Add new workers or contractor companies', 'WORKFORCE'),
(16, 'WORKFORCE_UPDATE', 'Update worker or contractor records', 'WORKFORCE'),
(17, 'WORKFORCE_ASSIGN', 'Assign workers to specific projects', 'WORKFORCE'),

-- Tasks
(18, 'TASK_VIEW', 'View task lists and task details', 'TASK'),
(19, 'TASK_CREATE', 'Create project tasks', 'TASK'),
(20, 'TASK_UPDATE', 'Update task progress and workflow status', 'TASK'),
(21, 'TASK_ASSIGN', 'Assign or reassign tasks to workers', 'TASK'),

-- Attendance
(22, 'ATTENDANCE_VIEW', 'View workforce attendance sheets', 'ATTENDANCE'),
(23, 'ATTENDANCE_RECORD', 'Mark daily attendance for workers', 'ATTENDANCE'),
(24, 'ATTENDANCE_CORRECT', 'Correct or modify past attendance records', 'ATTENDANCE'),

-- Inventory
(25, 'INVENTORY_VIEW', 'View material catalog and stock levels', 'INVENTORY'),
(26, 'INVENTORY_CREATE', 'Add new materials to catalog', 'INVENTORY'),
(27, 'INVENTORY_UPDATE', 'Update material details and threshold limits', 'INVENTORY'),
(28, 'INVENTORY_RECEIPT', 'Record incoming material shipments', 'INVENTORY'),
(29, 'INVENTORY_ISSUE', 'Issue materials to construction projects', 'INVENTORY'),

-- Equipment & Maintenance
(30, 'EQUIPMENT_VIEW', 'View construction equipment catalog and statuses', 'EQUIPMENT'),
(31, 'EQUIPMENT_CREATE', 'Register new machinery or equipment', 'EQUIPMENT'),
(32, 'EQUIPMENT_UPDATE', 'Update equipment status or details', 'EQUIPMENT'),
(33, 'MAINTENANCE_VIEW', 'View equipment maintenance schedules and records', 'MAINTENANCE'),
(34, 'MAINTENANCE_SCHEDULE', 'Schedule preventative or corrective maintenance', 'MAINTENANCE'),
(35, 'MAINTENANCE_UPDATE', 'Update and complete maintenance service orders', 'MAINTENANCE'),

-- Safety & Incidents
(36, 'SAFETY_INSPECTION_VIEW', 'View safety inspection records', 'SAFETY'),
(37, 'SAFETY_INSPECTION_CREATE', 'Conduct and submit safety inspection checklist', 'SAFETY'),
(38, 'SAFETY_VIOLATION_VIEW', 'View safety violations', 'SAFETY'),
(39, 'SAFETY_VIOLATION_CREATE', 'Log new safety violations', 'SAFETY'),
(40, 'SAFETY_VIOLATION_UPDATE', 'Update violation corrective actions and resolution', 'SAFETY'),
(41, 'INCIDENT_VIEW', 'View incident reports', 'INCIDENT'),
(42, 'INCIDENT_CREATE', 'Report site accidents or incidents', 'INCIDENT'),
(43, 'INCIDENT_UPDATE', 'Investigate and resolve site incidents', 'INCIDENT'),

-- Audit & Reports
(44, 'AUDIT_VIEW', 'View immutable system audit logs', 'AUDIT'),
(45, 'REPORT_VIEW', 'View and export system reports to CSV', 'REPORT'),
(46, 'NOTIFICATION_VIEW', 'View user notifications', 'NOTIFICATION')
ON DUPLICATE KEY UPDATE permission_code = VALUES(permission_code);

-- 3. Role-Permission mappings
-- Clear existing to avoid duplicate conflicts
DELETE FROM role_permissions;

-- Administrator: All permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT 1, permission_id FROM permissions;

-- Project Manager
INSERT INTO role_permissions (role_id, permission_id) VALUES
(2, 10), (2, 11), (2, 12), (2, 13), -- PROJECT
(2, 14), (2, 15), (2, 16), (2, 17), -- WORKFORCE
(2, 18), (2, 19), (2, 20), (2, 21), -- TASK
(2, 22),                            -- ATTENDANCE VIEW
(2, 25),                            -- INVENTORY VIEW
(2, 30),                            -- EQUIPMENT VIEW
(2, 36), (2, 38), (2, 41), (2, 42), -- SAFETY & INCIDENTS
(2, 45), (2, 46);                   -- REPORTS, NOTIFICATIONS

-- Site Engineer
INSERT INTO role_permissions (role_id, permission_id) VALUES
(3, 10),                            -- PROJECT VIEW
(3, 14),                            -- WORKFORCE VIEW
(3, 18), (3, 20), (3, 21),          -- TASK VIEW, UPDATE, ASSIGN
(3, 22), (3, 23), (3, 24),          -- ATTENDANCE VIEW, RECORD, CORRECT
(3, 25), (3, 29),                   -- INVENTORY VIEW, ISSUE
(3, 30),                            -- EQUIPMENT VIEW
(3, 38), (3, 41), (3, 42),          -- VIOLATION VIEW, INCIDENT VIEW, INCIDENT CREATE
(3, 46);                            -- NOTIFICATIONS

-- Safety Officer
INSERT INTO role_permissions (role_id, permission_id) VALUES
(4, 10),                            -- PROJECT VIEW
(4, 14),                            -- WORKFORCE VIEW
(4, 36), (4, 37),                   -- SAFETY INSPECTIONS
(4, 38), (4, 39), (4, 40),          -- SAFETY VIOLATIONS
(4, 41), (4, 42), (4, 43),          -- INCIDENTS
(4, 45), (4, 46);                   -- REPORTS, NOTIFICATIONS

-- Inventory Manager
INSERT INTO role_permissions (role_id, permission_id) VALUES
(5, 10),                            -- PROJECT VIEW
(5, 25), (5, 26), (5, 27), (5, 28), (5, 29), -- INVENTORY
(5, 45), (5, 46);                   -- REPORTS, NOTIFICATIONS

-- Equipment Manager
INSERT INTO role_permissions (role_id, permission_id) VALUES
(6, 10),                            -- PROJECT VIEW
(6, 30), (6, 31), (6, 32),          -- EQUIPMENT
(6, 33), (6, 34), (6, 35),          -- MAINTENANCE
(6, 45), (6, 46);                   -- REPORTS, NOTIFICATIONS

-- Contractor
INSERT INTO role_permissions (role_id, permission_id) VALUES
(7, 10),                            -- PROJECT VIEW
(7, 14),                            -- WORKFORCE VIEW
(7, 18), (7, 20),                   -- TASK VIEW & UPDATE
(7, 22),                            -- ATTENDANCE VIEW
(7, 42),                            -- INCIDENT CREATE
(7, 46);                            -- NOTIFICATIONS

-- Worker
INSERT INTO role_permissions (role_id, permission_id) VALUES
(8, 18), (8, 20),                   -- TASK VIEW & UPDATE
(8, 22),                            -- ATTENDANCE VIEW
(8, 42),                            -- INCIDENT CREATE
(8, 46);                            -- NOTIFICATIONS

-- 4. Demo Users (Seed Passwords)
-- All demo users have must_change_password = FALSE for fast evaluation.
-- reset_test user has must_change_password = TRUE for testing forced password change.
INSERT INTO users (user_id, username, email, password_hash, salt, full_name, role_id, is_active, failed_attempts, locked_until, must_change_password) VALUES
(1, 'admin', 'admin@csims.local', 'Tijfz/oDA5vJABk4LD9a30rnFlzStrwM9VvflPHr0MQ=', 'esyxJNsKGUrXBc76XCYQ1Q==', 'System Administrator', 1, TRUE, 0, NULL, FALSE),
(2, 'pm_user', 'pm@csims.local', 'zM8J/bhFHOw33Kail6F12Lb45iqDoo5ftp107jQQ/Zg=', 'KjggCEioG13hfbeoyLxsWQ==', 'Sarah Connor (PM)', 2, TRUE, 0, NULL, FALSE),
(3, 'engineer', 'engineer@csims.local', 'a26WUyRna7ot0HZUoO87tzUGZ5lZUnPgSnxTOL7iWP0=', 'IIDXWwJMhlEK7Ftft5+Glg==', 'Dave Miller (Engineer)', 3, TRUE, 0, NULL, FALSE),
(4, 'safety', 'safety@csims.local', 'G1zTf73BU/j1TfyN4p5E2WWXb0SVtuGmfVg8hYpTYnw=', 'hhnppZ31U/JzJJQMs2zPQQ==', 'Marcus Vance (Safety)', 4, TRUE, 0, NULL, FALSE),
(5, 'inventory', 'inventory@csims.local', '5vpAhmAbaIt8LW6WSRuPWFrGH7+VDWKWPHhiyGk7OXo=', 'R7GjIrCfUOeYihFDbpBlug==', 'Elena Rostova (Inventory)', 5, TRUE, 0, NULL, FALSE),
(6, 'equipment', 'equipment@csims.local', 'Fs6VIayAIZJSvQUq+s26oL5UqfPHq3PEzMjGoZIwKaU=', 'Djfmp0/2MILzmkXTIsGrQw==', 'Tom Hardy (Equipment)', 6, TRUE, 0, NULL, FALSE),
(7, 'contractor', 'contractor@csims.local', 'iEeqJP9eUDvtoVPKqIStIBpsOzg++lT9qQJmoMnKQiM=', '2HFKQQTmmK8WvIXkWiRXCA==', 'Apex Builders (Contractor)', 7, TRUE, 0, NULL, FALSE),
(8, 'worker', 'worker@csims.local', '2MABvrQQ39CR8dpSZwGix2SMF7juQE27AYbQRSU58iY=', 'HRGl+1uJe7gmBcbvGqOXgw==', 'John Doe (Site Worker)', 8, TRUE, 0, NULL, FALSE),
(9, 'reset_test', 'reset@csims.local', 'eQBzNRwB8CsTJuz1Pp7j4pbVJj8EANC28eGhXglndbY=', 'QWCUdn/ORS3rRRsP1bBl/Q==', 'Password Reset Test User', 8, TRUE, 0, NULL, TRUE)
ON DUPLICATE KEY UPDATE username = VALUES(username);

-- 5. Contractors
INSERT INTO contractors (contractor_id, company_name, contact_person, email, phone, address) VALUES
(1, 'Apex Structural Foundations', 'Robert Vance', 'contact@apexstruct.com', '+1-555-0199', '100 Industrial Pkwy, Sector 4'),
(2, 'Skyline Electricals & MEP', 'Linda Zhang', 'linda@skyline-mep.com', '+1-555-0245', '45 Highland Ave, Suite 300'),
(3, 'Titan Masonry & Concrete', 'Carlos Gomez', 'carlos@titanmasonry.com', '+1-555-0312', '78 Quarry Road, Westgate')
ON DUPLICATE KEY UPDATE company_name = VALUES(company_name);

-- 6. Projects
INSERT INTO projects (project_id, project_name, description, location, start_date, end_date, budget, status, progress_percent) VALUES
(1, 'Metro Rail Line Extension - Phase 3', 'High-speed urban rail station and elevated viaduct construction', 'Downtown Hub - Zone 1', '2026-01-15', '2027-06-30', 45000000.00, 'Active', 38),
(2, 'Harbor View Commercial Complex', '32-story modern steel and glass commercial office tower', 'Harbor District, Pier 12', '2025-08-01', '2026-12-15', 82000000.00, 'Active', 65),
(3, 'Greenfield Logistics Center', 'Industrial automated distribution warehouse and logistics yard', 'Industrial Park West', '2026-03-01', '2026-11-20', 18500000.00, 'Planning', 10),
(4, 'Riverside Promenade & Bridge', 'Pedestrian suspension bridge and flood barrier reinforcement', 'North Riverside', '2024-05-10', '2025-11-30', 12000000.00, 'Completed', 100)
ON DUPLICATE KEY UPDATE project_name = VALUES(project_name);

-- 7. Workers
INSERT INTO workers (worker_id, full_name, email, phone, trade, contractor_id, status, joined_date) VALUES
(1, 'John Doe', 'john.doe@workers.local', '+1-555-1001', 'Mason', 3, 'ACTIVE', '2025-01-10'),
(2, 'Carlos Ruiz', 'carlos.ruiz@workers.local', '+1-555-1002', 'Electrician', 2, 'ACTIVE', '2025-02-15'),
(3, 'Ahmed Khan', 'ahmed.k@workers.local', '+1-555-1003', 'Steel Fixer', 1, 'ACTIVE', '2025-03-01'),
(4, 'Michael Chang', 'm.chang@workers.local', '+1-555-1004', 'Heavy Machine Operator', 1, 'ACTIVE', '2025-01-20'),
(5, 'David O''Connor', 'doconnor@workers.local', '+1-555-1005', 'Carpenter', 3, 'ACTIVE', '2025-04-12'),
(6, 'Samuel Jackson', 'sam.j@workers.local', '+1-555-1006', 'Welder', 2, 'ON_LEAVE', '2025-05-01')
ON DUPLICATE KEY UPDATE full_name = VALUES(full_name);

-- 8. Project Workers
INSERT INTO project_workers (project_id, worker_id, role_in_project, assigned_date) VALUES
(1, 1, 'Lead Mason', '2026-01-20'),
(1, 3, 'Structural Steel Technician', '2026-01-20'),
(1, 4, 'Crane Operator', '2026-01-20'),
(2, 2, 'Lead Electrician', '2025-08-10'),
(2, 5, 'Formwork Carpenter', '2025-08-10'),
(3, 4, 'Excavator Operator', '2026-03-05')
ON DUPLICATE KEY UPDATE role_in_project = VALUES(role_in_project);

-- 9. Tasks
INSERT INTO tasks (task_id, project_id, title, description, priority, status, completion_percent, assigned_worker_id, start_date, due_date) VALUES
(1, 1, 'Pour Foundation Piles - Zone A', 'Borehole drilling and concrete pouring for viaduct piers 1 through 8', 'HIGH', 'In Progress', 60, 1, '2026-09-01', '2026-10-25'),
(2, 1, 'Erect Structural Steel Girders', 'Assemble and bolt pre-fabricated steel spans across the railway crossway', 'CRITICAL', 'Assigned', 15, 3, '2026-10-10', '2026-11-15'),
(3, 1, 'Site Earthwork & Grading', 'Excavate soil and level foundation bed for station concourse', 'MEDIUM', 'Completed', 100, 4, '2026-08-01', '2026-08-30'),
(4, 2, 'Rough-in Conduit - Floors 12 to 18', 'Install fire-rated electrical conduits and cable trays', 'MEDIUM', 'In Progress', 45, 2, '2026-09-15', '2026-10-30'),
(5, 2, 'Curtain Wall Framing Inspection', 'Verify exterior glazing brackets alignment before glass delivery', 'HIGH', 'Created', 0, 5, '2026-10-20', '2026-11-05')
ON DUPLICATE KEY UPDATE title = VALUES(title);

-- 10. Attendance
INSERT INTO attendance (worker_id, project_id, attendance_date, status, remarks, recorded_by) VALUES
(1, 1, '2026-10-07', 'Present', 'Shift 1, On-time', 3),
(3, 1, '2026-10-07', 'Present', 'Shift 1, Structural yard', 3),
(4, 1, '2026-10-07', 'Present', 'Shift 1, Heavy equipment crew', 3),
(2, 2, '2026-10-07', 'Present', 'Level 14 electrical works', 3),
(5, 2, '2026-10-07', 'Absent', 'Personal emergency reported', 3),
(6, 2, '2026-10-07', 'Leave', 'Approved medical leave', 3)
ON DUPLICATE KEY UPDATE status = VALUES(status);

-- 11. Materials
INSERT INTO materials (material_id, material_code, name, category, unit, quantity_on_hand, min_stock, max_stock, unit_price, location) VALUES
(1, 'MAT-CEM-01', 'Portland Cement Grade 53 (50kg)', 'Cement', 'Bags', 1200.00, 300.00, 3000.00, 7.50, 'Warehouse A - Bay 1'),
(2, 'MAT-STL-01', 'TMT Rebar Steel 16mm (12m)', 'Steel', 'Tonnes', 45.00, 15.00, 100.00, 680.00, 'Open Yard 2'),
(3, 'MAT-AGG-01', 'Crushed Stone Aggregate 20mm', 'Aggregates', 'Tonnes', 220.00, 50.00, 500.00, 22.00, 'Batching Yard B'),
(4, 'MAT-SAF-01', 'Hard Hats - ANSI Class E', 'Safety Gear', 'Units', 24.00, 50.00, 200.00, 18.00, 'Safety Locker 1'), -- LOW STOCK TRIGGER
(5, 'MAT-SAF-02', 'High-Visibility Safety Vests', 'Safety Gear', 'Units', 35.00, 40.00, 150.00, 9.50, 'Safety Locker 1') -- LOW STOCK TRIGGER
ON DUPLICATE KEY UPDATE material_code = VALUES(material_code);

-- 12. Inventory Transactions
INSERT INTO inventory_transactions (transaction_id, material_id, transaction_type, quantity, project_id, reference_no, remarks, performed_by) VALUES
(1, 1, 'RECEIPT', 1500.00, NULL, 'PO-2026-081', 'Bulk procurement from Holcim', 5),
(2, 1, 'ISSUE', 300.00, 1, 'REQ-MTR-01', 'Issued for Pier foundation concrete mix', 5),
(3, 2, 'RECEIPT', 60.00, NULL, 'PO-2026-085', 'ArcelorMittal steel delivery', 5),
(4, 2, 'ISSUE', 15.00, 1, 'REQ-MTR-02', 'Issued for station reinforcement cages', 5),
(5, 4, 'ISSUE', 76.00, 1, 'REQ-SAF-01', 'Issued to new batch of site workers', 5)
ON DUPLICATE KEY UPDATE reference_no = VALUES(reference_no);

-- 13. Equipment
INSERT INTO equipment (equipment_id, equipment_code, name, category, status, project_id, purchase_date, last_service_date, next_service_due) VALUES
(1, 'EQ-CRN-01', 'Liebherr 280 EC-H Tower Crane', 'Cranes', 'In Use', 1, '2023-04-10', '2026-08-15', '2026-10-15'),
(2, 'EQ-EXC-01', 'CAT 336 Hydraulic Excavator', 'Earthmoving', 'In Use', 1, '2024-02-18', '2026-07-20', '2026-10-05'), -- MAINTENANCE OVERDUE TRIGGER
(3, 'EQ-GEN-01', 'Cummins 250kVA Diesel Generator', 'Power', 'Assigned', 2, '2024-06-12', '2026-09-01', '2026-12-01'),
(4, 'EQ-MIX-01', 'Schwing Stetter Concrete Mixer Truck', 'Concrete', 'Under Maintenance', 1, '2023-11-05', '2026-09-25', '2026-10-25'),
(5, 'EQ-LIFT-01', 'JLG 660SJ Telescopic Boom Lift', 'Access', 'Available', NULL, '2025-01-14', '2026-08-10', '2026-11-10')
ON DUPLICATE KEY UPDATE equipment_code = VALUES(equipment_code);

-- 14. Maintenance
INSERT INTO maintenance (maintenance_id, equipment_id, maintenance_type, status, scheduled_date, completed_date, cost, technician, notes) VALUES
(1, 2, '500-Hour Hydraulic System Overhaul', 'Due', '2026-10-05', NULL, 1200.00, 'Apex Service Hub', 'Scheduled routine oil change and filter replacement'),
(2, 4, 'Hydraulic Drum Motor Replacement', 'Serviced', '2026-09-25', NULL, 3450.00, 'HydraTech Dynamics', 'Replaced worn seals and motor assembly; awaiting final load test'),
(3, 1, 'Monthly Safety Cable & Hook Testing', 'Completed', '2026-08-15', '2026-08-15', 450.00, 'Rigging Inspectors Ltd', 'Load cell and wire rope tested within safe margins')
ON DUPLICATE KEY UPDATE maintenance_id = VALUES(maintenance_id);

-- 15. Safety Inspections
INSERT INTO safety_inspections (inspection_id, project_id, inspector_id, inspection_date, ppe_score, worker_safety_score, equipment_score, work_area_score, emergency_score, overall_score, remarks) VALUES
(1, 1, 4, '2026-10-06', 90, 85, 95, 80, 90, 88.00, 'Comprehensive bi-weekly audit of Zone 1. Guard rails need reinforcement at elevated pier 4.'),
(2, 2, 4, '2026-10-02', 100, 95, 90, 95, 100, 96.00, 'Floors 12 to 16 inspected. Good housekeeping and clear emergency escape routes.')
ON DUPLICATE KEY UPDATE inspection_id = VALUES(inspection_id);

-- 16. Safety Violations
INSERT INTO safety_violations (violation_id, inspection_id, project_id, worker_id, severity, description, corrective_action, status, resolution_date, reported_by) VALUES
(1, 1, 1, 1, 'Medium', 'Worker observed working without chin strap secured on hard hat while near mobile crane.', 'Issued verbal warning and retrained on falling object hazards.', 'RESOLVED', '2026-10-06', 4),
(2, 1, 1, NULL, 'High', 'Perimeter scaffolding missing mid-rail on level 3 working platform.', 'Installed missing safety railing before continuing work.', 'RESOLVED', '2026-10-07', 4),
(3, NULL, 1, 3, 'Critical', 'Hot work (welding) conducted within 3 meters of flammable storage container without fire watch.', 'Work halted immediately; relocated storage area and deployed dry chemical extinguisher.', 'OPEN', NULL, 4)
ON DUPLICATE KEY UPDATE violation_id = VALUES(violation_id);

-- 17. Incidents
INSERT INTO incidents (incident_id, project_id, incident_date, severity, title, description, investigator_id, actions_taken, status, reported_by) VALUES
(1, 1, '2026-09-28 14:15:00', 'Moderate', 'Hydraulic hose rupture on excavator', 'Hydraulic fluid leak caused minor splash on soil. No worker injuries occurred.', 4, 'Absorbent pads deployed immediately, contaminated soil scooped and bagged for hazardous disposal.', 'Resolved', 3),
(2, 2, '2026-10-05 09:30:00', 'Minor', 'Scaffolding toe-board dislodged', 'Toe-board dropped 2 meters onto safety net during assembly. No personnel injured.', 4, 'Secured double clamping on all toe-board connections.', 'Action Taken', 2)
ON DUPLICATE KEY UPDATE incident_id = VALUES(incident_id);

-- 18. Notifications
INSERT INTO notifications (notification_id, user_id, title, message, type, is_read) VALUES
(1, 5, 'Low Stock Alert: Hard Hats', 'Hard Hats stock is at 24 units, which is below the minimum threshold of 50 units.', 'LOW_STOCK', FALSE),
(2, 5, 'Low Stock Alert: Safety Vests', 'Safety Vests stock is at 35 units, which is below the minimum threshold of 40 units.', 'LOW_STOCK', FALSE),
(3, 6, 'Maintenance Due: CAT 336 Excavator', 'Hydraulic overhaul was due on 2026-10-05.', 'MAINTENANCE_DUE', FALSE),
(4, 4, 'Critical Violation Logged', 'Critical welding safety violation reported at Metro Rail Line Extension.', 'SAFETY_VIOLATION', FALSE),
(5, 2, 'Task Assignment Update', 'Task "Pour Foundation Piles - Zone A" reached 60% completion.', 'TASK_UPDATE', TRUE)
ON DUPLICATE KEY UPDATE notification_id = VALUES(notification_id);

-- 19. Initial Audit Logs
INSERT INTO audit_logs (audit_id, user_id, username, role, action, module, entity_type, entity_id, old_value, new_value, status, description) VALUES
(1, 1, 'admin', 'Administrator', 'SYSTEM_INITIALIZATION', 'SYSTEM', 'DATABASE', 'SCHEMA', NULL, 'v1.0.0', 'SUCCESS', 'Initialized CSIMS database and seeded core security policies and demo datasets.'),
(2, 1, 'admin', 'Administrator', 'USER_CREATE', 'USER', 'USER', '2', NULL, 'pm_user', 'SUCCESS', 'Created project manager account Sarah Connor.'),
(3, 4, 'safety', 'Safety Officer', 'SAFETY_INSPECTION_CREATE', 'SAFETY', 'INSPECTION', '1', NULL, 'Score: 88%', 'SUCCESS', 'Conducted safety audit on Metro Rail Line Extension.')
ON DUPLICATE KEY UPDATE audit_id = VALUES(audit_id);

-- 20. Account Requests (Samples for Admin Review)
INSERT INTO account_requests (request_id, request_type, full_name, email, username, requested_role_id, reason, status) VALUES
(1, 'REGISTRATION', 'Daniel Craig', 'dcraig@construct.com', 'dcraig', 3, 'Newly appointed Junior Site Engineer for Metro Rail project.', 'PENDING'),
(2, 'PASSWORD_RESET', 'Elena Rostova', 'inventory@csims.local', 'inventory', 5, 'Forgot credentials after returning from field leave.', 'PENDING')
ON DUPLICATE KEY UPDATE request_id = VALUES(request_id);
