-- Create database if not exists
CREATE DATABASE IF NOT EXISTS pypygo;
USE pypygo;

-- Products table
CREATE TABLE IF NOT EXISTS products (
    id VARCHAR(255) NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP
);

-- Inventories table
CREATE TABLE IF NOT EXISTS inventories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id VARCHAR(255) NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    reserved_quantity INT NOT NULL DEFAULT 0,
    minimum_stock INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    UNIQUE KEY unique_product_inventory (product_id)
);

-- Inventory transactions table
CREATE TABLE IF NOT EXISTS inventory_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id VARCHAR(255) NOT NULL,
    type ENUM('STOCK_IN', 'STOCK_OUT', 'RESERVE', 'RELEASE', 'CONFIRM', 'ADJUSTMENT') NOT NULL,
    quantity INT NOT NULL,
    before_quantity INT NOT NULL,
    after_quantity INT NOT NULL,
    reason VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    INDEX idx_product_id (product_id),
    INDEX idx_created_at (created_at),
    INDEX idx_type (type)
);