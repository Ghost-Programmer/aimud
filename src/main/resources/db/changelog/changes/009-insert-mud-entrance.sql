--liquibase formatted sql

--changeset jeff:9
INSERT INTO rooms (id, name, description, room_type) VALUES
(1, 'Mud Entrance', 'You stand at the grand entrance of the AI MUD. This is the primary gathering place for adventurers starting their journey.', 'CITY')
ON CONFLICT (id) DO NOTHING;
