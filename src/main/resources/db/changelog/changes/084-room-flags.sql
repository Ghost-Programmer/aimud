--liquibase formatted sql

--changeset jeff:084-room-flags
ALTER TABLE rooms ADD COLUMN room_persist BOOLEAN DEFAULT false;
ALTER TABLE rooms ADD COLUMN room_house BOOLEAN DEFAULT false;
