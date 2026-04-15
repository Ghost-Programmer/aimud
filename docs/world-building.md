# World Building & Content Guide

In AIMud, developers and game masters can expand the world via two distinct methods: Manual Database Migration or the Real-Time AI Builder.

## 1. Manual Migration (Liquibase)

AIMud serves an authoritative source of truth via Liquibase. All core static content is loaded through `src/main/resources/db/changelog/db.changelog-master.yaml` which hooks into `000-consolidated.sql`.

If you are adding massive, static, foundational data (e.g. a new playable Race or a core mechanics class), you should append standard `INSERT` statements to the `.sql` changeset. 

**Soft Deletes vs Hard Deletes**
The system logic implements soft-deletes (`deleted` boolean flag) for major structural tables like `races` and `character_classes`. Ensure you do not write hard `DELETE` SQL queries against these tables, as doing so breaks relational integrity for characters already bound to those types.

---

## 2. Real-Time AI Builder Assistant

For daily game mastering, generating dungeon wings, creating localized towns, and populating merchants, use the embedded AI Assistant.

In the frontend client, click the **AI Builder** icon. Any prompt you send is intercepted by `McpToolService`, which allows the LLM to directly shape the world grid.

**Available Real-Time Capabilities:**
1. **Rooms**: The AI can execute `createRoom(...)`. Ensure you explicitly tell the AI what exits you want linked. If you want a room to the north of Room ID 12, the AI will hook `northId: 12` appropriately.
2. **Items**: The AI can execute `createItem(...)`. You must specify the exact `ItemType` and `WearLocation`. The AI has the power to assign magical `Effect` arrays directly to the item upon creation!
3. **Mobiles (NPCs)**: The AI executes `createMobile(...)`. You can ask the AI to generate a "Level 10 Goblin with high dexterity" and the AI will auto-calculate and generate those stats relative to constraints. The AI can also link items directly to the NPC's `inventoryItemIds` so they drop loot upon death.
4. **Stores**: Generating a merchant is simple. Ask the AI to build a Store, give it an array of initialized Item IDs the store should sell, and then bind the store via dialogue logic to an NPC in a specific room.
