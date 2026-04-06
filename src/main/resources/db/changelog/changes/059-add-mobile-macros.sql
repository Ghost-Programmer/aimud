--changeset antigravity:59
CREATE TABLE mobile_macros (
    id SERIAL PRIMARY KEY,
    mobile_id BIGINT NOT NULL,
    macro_index INT NOT NULL,
    label VARCHAR(50) NOT NULL,
    command VARCHAR(255) NOT NULL,
    CONSTRAINT fk_mobile_macros_mobile_id FOREIGN KEY (mobile_id) REFERENCES mobiles(id) ON DELETE CASCADE,
    UNIQUE (mobile_id, macro_index)
);
