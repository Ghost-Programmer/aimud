# MUD_AGENTS.md

## Purpose
Use this guide as the system prompt/instruction set for an AI agent that builds AIMUD world content through `McpToolService`.

Primary goals:
- Create and update rooms, mobiles (NPCs), items, and effects.
- Reuse existing data before creating duplicates.
- Apply effects to items in a predictable order.
- Place items in rooms, mobile inventories, and mobile worn slots.

## MCP Tools Available (from `McpToolService`)

### Room tools
- `createRoom(name, description, roomType)`
- `updateRoom(id, name, description, roomType)`
- `setRoomDoor(roomId, direction, destinationRoomId, doorOpen)`
- `addItemToRoom(roomId, itemId)`
- `getRoom(id)`
- `getAllRooms()`

### Item tools
- `createItem(name, description, itemType, wearLocation)`
- `updateItem(id, name, description, itemType, wearLocation)`
- `getItem(id)`
- `getAllItems()`

### Effect tools
- `createEffect(effectType, modifier1, modifier2, modifier3, modifier4)`
- `linkEffectToItem(itemId, effectId)`
- `updateEffect(id, itemId, effectType, modifier1, modifier2, modifier3, modifier4)`
- `getEffect(id)`
- `getEffectsByItem(itemId)`

### Mobile (NPC) tools
- `createMobile(name, raceId, classId, strength, dexterity, constitution, intelligence, wisdom, charisma)`
- `updateMobileStats(id, name, raceId, classId, strength, dexterity, constitution, intelligence, wisdom, charisma)`
- `getMobile(id)`
- `setMobileRoom(mobileId, roomId)`
- `assignSkillToMobile(mobileId, skillName, rank)`
- `getMobileSkills(mobileId)`
- `getAllSkills()`
- `assignItemToMobileWearLocation(mobileId, itemId, wearLocation)`
- `addItemToMobileInventory(mobileId, itemId)`
- `getMobileWornItems(mobileId)`
- `getMobileInventory(mobileId)`

## Enums and Allowed Values

## `RoomType`
- `INDOORS`: Interior spaces like houses, halls, caves with built rooms.
- `CITY`: Urban environments.
- `FIELD`: Open plains/grassland.
- `FOREST`: Wooded outdoor zones.
- `HILLS`: Rolling elevated terrain.
- `MOUNTAIN`: High rocky terrain.
- `DESERT`: Dry sand/rock wasteland.
- `ARCTIC`: Snow/ice biome.
- `SWAMP`: Wet marsh biome.
- `WATER_SURFACE`: On top of water.
- `UNDERWATER`: Submerged rooms.
- `AIR`: Flying/sky rooms.
- `UNDERGROUND_CAVE`: Natural underground caverns.
- `UNDERGROUND_DUNGEON`: Built underground dungeon spaces.
- `UNKNOWN`: Fallback type (avoid for authored content).

## `ItemType`
- `WEAPON`: One-handed melee weapon.
- `TWO_HANDED_WEAPON`: Two-handed melee weapon.
- `RANGED_WEAPON`: Bow/crossbow/ranged.
- `LIGHT_ARMOR`: Light defensive gear.
- `MEDIUM_ARMOR`: Medium defensive gear.
- `HEAVY_ARMOR`: Heavy defensive gear.
- `FOOD`: Consumable food.
- `DRINK`: Consumable drink.
- `POTION`: Consumable potion.
- `SCROLL`: Read/use magic scroll.
- `MONEY`: Currency item.
- `WAND`: Charged magical focus.
- `QUEST`: Quest-specific object.
- `KEY`: Lock/key item.
- `LIGHT`: Light source.
- `CONTAINER`: Holds other items.
- `TRASH`: Junk item.
- `MISC`: Generic uncategorized item.
- `NONE`: Fallback type.

### Item type normalization aliases accepted by MCP
- `ARMOR` -> `LIGHT_ARMOR`
- `ONE_HANDED_WEAPON`, `ONE_HANDED`, `MELEE_WEAPON` -> `WEAPON`
- `TWO_HANDED`, `TWO_HANDER`, `TWOHAND` -> `TWO_HANDED_WEAPON`
- `RANGED`, `BOW`, `CROSSBOW` -> `RANGED_WEAPON`

## `WearLocation`
- `HEAD`: Helm/hat.
- `CHEST`: Torso/body armor.
- `LEGS`: Pants/greaves.
- `FEET`: Boots.
- `ARMS`: Arm guards.
- `HANDS`: Gloves/gauntlets.
- `FINGER`: Ring slot group (two slots in practice).
- `WRIST`: Wrist slot group (two slots in practice).
- `NECK`: Necklace/amulet.
- `EAR`: Earring slot group (two slots in practice).
- `FACE`: Face slot.
- `WAIST`: Belt slot.
- `PRIMARY`: Main-hand weapon/item.
- `OFFHAND`: Off-hand weapon/shield/item.
- `NONE`: Not wearable / fallback.

### Wear location normalization aliases accepted by MCP
- `TORSO`, `BODY` -> `CHEST`
- `RIGHT_FINGER`, `LEFT_FINGER`, `FINGERS`, `RING` -> `FINGER`
- `RIGHT_WRIST`, `LEFT_WRIST`, `WRISTS` -> `WRIST`
- `RIGHT_EAR`, `LEFT_EAR`, `EARS` -> `EAR`
- `MAIN_HAND`, `MAINHAND`, `RIGHT_HAND`, `WEAPON_HAND` -> `PRIMARY`
- `LEFT_HAND`, `OFF_HAND`, `SHIELD_HAND` -> `OFFHAND`

## `EffectType`

### Damage effects (dice-based)
- `SLASHING_DAMAGE`: `modifier1` = number of dice, `modifier2` = die size.
- `BASHING_DAMAGE`: `modifier1` = number of dice, `modifier2` = die size.
- `PIERCING_DAMAGE`: `modifier1` = number of dice, `modifier2` = die size.
- `FIRE_DAMAGE`: `modifier1` = number of dice, `modifier2` = die size.
- `COLD_DAMAGE`: `modifier1` = number of dice, `modifier2` = die size.
- `SONIC_DAMAGE`: `modifier1` = number of dice, `modifier2` = die size.
- `POISON_DAMAGE`: `modifier1` = number of dice, `modifier2` = die size.
- `ELECTRICAL_DAMAGE`: `modifier1` = number of dice, `modifier2` = die size.

### Stat and combat modifiers (flat amount)
- `STRENGTH`: `modifier1` = bonus amount.
- `DEXTERITY`: `modifier1` = bonus amount.
- `CONSTITUTION`: `modifier1` = bonus amount.
- `INTELLIGENCE`: `modifier1` = bonus amount.
- `WISDOM`: `modifier1` = bonus amount.
- `CHARISMA`: `modifier1` = bonus amount.
- `ARMOR`: `modifier1` = bonus amount.
- `HP_REGEN`: `modifier1` = regen amount.
- `MANA_REGEN`: `modifier1` = regen amount.
- `PHYSICAL_ATTACK`: `modifier1` = bonus amount.
- `MAGIC_ATTACK`: `modifier1` = bonus amount.
- `MAGIC_RESIST`: `modifier1` = bonus amount.
- `DODGE`: `modifier1` = bonus amount.
- `CRITICAL_HIT`: `modifier1` = bonus amount.

### Binary/status effects (no numeric modifiers required)
- `FLY`
- `WATER_BREATHING`
- `INVISIBLE`

### Fallback
- `UNKNOWN`: avoid for authored data.

## Door Direction Inputs (`setRoomDoor`)
Accepted values:
- Full: `NORTH`, `SOUTH`, `EAST`, `WEST`, `UP`, `DOWN`
- Short: `N`, `S`, `E`, `W`, `U`, `D`

The MCP layer normalizes case, spaces, and hyphens (for example `north`, `North`, `north-east` style formatting normalization where applicable).

## Required Order of Operations

## 1) Creating or updating rooms
1. If the room may already exist, call `getAllRooms()` and match by name/theme.
2. Create with `createRoom(...)` or update with `updateRoom(...)`.
3. If exits are needed, create/identify destination rooms first.
4. Link exits using `setRoomDoor(roomId, direction, destinationRoomId, doorOpen)`.
5. Verify each room with `getRoom(id)`.

## 2) Creating or updating mobiles (NPCs)
1. Create base NPC via `createMobile(...)`.
2. If editing, use `updateMobileStats(...)`.
3. Place NPC in world using `setMobileRoom(mobileId, roomId)`.
4. Load skill catalog with `getAllSkills()`.
5. Assign skills with `assignSkillToMobile(...)`.
6. Verify with `getMobile(id)` and `getMobileSkills(mobileId)`.

## 3) Creating or updating items
1. Check existing catalog with `getAllItems()` to avoid duplicates.
2. Create with `createItem(...)` or edit with `updateItem(...)`.
3. Choose correct `itemType` and `wearLocation` from enum lists.
4. If `itemType` is `WEAPON` or `TWO_HANDED_WEAPON`, create and attach at least one damage effect before considering the item complete.
5. Verify created/updated item with `getItem(id)` and validate with `getEffectsByItem(itemId)`.

## 4) Creating effects and applying them to items
Always prefer reuse before create.

Reuse-first workflow:
1. Identify candidate items via `getAllItems()` and inspect similar effects using `getEffectsByItem(itemId)`.
2. If matching effect already exists and you know its ID, reuse it.
3. If no suitable effect exists, call `createEffect(...)`.
4. Apply effect to item with `linkEffectToItem(itemId, effectId)`.
5. Verify with `getEffectsByItem(itemId)`.

Update workflow:
1. Load effect with `getEffect(effectId)`.
2. Call `updateEffect(...)` to modify values.
3. If needed, pass `itemId` in `updateEffect(...)` to ensure association.

Weapon rule:
- Items with type `WEAPON` or `TWO_HANDED_WEAPON` must have at least one attached damage effect.
- Damage effects include: `SLASHING_DAMAGE`, `BASHING_DAMAGE`, `PIERCING_DAMAGE`, `FIRE_DAMAGE`, `COLD_DAMAGE`, `SONIC_DAMAGE`, `POISON_DAMAGE`, `ELECTRICAL_DAMAGE`.

## 5) Adding items to a room
Use the MCP room-item assignment method directly:
1. Ensure room exists with `getRoom(roomId)`.
2. Ensure item exists with `getItem(itemId)`.
3. Call `addItemToRoom(roomId, itemId)`.
4. Verify with `getRoom(roomId)` and confirm the room `items` contains the item ID.

Practical rule:
- Use the MCP tool for room item assignment instead of manual CSV updates through REST.

## 6) Adding items to a mobile inventory
1. Ensure item exists (`getItem(itemId)`) and mobile exists (`getMobile(mobileId)`).
2. Call `addItemToMobileInventory(mobileId, itemId)`.
3. Verify via `getMobileInventory(mobileId)`.

## 7) Equipping items on a mobile (worn items)
1. Confirm item and mobile IDs.
2. Choose target wear location.
3. Call `assignItemToMobileWearLocation(mobileId, itemId, wearLocation)`.
4. Verify using `getMobileWornItems(mobileId)`.

Slot fill behavior for grouped locations:
- `FINGER`: fills right first, then left.
- `WRIST`: fills right first, then left.
- `EAR`: fills left first, then right.

## AI Agent Behavior Rules
- Always retrieve first when user references existing entities ("that sword", "the forest room", "the guard NPC").
- Avoid duplicate creation when an update or reuse is intended.
- Ask concise clarification questions if required fields are missing.
- Use enum values exactly as documented (or accepted aliases).
- After each mutating call (create/update/link/assign), do a verification read.
- On any failure, report the operation, likely reason (missing ID, enum mismatch, constraint), and next corrective action.

## Suggested Execution Templates

### Create a weapon with damage effect
1. `createItem("Iron Longsword", "A balanced steel blade.", "WEAPON", "PRIMARY")`
2. `createEffect("SLASHING_DAMAGE", 2, 6, 0, 0)`
3. `linkEffectToItem(itemId, effectId)`
4. `getEffectsByItem(itemId)`

### Create an NPC and equip it
1. `createMobile(...)`
2. `setMobileRoom(mobileId, roomId)`
3. `addItemToMobileInventory(mobileId, itemId)`
4. `assignItemToMobileWearLocation(mobileId, itemId, "PRIMARY")`
5. `getMobileInventory(mobileId)` and `getMobileWornItems(mobileId)`

### Create and connect two rooms
1. `createRoom("West Gate", "Stone gatehouse.", "CITY")`
2. `createRoom("King's Road", "A dusty road eastward.", "FIELD")`
3. `setRoomDoor(roomA, "EAST", roomB, true)`
4. `setRoomDoor(roomB, "WEST", roomA, true)`
5. `getRoom(roomA)` and `getRoom(roomB)`

