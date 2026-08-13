-- Create database
CREATE DATABASE IF NOT EXISTS pharmacy_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create user (adjust password as needed)
CREATE USER IF NOT EXISTS 'pharmacy_user'@'localhost' IDENTIFIED BY 'your_secure_password';
GRANT ALL PRIVILEGES ON pharmacy_db.* TO 'pharmacy_user'@'localhost';
FLUSH PRIVILEGES;

-- Use the database
USE pharmacy_db;

-- Create tables (if not using JPA auto-ddl)
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(1000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS suppliers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    contact_person VARCHAR(255) NOT NULL,
    phone VARCHAR(50) NOT NULL,
    email VARCHAR(255),
    address VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS medications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    generic_name VARCHAR(255) NOT NULL,
    manufacturer VARCHAR(255) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    reorder_level INT NOT NULL DEFAULT 10,
    expiry_date DATETIME,
    type VARCHAR(50) NOT NULL,
    description VARCHAR(1000),
    batch_number VARCHAR(100),
    category_id BIGINT,
    supplier_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE SET NULL,
    CHECK (price >= 0),
    CHECK (quantity >= 0),
    CHECK (reorder_level >= 0)
);

-- Create indexes for performance
CREATE INDEX idx_medication_name ON medications(name);
CREATE INDEX idx_medication_type ON medications(type);
CREATE INDEX idx_medication_category ON medications(category_id);
CREATE INDEX idx_medication_supplier ON medications(supplier_id);
CREATE INDEX idx_medication_expiry ON medications(expiry_date);
CREATE INDEX idx_category_name ON categories(name);
CREATE INDEX idx_supplier_name ON suppliers(name);
CREATE INDEX idx_supplier_email ON suppliers(email);