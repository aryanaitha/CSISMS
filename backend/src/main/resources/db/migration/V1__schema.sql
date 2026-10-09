-- =====================================================================
-- Construction Site Intelligence & Safety Management System (CSIMS)
-- V1 Schema Migration
-- =====================================================================

-- 1. Roles
CREATE TABLE IF NOT EXISTS roles (
    role_id INT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
) ENGINE=InnoDB;

-- 2. Permissions
CREATE TABLE IF NOT EXISTS permissions (
    permission_id INT AUTO_INCREMENT PRIMARY KEY,
    permission_code VARCHAR(64) NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL,
    module VARCHAR(50) NOT NULL
) ENGINE=InnoDB;

-- 3. Role - Permissions mapping
CREATE TABLE IF NOT EXISTS role_permissions (
    role_id INT NOT NULL,
    permission_id INT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_rp_role FOREIGN KEY (role_id) REFERENCES roles (role_id) ON DELETE CASCADE,
    CONSTRAINT fk_rp_perm FOREIGN KEY (permission_id) REFERENCES permissions (permission_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 4. Users
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    salt VARCHAR(255) DEFAULT '',
    full_name VARCHAR(100) NOT NULL,
    role_id INT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    failed_attempts INT NOT NULL DEFAULT 0,
    locked_until DATETIME DEFAULT NULL,
    last_login DATETIME DEFAULT NULL,
    must_change_password BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles (role_id)
) ENGINE=InnoDB;

-- 5. Account Requests
CREATE TABLE IF NOT EXISTS account_requests (
    request_id INT AUTO_INCREMENT PRIMARY KEY,
    request_type VARCHAR(20) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    username VARCHAR(50) NOT NULL,
    requested_role_id INT DEFAULT NULL,
    reason TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reviewed_by INT DEFAULT NULL,
    review_note TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_at DATETIME DEFAULT NULL,
    CONSTRAINT fk_ar_role FOREIGN KEY (requested_role_id) REFERENCES roles (role_id) ON DELETE SET NULL,
    CONSTRAINT fk_ar_reviewer FOREIGN KEY (reviewed_by) REFERENCES users (user_id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 6. Contractors
CREATE TABLE IF NOT EXISTS contractors (
    contractor_id INT AUTO_INCREMENT PRIMARY KEY,
    company_name VARCHAR(100) NOT NULL,
    contact_person VARCHAR(100),
    email VARCHAR(100),
    phone VARCHAR(30),
    address TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 7. Projects
CREATE TABLE IF NOT EXISTS projects (
    project_id INT AUTO_INCREMENT PRIMARY KEY,
    project_name VARCHAR(150) NOT NULL,
    description TEXT,
    location VARCHAR(150) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE DEFAULT NULL,
    budget DECIMAL(15,2) DEFAULT 0.00,
    status VARCHAR(30) NOT NULL DEFAULT 'Planning',
    progress_percent INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 8. Workers
CREATE TABLE IF NOT EXISTS workers (
    worker_id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(30),
    trade VARCHAR(50) NOT NULL,
    contractor_id INT DEFAULT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    joined_date DATE NOT NULL,
    CONSTRAINT fk_workers_contractor FOREIGN KEY (contractor_id) REFERENCES contractors (contractor_id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 9. Project Workers
CREATE TABLE IF NOT EXISTS project_workers (
    project_id INT NOT NULL,
    worker_id INT NOT NULL,
    role_in_project VARCHAR(50) NOT NULL DEFAULT 'Site Worker',
    assigned_date DATE NOT NULL,
    PRIMARY KEY (project_id, worker_id),
    CONSTRAINT fk_pw_project FOREIGN KEY (project_id) REFERENCES projects (project_id) ON DELETE CASCADE,
    CONSTRAINT fk_pw_worker FOREIGN KEY (worker_id) REFERENCES workers (worker_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 10. Tasks
CREATE TABLE IF NOT EXISTS tasks (
    task_id INT AUTO_INCREMENT PRIMARY KEY,
    project_id INT NOT NULL,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    status VARCHAR(30) NOT NULL DEFAULT 'Created',
    completion_percent INT NOT NULL DEFAULT 0,
    assigned_worker_id INT DEFAULT NULL,
    start_date DATE DEFAULT NULL,
    due_date DATE DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_tasks_project FOREIGN KEY (project_id) REFERENCES projects (project_id) ON DELETE CASCADE,
    CONSTRAINT fk_tasks_worker FOREIGN KEY (assigned_worker_id) REFERENCES workers (worker_id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 11. Attendance
CREATE TABLE IF NOT EXISTS attendance (
    attendance_id INT AUTO_INCREMENT PRIMARY KEY,
    worker_id INT NOT NULL,
    project_id INT NOT NULL,
    attendance_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'Present',
    remarks VARCHAR(255),
    recorded_by INT DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_attendance (worker_id, project_id, attendance_date),
    CONSTRAINT fk_att_worker FOREIGN KEY (worker_id) REFERENCES workers (worker_id) ON DELETE CASCADE,
    CONSTRAINT fk_att_project FOREIGN KEY (project_id) REFERENCES projects (project_id) ON DELETE CASCADE,
    CONSTRAINT fk_att_recorder FOREIGN KEY (recorded_by) REFERENCES users (user_id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 12. Materials
CREATE TABLE IF NOT EXISTS materials (
    material_id INT AUTO_INCREMENT PRIMARY KEY,
    material_code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    unit VARCHAR(20) NOT NULL,
    quantity_on_hand DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    min_stock DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    max_stock DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    unit_price DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    location VARCHAR(100),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 13. Inventory Transactions
CREATE TABLE IF NOT EXISTS inventory_transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    material_id INT NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    quantity DECIMAL(12,2) NOT NULL,
    project_id INT DEFAULT NULL,
    reference_no VARCHAR(50),
    remarks VARCHAR(255),
    performed_by INT DEFAULT NULL,
    transaction_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_it_material FOREIGN KEY (material_id) REFERENCES materials (material_id) ON DELETE RESTRICT,
    CONSTRAINT fk_it_project FOREIGN KEY (project_id) REFERENCES projects (project_id) ON DELETE SET NULL,
    CONSTRAINT fk_it_user FOREIGN KEY (performed_by) REFERENCES users (user_id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 14. Equipment
CREATE TABLE IF NOT EXISTS equipment (
    equipment_id INT AUTO_INCREMENT PRIMARY KEY,
    equipment_code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'Available',
    project_id INT DEFAULT NULL,
    purchase_date DATE DEFAULT NULL,
    last_service_date DATE DEFAULT NULL,
    next_service_due DATE DEFAULT NULL,
    CONSTRAINT fk_eq_project FOREIGN KEY (project_id) REFERENCES projects (project_id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 15. Maintenance
CREATE TABLE IF NOT EXISTS maintenance (
    maintenance_id INT AUTO_INCREMENT PRIMARY KEY,
    equipment_id INT NOT NULL,
    maintenance_type VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'Due',
    scheduled_date DATE DEFAULT NULL,
    completed_date DATE DEFAULT NULL,
    cost DECIMAL(12,2) DEFAULT 0.00,
    technician VARCHAR(100),
    notes TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_maint_equipment FOREIGN KEY (equipment_id) REFERENCES equipment (equipment_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 16. Safety Inspections
CREATE TABLE IF NOT EXISTS safety_inspections (
    inspection_id INT AUTO_INCREMENT PRIMARY KEY,
    project_id INT NOT NULL,
    inspector_id INT NOT NULL,
    inspection_date DATE NOT NULL,
    ppe_score INT NOT NULL DEFAULT 100,
    worker_safety_score INT NOT NULL DEFAULT 100,
    equipment_score INT NOT NULL DEFAULT 100,
    work_area_score INT NOT NULL DEFAULT 100,
    emergency_score INT NOT NULL DEFAULT 100,
    overall_score DECIMAL(5,2) NOT NULL DEFAULT 100.00,
    remarks TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_si_project FOREIGN KEY (project_id) REFERENCES projects (project_id) ON DELETE CASCADE,
    CONSTRAINT fk_si_inspector FOREIGN KEY (inspector_id) REFERENCES users (user_id)
) ENGINE=InnoDB;

-- 17. Safety Violations
CREATE TABLE IF NOT EXISTS safety_violations (
    violation_id INT AUTO_INCREMENT PRIMARY KEY,
    inspection_id INT DEFAULT NULL,
    project_id INT NOT NULL,
    worker_id INT DEFAULT NULL,
    severity VARCHAR(20) NOT NULL,
    description TEXT NOT NULL,
    corrective_action TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    resolution_date DATE DEFAULT NULL,
    reported_by INT DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sv_inspection FOREIGN KEY (inspection_id) REFERENCES safety_inspections (inspection_id) ON DELETE SET NULL,
    CONSTRAINT fk_sv_project FOREIGN KEY (project_id) REFERENCES projects (project_id) ON DELETE CASCADE,
    CONSTRAINT fk_sv_worker FOREIGN KEY (worker_id) REFERENCES workers (worker_id) ON DELETE SET NULL,
    CONSTRAINT fk_sv_reporter FOREIGN KEY (reported_by) REFERENCES users (user_id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 18. Incidents
CREATE TABLE IF NOT EXISTS incidents (
    incident_id INT AUTO_INCREMENT PRIMARY KEY,
    project_id INT NOT NULL,
    incident_date DATETIME NOT NULL,
    severity VARCHAR(20) NOT NULL,
    title VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,
    investigator_id INT DEFAULT NULL,
    actions_taken TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'Reported',
    reported_by INT DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_inc_project FOREIGN KEY (project_id) REFERENCES projects (project_id) ON DELETE CASCADE,
    CONSTRAINT fk_inc_investigator FOREIGN KEY (investigator_id) REFERENCES users (user_id) ON DELETE SET NULL,
    CONSTRAINT fk_inc_reporter FOREIGN KEY (reported_by) REFERENCES users (user_id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 19. Notifications
CREATE TABLE IF NOT EXISTS notifications (
    notification_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT DEFAULT NULL,
    title VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 20. Audit Logs
CREATE TABLE IF NOT EXISTS audit_logs (
    audit_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT DEFAULT NULL,
    username VARCHAR(50),
    role VARCHAR(50),
    action VARCHAR(100) NOT NULL,
    module VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50),
    entity_id VARCHAR(50),
    old_value TEXT,
    new_value TEXT,
    timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
    description TEXT,
    INDEX idx_audit_user (user_id),
    INDEX idx_audit_module (module),
    INDEX idx_audit_action (action),
    INDEX idx_audit_ts (timestamp)
) ENGINE=InnoDB;
