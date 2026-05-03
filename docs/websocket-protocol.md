# WebSocket & API Protocol

The AIMud frontend and backend communicate real-time game changes nearly entirely over WebSockets via Project Reactor. Native HTTP REST endpoints are used primarily for authentication (`/api/users/login`) and AI prompting (`/api/ai/prompt`), but the live MUD environment relies on a fast, asynchronous socket stream.

## Connection Layer
**Main Endpoint:** `ws://localhost:8080/ws/game` (Handled by `GameWebSocketHandler.java`)
**Combat Log Endpoint:** `ws://localhost:8080/ws/combat_log` (Handled by `CombatLogWebSocketHandler.java`)

When the Angular frontend connects, it passes the JWT token as a bearer header. The connection must establish the user's active character immediately contextually.

## Payload Protocols

### The Standard JSON Payload (`/ws/game`)
All standard WebSocket messages sent and received by the `CommunicationService` via the `/ws/game` endpoint strictly adhere to a consistent JSON structure:

```json
{
  "type": "string",
  "id": "number",
  "data": "any (object or string)"
}
```

### The Binary CBOR Payload (`/ws/combat_log`)
To reduce bandwidth and serialization overhead during group encounters, high-frequency combat events are streamed via the `/ws/combat_log` endpoint using **CBOR** (Concise Binary Object Representation) instead of plain-text JSON. The Angular frontend handles the native decoding of the `ArrayBuffer` payloads using the `cbor-web` library.

### 1. `type` (String)
Dictates how the frontend should parse the message or how the backend should route the command.
* `text` - General server messages, room broadcasts, or dialogue.
* `character` - State updates for the active character (HP, Mana, inventory refresh).
* `logout` - Server-forced disconnects.
* (Additional custom event types route to specific Component state in Angular).

### 2. `id` (Number)
An optional identifier targeting the recipient or the origin.
* When broadcasting to everyone globally, the `id` is typically set to `-1`.
* When targeting a specific user/character, their internal ID is supplied to prevent cross-talk on the reactive streams.

### 3. `data` (Object/String)
The actual payload contents.

---

## Reactive `Flux/Mono` Pipeline
In the backend, all messages are passed through `Sinks.Many`. Incoming character commands are parsed by the `CommandService`, evaluated against the game loop, and the results are pushed down the Sink back to the frontend.

**Example Broadcast Flow:**
1. Player types `say hello`.
2. Frontend sends socket text `say hello`.
3. Backend `CommandService` splits the text, fires the `SayCommand`.
4. `CommunicationService.sendMessageToRoom(roomId, message)` pushes a JSON payload to the Sink.
5. All connected web clients in that room receive `{ "type": "text", "id": -1, "data": "Player says 'hello'" }`.
