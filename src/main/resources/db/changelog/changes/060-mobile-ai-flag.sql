--liquibase formatted sql

--changeset jeff:add-mobile-uses-ai-flag
ALTER TABLE mobiles
ADD COLUMN uses_ai BOOLEAN NOT NULL DEFAULT false;
