-- Savora Finance Tracker Database Schema
-- Create tables for the finance tracking application (MySQL compatible)

-- Categories table
CREATE TABLE IF NOT EXISTS categories (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Insert default categories
INSERT IGNORE INTO categories (name, is_default) VALUES
('Food & Dining', TRUE),
('Transportation', TRUE),
('Entertainment', TRUE),
('Bills & Utilities', TRUE),
('Shopping', TRUE),
('Healthcare', TRUE),
('Education', TRUE),
('Travel', TRUE),
('Salary', TRUE),
('Freelance', TRUE),
('Investment', TRUE),
('Gift', TRUE),
('Other', TRUE);

-- Transactions table
CREATE TABLE IF NOT EXISTS transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    amount DECIMAL(10,2) NOT NULL,
    date DATE NOT NULL,
    category_id INT NOT NULL,
    description TEXT,
    type ENUM('income', 'expense') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(category_id)
) ENGINE=InnoDB;

CREATE INDEX idx_transactions_date ON transactions(date);
CREATE INDEX idx_transactions_category ON transactions(category_id);
CREATE INDEX idx_transactions_type ON transactions(type);

-- Budgets table
CREATE TABLE IF NOT EXISTS budgets (
    budget_id INT AUTO_INCREMENT PRIMARY KEY,
    category_id INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    period ENUM('daily', 'weekly', 'monthly', 'yearly') DEFAULT 'monthly',
    start_date DATE NOT NULL,
    end_date DATE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(category_id)
) ENGINE=InnoDB;

CREATE INDEX idx_budgets_category_period ON budgets(category_id, period);
CREATE INDEX idx_budgets_start_date ON budgets(start_date);

-- Spending limits table
CREATE TABLE IF NOT EXISTS spending_limits (
    limit_id INT AUTO_INCREMENT PRIMARY KEY,
    category_id INT,
    limit_amount DECIMAL(10,2) NOT NULL,
    period ENUM('daily', 'weekly', 'monthly', 'yearly') DEFAULT 'monthly',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(category_id)
) ENGINE=InnoDB;

CREATE INDEX idx_spending_limits_category_period ON spending_limits(category_id, period);

-- Sample data for testing
INSERT INTO transactions (amount, date, category_id, description, type) VALUES
(2500.00, '2024-01-01', 9, 'Monthly salary', 'income'),
(150.00, '2024-01-02', 1, 'Grocery shopping', 'expense'),
(45.00, '2024-01-03', 2, 'Gas for car', 'expense'),
(25.00, '2024-01-04', 3, 'Movie tickets', 'expense'),
(120.00, '2024-01-05', 4, 'Electric bill', 'expense'),
(80.00, '2024-01-06', 1, 'Restaurant dinner', 'expense'),
(300.00, '2024-01-07', 10, 'Freelance project', 'income'),
(60.00, '2024-01-08', 2, 'Public transport', 'expense'),
(200.00, '2024-01-09', 5, 'Online shopping', 'expense'),
(90.00, '2024-01-10', 1, 'Weekly groceries', 'expense');

-- Sample budgets
INSERT INTO budgets (category_id, amount, period, start_date) VALUES
(1, 400.00, 'monthly', '2024-01-01'),
(2, 200.00, 'monthly', '2024-01-01'),
(3, 150.00, 'monthly', '2024-01-01'),
(4, 300.00, 'monthly', '2024-01-01'),
(5, 250.00, 'monthly', '2024-01-01');

-- Sample spending limits
INSERT INTO spending_limits (category_id, limit_amount, period) VALUES
(1, 500.00, 'monthly'),
(2, 300.00, 'monthly'),
(NULL, 2000.00, 'monthly'); -- Overall spending limit
