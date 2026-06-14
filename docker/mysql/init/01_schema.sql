CREATE DATABASE IF NOT EXISTS home_tools DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE home_tools;

CREATE TABLE IF NOT EXISTS category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    parent_id BIGINT,
    level INT NOT NULL,
    sort_order INT DEFAULT 0,
    description TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_parent_id (parent_id),
    INDEX idx_level (level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS maintenance_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    default_cycle_days INT NOT NULL DEFAULT 90,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS tool (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(200) NOT NULL,
    model VARCHAR(100),
    brand VARCHAR(100),
    category_id BIGINT NOT NULL,
    sub_category_id BIGINT,
    purpose TEXT,
    specification TEXT,
    location VARCHAR(200),
    purchase_date DATE,
    price DECIMAL(10,2) DEFAULT 0.00,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    last_maintenance_date DATE,
    next_maintenance_date DATE,
    maintenance_cycle_days INT DEFAULT 90,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category (category_id),
    INDEX idx_status (status),
    INDEX idx_next_maintenance (next_maintenance_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS usage_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tool_id BIGINT NOT NULL,
    use_date DATE NOT NULL,
    duration_minutes INT DEFAULT 0,
    scenario VARCHAR(500),
    operator VARCHAR(50),
    remarks TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_tool_id (tool_id),
    INDEX idx_use_date (use_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS maintenance_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tool_id BIGINT NOT NULL,
    maintenance_type VARCHAR(20) NOT NULL,
    maintenance_date DATE NOT NULL,
    operator VARCHAR(50),
    cost DECIMAL(10,2) DEFAULT 0.00,
    parts_replaced VARCHAR(500),
    description TEXT,
    next_maintenance_date DATE,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_tool_id (tool_id),
    INDEX idx_maintenance_date (maintenance_date),
    INDEX idx_maintenance_type (maintenance_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS inventory (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    inventory_date DATE NOT NULL,
    total_tools INT DEFAULT 0,
    checked_tools INT DEFAULT 0,
    available_tools INT DEFAULT 0,
    loaned_tools INT DEFAULT 0,
    maintenance_tools INT DEFAULT 0,
    lost_tools INT DEFAULT 0,
    operator VARCHAR(50),
    remarks TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_inventory_date (inventory_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS inventory_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    inventory_id BIGINT NOT NULL,
    tool_id BIGINT NOT NULL,
    expected_status VARCHAR(20),
    actual_status VARCHAR(20),
    checked TINYINT DEFAULT 0,
    remarks TEXT,
    INDEX idx_inventory_id (inventory_id),
    INDEX idx_tool_id (tool_id),
    UNIQUE KEY uk_inventory_tool (inventory_id, tool_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
