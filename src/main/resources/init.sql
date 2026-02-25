CREATE TABLE IF NOT EXISTS server_info (
    id SERIAL PRIMARY KEY,
    key VARCHAR(255) NOT NULL UNIQUE,
    value VARCHAR(255) NOT NULL
);

INSERT INTO server_info (key, value) VALUES ('db_status', 'R2DBC Connection Established!')
ON CONFLICT (key) DO UPDATE SET value = 'R2DBC Connection Established!';
