-- FILE: db/sql/v1.0.0/22_community_maintenance_billing.sql
-- Community Maintenance Billing & Double-Entry General Ledger

CREATE TABLE IF NOT EXISTS maintenance_plans (
    id BIGSERIAL PRIMARY KEY,
    community_id BIGINT NOT NULL,
    name VARCHAR(128) NOT NULL,
    description TEXT,
    calculation_type VARCHAR(32) NOT NULL,
    fixed_amount_per_flat NUMERIC(14,2) DEFAULT 0.00,
    rate_per_sq_ft NUMERIC(14,2) DEFAULT 0.00,
    billing_frequency VARCHAR(32) DEFAULT 'MONTHLY',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS flat_charge_assignments (
    id BIGSERIAL PRIMARY KEY,
    community_id BIGINT NOT NULL,
    flat_id BIGINT NOT NULL,
    flat_number VARCHAR(32) NOT NULL,
    tower VARCHAR(32) NOT NULL,
    flat_type VARCHAR(32) DEFAULT '2BHK',
    area_sq_ft NUMERIC(10,2) NOT NULL,
    owner_user_id BIGINT,
    tenant_user_id BIGINT,
    responsible_party VARCHAR(32) DEFAULT 'OWNER',
    plan_id BIGINT REFERENCES maintenance_plans(id),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_flat_assignment UNIQUE (community_id, flat_id)
);

CREATE TABLE IF NOT EXISTS maintenance_charge_rules (
    id BIGSERIAL PRIMARY KEY,
    community_id BIGINT NOT NULL,
    rule_name VARCHAR(128) NOT NULL,
    component_type VARCHAR(64) NOT NULL,
    calculation_type VARCHAR(32) NOT NULL,
    rate NUMERIC(14,2) NOT NULL,
    flat_type VARCHAR(32),
    tower VARCHAR(32),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS penalty_rule_configs (
    id BIGSERIAL PRIMARY KEY,
    community_id BIGINT NOT NULL UNIQUE,
    grace_period_days INT DEFAULT 10,
    penalty_type VARCHAR(32) DEFAULT 'PERCENTAGE_PER_MONTH',
    penalty_value NUMERIC(10,2) DEFAULT 2.00,
    max_penalty_cap NUMERIC(14,2) DEFAULT 5000.00,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS community_invoices (
    id BIGSERIAL PRIMARY KEY,
    community_id BIGINT NOT NULL,
    flat_id BIGINT NOT NULL,
    flat_number VARCHAR(32) NOT NULL,
    tower VARCHAR(32) NOT NULL,
    owner_user_id BIGINT,
    resident_user_id BIGINT,
    invoice_number VARCHAR(64) NOT NULL UNIQUE,
    billing_period VARCHAR(16) NOT NULL,
    invoice_date DATE NOT NULL,
    due_date DATE NOT NULL,
    subtotal NUMERIC(14,2) NOT NULL,
    penalty_amount NUMERIC(14,2) DEFAULT 0.00,
    discount_amount NUMERIC(14,2) DEFAULT 0.00,
    total_amount NUMERIC(14,2) NOT NULL,
    paid_amount NUMERIC(14,2) DEFAULT 0.00,
    outstanding_amount NUMERIC(14,2) NOT NULL,
    status VARCHAR(32) NOT NULL,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_invoice_period UNIQUE (community_id, flat_id, billing_period)
);

CREATE TABLE IF NOT EXISTS community_invoice_items (
    id BIGSERIAL PRIMARY KEY,
    invoice_id BIGINT NOT NULL REFERENCES community_invoices(id) ON DELETE CASCADE,
    component_type VARCHAR(64) NOT NULL,
    item_description VARCHAR(255) NOT NULL,
    amount NUMERIC(14,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS community_payment_transactions (
    id BIGSERIAL PRIMARY KEY,
    community_id BIGINT NOT NULL,
    invoice_id BIGINT REFERENCES community_invoices(id),
    transaction_ref VARCHAR(64) NOT NULL UNIQUE,
    paid_by_user_id BIGINT,
    amount NUMERIC(14,2) NOT NULL,
    payment_mode VARCHAR(32) NOT NULL,
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(32) NOT NULL,
    gateway_payment_id VARCHAR(128),
    gateway_signature VARCHAR(256),
    receipt_number VARCHAR(64),
    notes TEXT
);

CREATE TABLE IF NOT EXISTS community_receipts (
    id BIGSERIAL PRIMARY KEY,
    community_id BIGINT NOT NULL,
    invoice_id BIGINT NOT NULL REFERENCES community_invoices(id),
    transaction_id BIGINT REFERENCES community_payment_transactions(id),
    receipt_number VARCHAR(64) NOT NULL UNIQUE,
    receipt_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    amount_paid NUMERIC(14,2) NOT NULL,
    payment_mode VARCHAR(32) NOT NULL,
    receipt_pdf_url VARCHAR(512),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS resident_advance_payments (
    id BIGSERIAL PRIMARY KEY,
    community_id BIGINT NOT NULL,
    flat_id BIGINT NOT NULL,
    resident_user_id BIGINT NOT NULL,
    balance_amount NUMERIC(14,2) DEFAULT 0.00,
    total_deposited NUMERIC(14,2) DEFAULT 0.00,
    total_utilized NUMERIC(14,2) DEFAULT 0.00,
    last_utilized_at TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_resident_advance UNIQUE (community_id, flat_id, resident_user_id)
);

-- Double Entry General Ledger Tables
CREATE TABLE IF NOT EXISTS general_ledger_accounts (
    id BIGSERIAL PRIMARY KEY,
    community_id BIGINT NOT NULL,
    account_code VARCHAR(32) NOT NULL,
    account_name VARCHAR(128) NOT NULL,
    account_type VARCHAR(32) NOT NULL,
    current_balance NUMERIC(14,2) DEFAULT 0.00,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_community_account_code UNIQUE (community_id, account_code)
);

CREATE TABLE IF NOT EXISTS general_ledger_transactions (
    id BIGSERIAL PRIMARY KEY,
    community_id BIGINT NOT NULL,
    transaction_ref VARCHAR(64) NOT NULL UNIQUE,
    transaction_date DATE NOT NULL,
    reference_type VARCHAR(32) NOT NULL,
    reference_id BIGINT,
    narration VARCHAR(512),
    total_amount NUMERIC(14,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS general_ledger_entries (
    id BIGSERIAL PRIMARY KEY,
    transaction_id BIGINT NOT NULL REFERENCES general_ledger_transactions(id) ON DELETE CASCADE,
    account_id BIGINT NOT NULL REFERENCES general_ledger_accounts(id),
    entry_type VARCHAR(16) NOT NULL,
    amount NUMERIC(14,2) NOT NULL,
    notes VARCHAR(256)
);
