--liquibase formatted sql

--changeset jeff:5
ALTER TABLE races ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE character_classes ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT false;
