--liquibase formatted sql

--changeset jeff:8
CREATE TABLE IF NOT EXISTS rooms (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    room_type VARCHAR(50) NOT NULL,
    north_id BIGINT,
    south_id BIGINT,
    east_id BIGINT,
    west_id BIGINT,
    up_id BIGINT,
    down_id BIGINT,
    north_door BOOLEAN DEFAULT false,
    south_door BOOLEAN DEFAULT false,
    east_door BOOLEAN DEFAULT false,
    west_door BOOLEAN DEFAULT false,
    up_door BOOLEAN DEFAULT false,
    down_door BOOLEAN DEFAULT false,
    north_door_open BOOLEAN DEFAULT false,
    south_door_open BOOLEAN DEFAULT false,
    east_door_open BOOLEAN DEFAULT false,
    west_door_open BOOLEAN DEFAULT false,
    up_door_open BOOLEAN DEFAULT false,
    down_door_open BOOLEAN DEFAULT false
);
