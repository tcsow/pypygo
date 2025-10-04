-- Clear existing data (for fresh start)
USE pypygo;

DELETE FROM inventory_transactions;
DELETE FROM inventories;
DELETE FROM products;

-- Insert sample products
INSERT INTO products (id, name, description, price, created_at) VALUES
('prod-001', 'Laptop Computer', 'High-performance laptop for business use', 999.99, NOW()),
('prod-002', 'Wireless Mouse', 'Ergonomic wireless mouse with long battery life', 29.99, NOW()),
('prod-003', 'Mechanical Keyboard', 'RGB mechanical keyboard for gaming', 149.99, NOW()),
('prod-004', 'Monitor 24"', '24-inch Full HD monitor with IPS panel', 199.99, NOW()),
('prod-005', 'USB-C Hub', 'Multi-port USB-C hub with HDMI and USB 3.0', 79.99, NOW());

-- Insert corresponding inventory records
INSERT INTO inventories (product_id, quantity, reserved_quantity, minimum_stock, created_at) VALUES
('prod-001', 50, 0, 10, NOW()),
('prod-002', 200, 5, 20, NOW()),
('prod-003', 75, 0, 15, NOW()),
('prod-004', 30, 2, 8, NOW()),
('prod-005', 100, 0, 25, NOW());

-- Insert sample inventory transactions
INSERT INTO inventory_transactions (product_id, type, quantity, before_quantity, after_quantity, reason, created_at) VALUES
('prod-001', 'STOCK_IN', 50, 0, 50, 'Initial stock', NOW()),
('prod-002', 'STOCK_IN', 200, 0, 200, 'Initial stock', NOW()),
('prod-002', 'RESERVE', 5, 5, 5, 'Reserved for pending orders', NOW()),
('prod-003', 'STOCK_IN', 75, 0, 75, 'Initial stock', NOW()),
('prod-004', 'STOCK_IN', 30, 0, 30, 'Initial stock', NOW()),
('prod-004', 'RESERVE', 2, 2, 2, 'Reserved for VIP customer', NOW()),
('prod-005', 'STOCK_IN', 100, 0, 100, 'Initial stock', NOW());