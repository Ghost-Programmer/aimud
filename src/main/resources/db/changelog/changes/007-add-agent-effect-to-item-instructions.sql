--changeset jeff:60
INSERT INTO agents (id, title, content)
VALUES (
    11,
    'Add Effect to Item Instructions',
    'To add an effect to an item, first make sure you know the item id. If the item id is unknown, use getAllItems or getItem to identify the correct item. Then call createEffect with itemId set to that item id. Always use an exact EffectType enum value. For weapon damage effects such as SLASHING_DAMAGE, PIERCING_DAMAGE, or BASHING_DAMAGE, set modifier1 to the number of dice and modifier2 to the die size. For stat or combat effects such as STRENGTH, DEXTERITY, ARMOR, DODGE, MAGIC_RESIST, PHYSICAL_ATTACK, MAGIC_ATTACK, or CRITICAL_HIT, set modifier1 to the bonus amount. For status effects such as FLY, WATER_BREATHING, and INVISIBLE, pass 0 for all modifiers. If some modifier fields are unused, pass 0 for those values. After creating the effect, you may use getEffectsByItem with the same item id to confirm the effect is attached.'
)
ON CONFLICT (id) DO UPDATE SET
    title = EXCLUDED.title,
    content = EXCLUDED.content;

SELECT setval(pg_get_serial_sequence('agents', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM agents;

