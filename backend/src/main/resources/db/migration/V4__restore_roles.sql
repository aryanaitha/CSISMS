-- =====================================================================
-- CSIMS V4 Migration: Restore Original 8 Enterprise Roles
-- Roles: Administrator (1), Project Manager (2), Site Engineer (3),
--        Safety Officer (4), Inventory Manager (5), Equipment Manager (6),
--        Contractor (7), Worker (8)
-- =====================================================================

SET FOREIGN_KEY_CHECKS = 0;

-- 1. Restore the 8 Roles
DELETE FROM role_permissions;
DELETE FROM roles;

INSERT INTO roles (role_id, role_name, description) VALUES
(1, 'Administrator', 'Full system access, user management, audit logs, and security configuration'),
(2, 'Project Manager', 'Project oversight, task assignment, workforce allocation, and progress tracking'),
(3, 'Site Engineer', 'On-site execution, task updates, attendance recording, and material requests'),
(4, 'Safety Officer', 'Site safety inspections, violation logging, and incident management'),
(5, 'Inventory Manager', 'Warehouse inventory, material receipts, issues, and stock level control'),
(6, 'Equipment Manager', 'Heavy machinery and equipment tracking, service scheduling, and maintenance'),
(7, 'Contractor', 'Subcontractor partner managing assigned workforce and tasks'),
(8, 'Worker', 'Field worker viewing personal assignments, attendance, and reporting safety concerns');

-- 2. Restore Role-Permission Mappings
-- Role 1: Administrator (All 46 permissions)
INSERT INTO role_permissions (role_id, permission_id)
SELECT 1, permission_id FROM permissions;

-- Role 2: Project Manager
INSERT INTO role_permissions (role_id, permission_id) VALUES
(2, 10), (2, 11), (2, 12), (2, 13), -- PROJECT
(2, 14), (2, 15), (2, 16), (2, 17), -- WORKFORCE
(2, 18), (2, 19), (2, 20), (2, 21), -- TASK
(2, 22),                            -- ATTENDANCE VIEW
(2, 25),                            -- INVENTORY VIEW
(2, 30),                            -- EQUIPMENT VIEW
(2, 36), (2, 38), (2, 41), (2, 42), -- SAFETY & INCIDENTS
(2, 45), (2, 46);                   -- REPORTS, NOTIFICATIONS

-- Role 3: Site Engineer
INSERT INTO role_permissions (role_id, permission_id) VALUES
(3, 10),                            -- PROJECT VIEW
(3, 14),                            -- WORKFORCE VIEW
(3, 18), (3, 20), (3, 21),          -- TASK VIEW, UPDATE, ASSIGN
(3, 22), (3, 23), (3, 24),          -- ATTENDANCE VIEW, RECORD, CORRECT
(3, 25), (3, 29),                   -- INVENTORY VIEW, ISSUE
(3, 30),                            -- EQUIPMENT VIEW
(3, 38), (3, 41), (3, 42),          -- VIOLATION VIEW, INCIDENT VIEW, INCIDENT CREATE
(3, 46);                            -- NOTIFICATIONS

-- Role 4: Safety Officer
INSERT INTO role_permissions (role_id, permission_id) VALUES
(4, 10),                            -- PROJECT VIEW
(4, 14),                            -- WORKFORCE VIEW
(4, 36), (4, 37),                   -- SAFETY INSPECTIONS
(4, 38), (4, 39), (4, 40),          -- SAFETY VIOLATIONS
(4, 41), (4, 42), (4, 43),          -- INCIDENTS
(4, 45), (4, 46);                   -- REPORTS, NOTIFICATIONS

-- Role 5: Inventory Manager
INSERT INTO role_permissions (role_id, permission_id) VALUES
(5, 10),                            -- PROJECT VIEW
(5, 25), (5, 26), (5, 27), (5, 28), (5, 29), -- INVENTORY
(5, 45), (5, 46);                   -- REPORTS, NOTIFICATIONS

-- Role 6: Equipment Manager
INSERT INTO role_permissions (role_id, permission_id) VALUES
(6, 10),                            -- PROJECT VIEW
(6, 30), (6, 31), (6, 32),          -- EQUIPMENT
(6, 33), (6, 34), (6, 35),          -- MAINTENANCE
(6, 45), (6, 46);                   -- REPORTS, NOTIFICATIONS

-- Role 7: Contractor
INSERT INTO role_permissions (role_id, permission_id) VALUES
(7, 10),                            -- PROJECT VIEW
(7, 14),                            -- WORKFORCE VIEW
(7, 18), (7, 20),                   -- TASK VIEW & UPDATE
(7, 22),                            -- ATTENDANCE VIEW
(7, 42),                            -- INCIDENT CREATE
(7, 46);                            -- NOTIFICATIONS

-- Role 8: Worker
INSERT INTO role_permissions (role_id, permission_id) VALUES
(8, 18), (8, 20),                   -- TASK VIEW & UPDATE
(8, 22),                            -- ATTENDANCE VIEW
(8, 39),                            -- SAFETY_VIOLATION_CREATE
(8, 42),                            -- INCIDENT CREATE
(8, 46);                            -- NOTIFICATIONS

-- 3. Restore Exactly 1 Demo User per Role (8 Demo Users)
DELETE FROM users;

INSERT INTO users (user_id, username, email, password_hash, salt, full_name, role_id, is_active, must_change_password) VALUES
(1, 'admin', 'admin@csims.local', '$2a$10$VZqzILhvEL8Sztmm/omwKOUth45hinpT6Hxut7ba1ACIw2FGgekZC', '', 'System Administrator', 1, 1, 0),
(2, 'pm_sarah', 'sarah.pm@csims.local', '$2a$10$vPGTicCZZS9mbNqG3CJWZOq6P.rDDNplz/iCnAX2Z0/LHB42Rwc0.', '', 'Sarah Jenkins (PM)', 2, 1, 0),
(3, 'site_eng', 'eng@csims.local', '$2a$10$ammJLjaBp5Rne/U4atuEy.FoEd/auBRjZFHT024n9RDM5sZhfFq.e', '', 'Dave Miller (Site Eng)', 3, 1, 0),
(4, 'safety_dan', 'safety@csims.local', '$2a$10$v3sQeCgbFwOvxTocDGBsQuSEItuzbxXqcea2paXoAaEScUSzKnFvi', '', 'Dan Mitchell (Safety)', 4, 1, 0),
(5, 'inv_maria', 'inventory@csims.local', '$2a$10$Lt7oNDvaqqUIY1o3qwqPu.5AXj4LibqTyEJt8CqtiporUYVRyL6cS', '', 'Maria Santos (Inventory)', 5, 1, 0),
(6, 'equip_sam', 'equipment@csims.local', '$2a$10$LuCRep.J8/2EBk7VrBn8jeGMQ20nzmCJjMXCY6t63UFsnRy7rNihm', '', 'Sam Taylor (Equipment)', 6, 1, 0),
(7, 'contractor_apex', 'contractor@csims.local', '$2a$10$Dv01AQE3S23YaHpHwxnJyuqOg6h.KXgKFARb4nxjfG/dMBoBlQPQe', '', 'Apex Builders Partner', 7, 1, 0),
(8, 'worker_bill', 'worker@csims.local', '$2a$10$LSBLgqqs6v.Z9d/Dp32YZur7C0He5OtHb.XND4ALP2fo49OQGeeQi', '', 'Bill Miller (Site Worker)', 8, 1, 0);

-- 4. Projects (Owned / Overseen by Project Manager - user_id 2)
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

-- 5. Contractor & Workers (Contractor Apex Builders - user_id 7)
INSERT INTO contractors (contractor_id, company_name, contact_person, email, phone, address) VALUES
(1, 'Apex Structural Builders', 'David Ross', 'contractor@csims.local', '555-0199', '100 Industrial Pkwy, Sector 4')
ON DUPLICATE KEY UPDATE company_name = VALUES(company_name);

INSERT INTO workers (worker_id, full_name, email, phone, trade, contractor_id, status, joined_date) VALUES
(1, 'Bill Miller (Site Worker)', 'worker@csims.local', '555-0101', 'Structural Carpentry', 1, 'ACTIVE', '2026-01-20'),
(2, 'Carlos Santos', 'carlos.s@apexbuilders.com', '555-0102', 'Masonry & Concrete', 1, 'ACTIVE', '2026-01-22'),
(3, 'David Chen', 'david.c@apexbuilders.com', '555-0103', 'Heavy Equipment Operator', 1, 'ACTIVE', '2026-02-01')
ON DUPLICATE KEY UPDATE full_name = VALUES(full_name), email = VALUES(email);

INSERT IGNORE INTO project_workers (project_id, worker_id, role_in_project, assigned_date) VALUES
(1, 1, 'Lead Carpenter', '2026-01-25'),
(1, 2, 'Senior Mason', '2026-01-25'),
(2, 3, 'Excavator Operator', '2026-03-05');

-- 6. Tasks (Assigned and managed by Site Engineer - user_id 3 & Worker - worker_id 1)
DELETE FROM tasks;
INSERT INTO tasks (task_id, project_id, title, description, priority, status, completion_percent, assigned_worker_id, start_date, due_date) VALUES
(1, 1, 'Level 4 Slab Formwork', 'Assemble timber and aluminium formwork deck for fourth-floor concrete pour.', 'HIGH', 'Completed', 100, 1, '2026-10-01', '2026-10-10'),
(2, 1, 'Rebar Cage Tying Section B', 'Tie vertical reinforcement steel rebars as per structural engineering drawings.', 'URGENT', 'In Progress', 70, 1, '2026-10-12', '2026-10-25'),
(3, 1, 'Perimeter Safety Netting Inspection', 'Inspect safety guardrails and catch netting on exposed building perimeter.', 'HIGH', 'In Progress', 50, 1, '2026-10-05', '2026-10-20'),
(4, 2, 'Deep Pit Excavation Zone 3', 'Bulk excavation for lift core shaft down to -8.5m foundation datum.', 'URGENT', 'In Progress', 40, 3, '2026-10-10', '2026-10-28'),
(5, 2, 'Raft Slab Reinforcement Layout', 'Position bottom rebar mat on blinding concrete.', 'MEDIUM', 'Created', 0, NULL, '2026-10-25', '2026-11-05'),
(6, 3, 'Site Mobilization & Traffic Diversion', 'Set up warning barricades, high-visibility signage, and temporary detour lane.', 'LOW', 'Created', 0, NULL, '2026-11-01', '2026-11-10');

-- 7. Attendance Records (Recorded by Site Engineer - user_id 3)
DELETE FROM attendance;
INSERT INTO attendance (worker_id, project_id, attendance_date, status, remarks, recorded_by) VALUES
(1, 1, '2026-10-18', 'Present', 'Shift 07:00 - 16:00 on Level 4', 3),
(1, 1, '2026-10-19', 'Present', 'Shift 07:00 - 16:00', 3),
(1, 1, '2026-10-20', 'Present', 'Formwork assembly', 3),
(1, 1, '2026-10-21', 'Present', 'Full day worked', 3),
(1, 1, '2026-10-22', 'Present', 'Morning check-in OK', 3),
(2, 1, '2026-10-22', 'Present', 'Concrete pump pouring', 3),
(3, 2, '2026-10-22', 'Present', 'Operating CAT Excavator', 3);

-- 8. Materials Catalog & Transactions (Managed by Inventory Manager - user_id 5)
DELETE FROM materials;
INSERT INTO materials (material_id, material_code, name, category, unit, quantity_on_hand, min_stock, max_stock, unit_price, location) VALUES
(1, 'MAT-CEM-01', 'Portland Cement Type I', 'Binding Materials', 'Bags (50kg)', 45.00, 100.00, 1000.00, 12.50, 'Warehouse Bay A-1'),
(2, 'MAT-STE-02', 'Grade 60 Steel Rebar 16mm', 'Reinforcement', 'Tons', 140.00, 30.00, 300.00, 850.00, 'Open Yard B'),
(3, 'MAT-AGG-03', 'Coarse Crushed Aggregate 20mm', 'Aggregates', 'Cubic Metres', 280.00, 80.00, 600.00, 45.00, 'Aggregate Stockpile 1'),
(4, 'MAT-BRK-04', 'Modular Solid Clay Bricks', 'Masonry', 'Thousand Units', 25.00, 10.00, 50.00, 320.00, 'Pallet Storage C');

DELETE FROM inventory_transactions;
INSERT INTO inventory_transactions (transaction_id, material_id, transaction_type, quantity, project_id, reference_no, remarks, performed_by) VALUES
(1, 1, 'RECEIPT', 500.00, NULL, 'PO-2026-101', 'Procured from Holcim Ltd.', 5),
(2, 1, 'ISSUE', 455.00, 1, 'REQ-MTR-01', 'Issued for Level 4 concrete slab pour', 5),
(3, 2, 'RECEIPT', 150.00, NULL, 'PO-2026-102', 'ArcelorMittal steel delivery', 5),
(4, 2, 'ISSUE', 10.00, 1, 'REQ-MTR-02', 'Rebar tying on Section B', 5);

-- 9. Equipment & Maintenance (Managed by Equipment Manager - user_id 6)
DELETE FROM equipment;
INSERT INTO equipment (equipment_id, equipment_code, name, category, status, project_id, purchase_date, last_service_date, next_service_due) VALUES
(1, 'EQ-CRN-01', 'Potain Tower Crane 50m Jib', 'Cranes', 'In Use', 1, '2024-01-10', '2026-08-10', '2026-12-15'),
(2, 'EQ-EXC-02', 'CAT 320D Hydraulic Excavator', 'Earthmoving', 'Under Maintenance', 2, '2024-03-15', '2026-05-15', '2026-10-22'),
(3, 'EQ-MIX-03', 'Schwing Stetter Concrete Transit Mixer', 'Concrete', 'Available', 1, '2024-06-20', '2026-09-01', '2026-11-20');

DELETE FROM maintenance;
INSERT INTO maintenance (maintenance_id, equipment_id, maintenance_type, status, scheduled_date, completed_date, cost, technician, notes) VALUES
(1, 2, '500-Hour Hydraulic Fluid & Filter Service', 'Due', '2026-10-22', NULL, 650.00, 'CAT Field Technician', '500-hour hydraulic fluid change, filter replacement, and track tensioning.');

-- 10. Safety Inspections & Violations (Managed by Safety Officer - user_id 4)
DELETE FROM safety_inspections;
INSERT INTO safety_inspections (inspection_id, project_id, inspector_id, inspection_date, ppe_score, worker_safety_score, equipment_score, work_area_score, emergency_score, overall_score, remarks) VALUES
(1, 1, 4, '2026-10-20', 90, 85, 80, 85, 90, 86.00, 'Overall good compliance. Scaffolding tag inspection verified.');

DELETE FROM safety_violations;
INSERT INTO safety_violations (violation_id, inspection_id, project_id, worker_id, severity, description, corrective_action, status, resolution_date, reported_by) VALUES
(1, 1, 1, 2, 'Major', 'Perimeter edge guardrail removed without temporary harness tie-off point on Level 4.', 'Reinstall double-height guardrails and inspect lanyard anchor rings before work resumes.', 'OPEN', NULL, 4);

-- 11. Site Incidents (Reported by Site Eng - user_id 3, Investigated by Safety Officer - user_id 4)
DELETE FROM incidents;
INSERT INTO incidents (incident_id, project_id, incident_date, severity, title, description, investigator_id, actions_taken, status, reported_by) VALUES
(1, 2, '2026-10-21 14:30:00', 'Moderate', 'Hydraulic hose rupture on CAT 320D during trench digging', 'High-pressure hydraulic return line burst. Equipment safely turned off, spill containment pads applied.', 4, 'Replaced hydraulic line with heavy-duty reinforced hose. Machine scheduled for full inspection.', 'Investigating', 3);

-- 12. Account Requests (Reviewed by Administrator - user_id 1)
DELETE FROM account_requests;
INSERT INTO account_requests (request_id, request_type, full_name, email, username, requested_role_id, reason, status, reviewed_by) VALUES
(1, 'REGISTRATION', 'Daniel Craig', 'dcraig@construct.com', 'dcraig', 3, 'Newly appointed Junior Site Engineer for Metro Rail project.', 'PENDING', NULL),
(2, 'PASSWORD_RESET', 'Elena Rostova', 'inventory@csims.local', 'inventory', 5, 'Forgot credentials after returning from field leave.', 'PENDING', NULL);

-- 13. System Notifications for all 8 users
DELETE FROM notifications;
INSERT INTO notifications (notification_id, user_id, title, message, type, is_read) VALUES
(1, 1, 'Pending Account Request', 'Daniel Craig has requested access as Site Engineer.', 'REGISTRATION_REQUEST', 0),
(2, 2, 'Project Milestone Update', 'Riverside Apartments reached 62% overall completion.', 'PROJECT_UPDATE', 0),
(3, 3, 'Task Updated: Level 4 Formwork', 'Bill Miller reported formwork completed on Level 4.', 'TASK_UPDATE', 0),
(4, 4, 'Safety Audit Due', 'Bi-weekly safety audit scheduled for City Mall Foundation Work.', 'SAFETY_ALERT', 0),
(5, 5, 'Low Stock Alert: Portland Cement', 'Portland Cement stock is at 45 Bags, below minimum threshold of 100 Bags.', 'LOW_STOCK', 0),
(6, 6, 'Maintenance Due Today: CAT 320D', 'Preventative maintenance for CAT 320D Excavator is due today.', 'MAINTENANCE_DUE', 0),
(7, 7, 'Subcontractor Task Assigned', 'Apex Builders assigned to Level 4 slab reinforcement.', 'TASK_ASSIGNMENT', 0),
(8, 8, 'New Task Assigned: Perimeter Netting', 'You have been assigned to inspect perimeter safety netting on Project Riverside.', 'TASK_ASSIGNMENT', 0);

-- 14. Immutable Audit Logs
DELETE FROM audit_logs;
INSERT INTO audit_logs (audit_id, user_id, username, role, action, module, entity_type, entity_id, old_value, new_value, status, description) VALUES
(1, 1, 'admin', 'Administrator', 'SYSTEM_MIGRATION', 'SYSTEM', 'DATABASE', 'V4', 'v3.0.0', 'v4.0.0', 'SUCCESS', 'Restored 8 enterprise roles and mapped role-permission policies.'),
(2, 2, 'pm_sarah', 'Project Manager', 'PROJECT_UPDATE', 'PROJECT', 'PROJECT', '1', '60%', '62%', 'SUCCESS', 'Updated Riverside Apartments progress to 62%.'),
(3, 3, 'site_eng', 'Site Engineer', 'ATTENDANCE_RECORD', 'ATTENDANCE', 'WORKER', '1', NULL, 'Present', 'SUCCESS', 'Marked morning attendance for Bill Miller.'),
(4, 4, 'safety_dan', 'Safety Officer', 'SAFETY_INSPECTION_CREATE', 'SAFETY', 'INSPECTION', '1', NULL, 'Score: 86%', 'SUCCESS', 'Submitted safety audit for Riverside Apartments.'),
(5, 5, 'inv_maria', 'Inventory Manager', 'STOCK_ISSUE', 'INVENTORY', 'MATERIAL', '1', '500', '45', 'SUCCESS', 'Issued 455 bags of Portland Cement for concrete pour.'),
(6, 6, 'equip_sam', 'Equipment Manager', 'MAINTENANCE_SCHEDULE', 'EQUIPMENT', 'MAINTENANCE', '1', NULL, 'Due', 'SUCCESS', 'Scheduled 500-hour service on CAT 320D Excavator.');

SET FOREIGN_KEY_CHECKS = 1;
