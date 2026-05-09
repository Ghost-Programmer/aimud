-- changeset aimud:083-auto-map
CREATE TABLE mobile_visited_rooms (
    mobile_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    PRIMARY KEY (mobile_id, room_id)
);
