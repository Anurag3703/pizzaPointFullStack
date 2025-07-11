-- Create discounts table with CHECK constraints for enum values
CREATE TABLE discounts (
                           id SERIAL PRIMARY KEY,
                           discount_code VARCHAR(255) UNIQUE NOT NULL,
                           discount_value DECIMAL(10, 2) NOT NULL,
                           minimum_order_amount DECIMAL(10, 2) NOT NULL,
                           discount_type VARCHAR(50) NOT NULL,
                           valid_from TIMESTAMP,
                           valid_until TIMESTAMP,
                           max_uses INTEGER,
                           current_uses INTEGER DEFAULT 0,
                           active BOOLEAN DEFAULT TRUE,
                           applicable_order_type VARCHAR(50),
                           CONSTRAINT chk_discount_value CHECK (discount_value > 0),
                           CONSTRAINT chk_min_order_amount CHECK (minimum_order_amount >= 3500), -- 3500 HUF minimum
                           CONSTRAINT chk_discount_type CHECK (discount_type IN ('FIXED_AMOUNT', 'PERCENTAGE', 'FIRST_ORDER', 'SEASONAL', 'FREE_DELIVERY')),
                           CONSTRAINT chk_order_type CHECK (applicable_order_type IN ('DELIVERY', 'PICKUP', 'DINE_IN') OR applicable_order_type IS NULL),
                           CONSTRAINT chk_valid_dates CHECK (valid_until IS NULL OR valid_from IS NULL OR valid_until >= valid_from)
);

-- Create discount_usages table
CREATE TABLE discount_usages (
                                 id BIGSERIAL PRIMARY KEY,
                                 discount_id INTEGER NOT NULL,
                                 user_id BIGINT,
                                 user_phone VARCHAR(15) NOT NULL,
                                 order_id VARCHAR(255) NOT NULL,
                                 used_at TIMESTAMP NOT NULL,
                                 CONSTRAINT fk_discount FOREIGN KEY (discount_id) REFERENCES discounts(id),
                                 CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Add discount columns to orders table
ALTER TABLE orders
    ADD COLUMN discount_id INTEGER,
    ADD COLUMN discount_amount DECIMAL(10, 2) DEFAULT 0.00,
    ADD COLUMN package_fee DECIMAL(10, 2) DEFAULT 299.00,
    ADD CONSTRAINT fk_order_discount FOREIGN KEY (discount_id) REFERENCES discounts(id);

-- Create indexes for better performance
CREATE INDEX idx_discounts_code ON discounts(discount_code);
CREATE INDEX idx_discounts_valid ON discounts(valid_from, valid_until);
CREATE INDEX idx_discount_usages_user ON discount_usages(user_id, discount_id);