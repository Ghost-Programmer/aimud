--liquibase formatted sql
--changeset jeff:087-house-expansion-permit validCheckSum:ANY
INSERT INTO items (id, item_type, wear_location, name, description, property_1)
VALUES (670, 'DOCUMENT', 'NONE', 'House Expansion Permit', 'An official permit granting the bearer the right to expand an existing house they own.', 100000);
