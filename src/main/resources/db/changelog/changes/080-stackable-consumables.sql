--changeset jeff:080-stackable-consumables
UPDATE items SET stackable = TRUE, count = 1 WHERE item_type IN ('BANDAGE', 'DRINK', 'FOOD');
