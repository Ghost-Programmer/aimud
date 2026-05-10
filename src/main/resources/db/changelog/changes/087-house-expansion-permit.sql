--liquibase formatted sql
--changeset jeff:087-house-expansion-permit
INSERT INTO items (item_type, wear_location, name, description, property_1)
VALUES ('DOCUMENT', 'NONE', 'House Expansion Permit', 'An official permit granting the bearer the right to expand an existing house they own.', 100000);
