# Player Commands Reference

AIMud uses a reflective `@MudCommand` annotation system across the `com.aimud.aimud.commands` package. Typing any of these inside the game client will trigger their respective actions.

## Movement & Exploration
* `north` (or `n`) - Move North
* `south` (or `s`) - Move South
* `east` (or `e`) - Move East
* `west` (or `w`) - Move West
* `up` (or `u`) - Move Up
* `down` (or `d`) - Move Down
* `look` (or `l`) - Look at your immediate surroundings.
* `exits` - List all visible room exits.

## Inventory & Items
* `take <item>` / `get <item>` - Pick up an item from the room.
* `drop <item>` - Drop an item onto the floor.
* `equip <item>` - Wear or wield an item from your inventory.
* `remove <item>` - Unequip an item.
* `loot <corpse>` - Gather items from a fallen enemy.
* `inventory` (or `i`) - View what you are carrying.

## Combat & Subterfuge
* `attack <target>` (or `kill`) - Initiate physical combat.
* `bash <target>` - Perform a physical knockdown attempt.
* `taunt <target>` / `intimidate <target>` / `warcry` - Threat generation and utility skills.
* `pickpocket <target>` - Attempt to steal coins.
* `hide` / `vanish` - Attempt to enter stealth mode.

## Magic & Classes
* `cast <spell>` - Cast an arcane spell (Mage/Cleric).
* `pray <prayer>` - Beseech a deity for a blessing or smite.
* `sing <song>` - Perform a Bardic tune for party buffs.
* `learn` - Spend points/xp to acquire new abilities.

## Social & Communication
* `say <text>` - Speak out loud to your room.
* `shout <text>` / `yell <text>` - Broadcast to connected adjacent rooms.
* `tell <player> <text>` / `whisper <player> <text>` - Send a private message.
* `party <text>` - Speak strictly to your grouped members.
* `group` / `follow <player>` - Party management utilities.

## Roleplay Emotes
The server natively supports social emotes that broadcast flavorful text to the room.
* `yawn`, `wink`, `wave`, `smirk`, `smile`, `sigh`, `shrug`, `shake`, `pout`, `nod`, `laugh`, `groan`, `grin`, `glare`, `giggle`, `gasp`, `dance`, `cry`, etc.

## System
* `help` - Show a general overview of commands and syntax.
* `store` - Interact with a merchant (requires being in a room with a Store mobile).
* `logout` / `quit` / `exit` - Disconnect from the MUD safely.
