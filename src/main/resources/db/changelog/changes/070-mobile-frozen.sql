-- liquibase formatted sql

-- changeset admin:070
ALTER TABLE mobiles ADD COLUMN frozen BOOLEAN DEFAULT false;