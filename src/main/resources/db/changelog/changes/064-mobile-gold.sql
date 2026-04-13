-- liquibase formatted sql

-- changeset jeff:064-add-mobile-gold
ALTER TABLE mobiles ADD COLUMN gold INT DEFAULT 0;
