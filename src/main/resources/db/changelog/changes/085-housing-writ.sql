--liquibase formatted sql
--changeset jeff:085-housing-writ
INSERT INTO items (item_type, wear_location, name, description, property_1)
VALUES ('DOCUMENT', 'NONE', 'Housing Writ', 'An official writ granting the bearer the right to claim a plot of land for housing.', 250000);
