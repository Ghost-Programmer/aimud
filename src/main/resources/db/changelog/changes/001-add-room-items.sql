-- liquibase formatted sql

-- changeset jeff:001-add-room-items
ALTER TABLE rooms ADD COLUMN items TEXT;
