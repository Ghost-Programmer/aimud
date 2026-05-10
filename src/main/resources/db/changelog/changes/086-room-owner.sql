-- liquibase formatted sql

-- changeset aimud:86
ALTER TABLE rooms ADD COLUMN room_owner BIGINT NULL;
