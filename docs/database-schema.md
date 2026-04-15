# Database Schema Overview

AIMud relies on PostgreSQL driven by Reactive Relational Database Connectivity (R2DBC). The object-relational mapping expects a non-blocking asynchronous flow (there is no Hibernate/JPA locking the threads). 

## Core Persistence Tables

### 1. The World Mesh
* `rooms`: Contains descriptions, coordinate/zone groupings if applicable, and integer pointers to adjacent rooms (`north_id`, `south_id`, etc.). This forms a linked-list grid structure natively in SQL.
* `mobiles`: Non-player characters localized to a `current_room_id`.
* `stores`: Mapped commerce definitions linked heavily to the GUI trading elements.

### 2. Entities & Relational Mapping
Due to the constraints of R2DBC, complex Many-To-Many mappings often use explicit joint tables parsed manually via Spring Data `DatabaseClient`.
* `character_inventory`: Maps `character_id` to `item_id`.
* `store_items`: Maps a merchant's `store_id` to available `item_id` stock.
* `effects`: Abstract status ailments/buffs linked manually through code arrays rather than deeply nested eager-loaded objects.

### 3. Enumerated Boundaries
Tables map strictly to internal Java Enums. Modifying the database directly must ensure string alignment with the Enums:
* `WearLocation`: `HEAD`, `CHEST`, `LEGS`, `FEET`, `PRIMARY`, `OFFHAND`, etc.
* `ItemType`: `WEAPON`, `LIGHT_ARMOR`, `POTION`, `SCROLL`, etc.
* `RoomType`: `INDOORS`, `CITY`, `FOREST`, `UNDERGROUND_DUNGEON`, etc.

### 4. Configuration & Server Metadata
The project stores runtime settings that affect all connections in SQL:
* `agents`: Custom system prompts fed directly to the Ollama integration for the AI system.
* `server_settings`: Toggles for Maintenance Mode, New Account Lockouts, and MOTD (Message of the Day) rendering.

## Authoritative Migrations
All changes must flow through Liquibase. The primary initialization chunk (`000-consolidated.sql`) boots the initial seed schema. When creating new schema definitions, always ensure indices are generated for high-traffic lookups (such as `room_id` lookups for broadcasting messages to clients sharing a space).
