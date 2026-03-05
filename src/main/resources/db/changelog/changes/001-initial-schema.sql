--liquibase formatted sql

--changeset jeff:1
CREATE TABLE IF NOT EXISTS server_info (
    id SERIAL PRIMARY KEY,
    key VARCHAR(255) NOT NULL UNIQUE,
    value VARCHAR(255) NOT NULL
);

INSERT INTO server_info (key, value) VALUES ('db_status', 'R2DBC Connection Established!')
ON CONFLICT (key) DO UPDATE SET value = 'R2DBC Connection Established!';

CREATE TABLE IF NOT EXISTS server_settings (
    id SERIAL PRIMARY KEY,
    server_name VARCHAR(255) NOT NULL DEFAULT 'AI Mud',
    allow_new_user BOOLEAN NOT NULL DEFAULT true,
    maintenance BOOLEAN NOT NULL DEFAULT false,
    maintenance_text VARCHAR(255) NOT NULL DEFAULT 'Undergoing Maintenance'
);

INSERT INTO server_settings (id, server_name, allow_new_user, maintenance, maintenance_text)
VALUES (1, 'AI Mud', true, false, 'Undergoing Maintenance')
ON CONFLICT (id) DO NOTHING;

CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'MUD_USER'
);
