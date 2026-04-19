# Admin Dashboard Overview

The **Admin Dashboard** natively extends the `MUD_ADMIN` role session by embedding advanced management panels and World Building UI sets concurrently into the browser environment alongside their standard play sessions. 

An administrative user maintains everything a generic player utilizes (multi-socket tabs, drag-and-drop sessions, and theme toggling), but unlocks the following distinct modules.

## Administrative Interface Additions

### Top Status Bar Expansion
* **The AI Assistant (`🤖` Icon):** Clicking this module launches the persistent LLM administration widget. It interacts exclusively with the local Ollama instance running the `Builder Agent` model allowing the admin to run conversational Model Context Protocol (MCP) commands. From this chat module, the GM can say `"Write me a spooky room and spawn an Orc inside of it"`, which explicitly maps parameters directly through Spring REST pipelines into native SQL database updates in real-time.

### Tab Interface Expansion

When logged in, the application hooks three additional primary tabs explicitly.

#### 1. Config System (`Config`)
An essential operational settings dashboard. Admins can view, edit, soft-delete, and create globally tracked system metrics.
* **Server Status Mapping:** Adjust current `SystemStatus` thresholds.
* **Base Architectures:** Add or edit fundamental baseline structures like universal `Races` and `CharacterClasses`.
* **AI Configuration:** Live-edit the active `ai_system_prompt` and LLM variables modifying how aggressively the `Conversational Engine` hallucinates or builds structures globally.

#### 2. Players Network (`Players`)
Active network visibility.
* Displays all completely registered server accounts, listing active roles.
* Contains kick metrics and offline moderation capabilities over connected users.

#### 3. World Builder (`World`)
This is the core content-creation suite housing five distinct expandable accordion zones. A GM operates out of here while live-playing to rapidly drop structures into existence.
* **Room Management:** Modify dimensions, tags, environmental descriptions, light limits, boundaries, coordinate layers, and zone tags to expand the physical map. 
* **Mobile Editor:** Assemble NPC structures, modify attributes, link dialogue seeds, append specific weapons/gold drops, assign conversational hostility matrices, and define spawning locations.
* **Item Creator:** Prototype equipment sets, inject weights, configure value offsets, assign standard combat variables alongside dynamic JSON metadata extensions mappings.
* **Effect Creator:** Structure raw status/enchantment spells. Build custom damage-over-time variables, modify base characteristics, and establish runtime durations. 
* **Store Management:** Construct custom NPC merchant storefronts mapping infinite pools of unique items with markup margins directly into dialogue arrays. A player interacting with an NPC who possesses a Store module natively triggers a trading screen over the chat interface.
