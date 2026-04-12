-- changeset jeff:063-mobile-non-combat

ALTER TABLE mobiles
    ADD COLUMN IF NOT EXISTS non_combat BOOLEAN DEFAULT FALSE;
