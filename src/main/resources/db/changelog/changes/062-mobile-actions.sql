-- liquibase formatted sql

-- changeset jeff:062-mobile-actions
ALTER TABLE mobiles DROP COLUMN IF EXISTS ai_instructions;

CREATE TABLE mobile_actions (
    id SERIAL PRIMARY KEY,
    mobile_id BIGINT NOT NULL,
    action_command VARCHAR(255) NOT NULL,
    description VARCHAR(500) NOT NULL,
    CONSTRAINT fk_mobile_actions_mobile FOREIGN KEY (mobile_id) REFERENCES mobiles(id) ON DELETE CASCADE
);
