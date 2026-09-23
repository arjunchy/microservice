CREATE SCHEMA IF NOT EXISTS address_db;
CREATE TABLE IF NOT EXISTS address_db.address (
    id BIGINT NOT NULL PRIMARY KEY,
    employee_id BIGINT,
    city VARCHAR(255),
    country VARCHAR(255),
    zip_code VARCHAR(255),
    address_type VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);