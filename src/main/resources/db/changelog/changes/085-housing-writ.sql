--liquibase formatted sql
--changeset jeff:085-housing-writ validCheckSum:ANY
INSERT INTO items (id, item_type, wear_location, name, description, property_1)
VALUES (669, 'DOCUMENT', 'NONE', 'Housing Writ', 'An official writ granting the bearer the right to claim a plot of land for housing.', 250000);
