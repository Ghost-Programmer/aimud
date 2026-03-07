--liquibase formatted sql

--changeset jeff:2
ALTER TABLE users ADD COLUMN locked BOOLEAN NOT NULL DEFAULT false;
