--changeset jeff:58
-- Resynchronize PostgreSQL sequences after explicit seed inserts so new records get fresh IDs.
SELECT setval(pg_get_serial_sequence('server_settings', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM server_settings;
SELECT setval(pg_get_serial_sequence('character_classes', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM character_classes;
SELECT setval(pg_get_serial_sequence('rooms', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM rooms;
SELECT setval(pg_get_serial_sequence('effects', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM effects;
SELECT setval(pg_get_serial_sequence('skills_registry', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM skills_registry;
SELECT setval(pg_get_serial_sequence('items', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM items;

