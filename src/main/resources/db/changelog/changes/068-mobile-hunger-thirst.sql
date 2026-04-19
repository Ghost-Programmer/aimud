-- liquibase formatted sql
-- changeset jeff:068-mobile-hunger-thirst

ALTER TABLE mobiles 
ADD COLUMN hunger INT DEFAULT 100 NOT NULL,
ADD COLUMN thirst INT DEFAULT 100 NOT NULL;
