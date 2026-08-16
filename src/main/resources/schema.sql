-- Categories table
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(1000),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- Suppliers table
CREATE TABLE IF NOT EXISTS suppliers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    contact_person VARCHAR(255) NOT NULL,
    phone VARCHAR(50) NOT NULL,
    email VARCHAR(255),
    address VARCHAR(500),
    active BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- Medications table
CREATE TABLE IF NOT EXISTS medications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(1000),
    quantity INTEGER NOT NULL,
    price DOUBLE PRECISION NOT NULL,
    expiration_date DATE,
    low_stock_threshold INTEGER DEFAULT 10,
    manufacturer VARCHAR(255),
    batch_number VARCHAR(100),
    type VARCHAR(50),
    is_deleted BOOLEAN DEFAULT FALSE,
    category_id BIGINT,
    supplier_id BIGINT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id),
    FOREIGN KEY (supplier_id) REFERENCES suppliers(id)
);

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP
);

-- Sales table
CREATE TABLE IF NOT EXISTS sales (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sale_date DATE NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(10,2) NOT NULL,
    cost_price NUMERIC(10,2),
    total_price NUMERIC(10,2) NOT NULL,
    medication_id BIGINT NOT NULL,
    created_at TIMESTAMP,
    FOREIGN KEY (medication_id) REFERENCES medications(id)
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_sales_date ON sales(sale_date);
CREATE INDEX IF NOT EXISTS idx_sales_medication ON sales(medication_id);
CREATE INDEX IF NOT EXISTS idx_medication_name ON medications(name);
CREATE INDEX IF NOT EXISTS idx_medication_type ON medications(type);
CREATE INDEX IF NOT EXISTS idx_medication_category ON medications(category_id);
CREATE INDEX IF NOT EXISTS idx_medication_supplier ON medications(supplier_id);
CREATE INDEX IF NOT EXISTS idx_category_name ON categories(name);
CREATE INDEX IF NOT EXISTS idx_supplier_name ON suppliers(name);