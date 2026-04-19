# Player Dashboard Overview

The **Player Dashboard** is the primary access point for any connected user interacting with the AIMud client. Unlike traditional telnet-based MUDs, the AIMud angular wrapper exposes an interactive Web UI capable of multi-tab multi-boxing navigation natively.

## Interface Elements

When a user logs in with the standard `PLAYER` role, they are granted access to a horizontal tab-based window layout designed for ease of mobility and multi-character management.

### Top Status Bar
* **Role Indicator:** Confirms your connected role level.
* **Time Synchronicity:** Displays current local machine time.
* **Session Tracking:** Highlights the total connected uptime in real-time (`HH:MM:SS`).
* **Theme Toggle:** Allows players to instantly switch between Light (`☀️`) and Dark (`🌙`) modes using `ThemeService`.
* **Logout Button:** Disconnects and purges local storage authentication payloads.

### Drag & Drop Tab Interface
The dashboard utilizes an interactive Angular drag-and-drop list `cdkDropList`.
Players can freely arrange their open tabs and switch active views synchronously without dropping their TCP background connections to the game engine.

### Core Tabs

#### 1. Select Character
The default entry tab. It populates a list of all existing characters mapped to the user.
* Clicking a character launches them into the active game loop.
* Launching a character spawns a new distinct **Play Character** tab seamlessly.
* The selection menu hides characters that are already actively played/spawned into a tab.

#### 2. Create Character
An onboard creation wizard allowing players to construct a raw identity.
* Validates unique naming.
* Prompts logic loops for choosing Base Identity -> Race -> Class.
* Injects starting loadouts, standard spells, and permanently assigns nested racial characteristics (like Darkvision or Fly).

#### 3. Play Character (Active Session)
Whenever a player selects a character, a dedicated `app-character-play` tab is mapped specifically to that `character_id`.
* **Multi-boxing Native:** You can open two distinctly different characters and jump between their tabs without losing connection.
* **Component Housing:** Each Play Tab encapsulates its own chat output window alongside customized graphical components (Health, Mana, Macros, Room Details).
* **Closing:** Clicking the `[x]` on a playing tab triggers an explicit logout event gracefully withdrawing the character from the active room state simulation before destroying the view.
