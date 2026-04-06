-- liquibase formatted sql
-- changeset jeff:056-mobile-flags
ALTER TABLE mobiles
    ADD COLUMN hate_healer BOOLEAN DEFAULT FALSE,
    ADD COLUMN hate_debuffer BOOLEAN DEFAULT FALSE,
    ADD COLUMN hate_wizard BOOLEAN DEFAULT FALSE,
    ADD COLUMN hate_cleric BOOLEAN DEFAULT FALSE,
    ADD COLUMN hate_singer BOOLEAN DEFAULT FALSE,
    ADD COLUMN will_follow BOOLEAN DEFAULT FALSE,
    ADD COLUMN will_loot BOOLEAN DEFAULT FALSE;
