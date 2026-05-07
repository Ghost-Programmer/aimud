--liquibase formatted sql

--changeset jeff:update-light-wear-location
UPDATE items SET wear_location = 'OFFHAND' WHERE item_type = 'LIGHT';
