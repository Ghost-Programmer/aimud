# AI Integration in AIMUD

This document provides a comprehensive overview of how Artificial Intelligence is integrated into the AIMUD project, detailing its components, models, and integration points.

## Overview
AIMUD strongly leverages large language models to deliver dynamic interactions and simplify development. Rather than relying on external API subscriptions, the project utilizes local, offline inferencing via **Ollama**, which is orchestrated directly through the project's `docker-compose.yml`.

The AI ecosystem in this project achieves two massive system goals:
1. **The Builder/Creator Agent**: An interactive tool-enabled assistant that allows game masters/admins to generate and manage world data (rooms, items, modules) through natural language.
2. **The NPC Conversational Engine**: An autonomous simulation loop that brings in-game Mobiles (NPCs) to life with contextual awareness and unscripted conversation.

## Models Used
Our Docker configuration automatically pulls down two primary models on startup:
* **`llama3.2`**: The primary intelligence engine. Used for complex task planning and function execution (Tool Calling) to act as the Builder assistant.
* **`hermes3`**: The natural language engine. Used to govern the behavioral and spoken output of NPCs due to its strong contextual and conversational alignment.

---

## 1. The Builder/Creator AI 

### Integration Stack
The Builder AI acts primarily as an administrator dashboard extension.

* **Frontend (`AiDialogComponent.ts` & `ai.service.ts`)**: 
  The UI offers a chat interface that sends requests to the backend. It uses HTML5 `fetch` to read the responsive streams via a `TextDecoder`. This implements Server-Sent Events (SSE) manually to map data chunks over RxJS observables, providing users with a rapid real-time typing effect.
  
* **Backend Endpoint (`AiController.java`)**: 
  Exposes the `POST /api/ai/prompt` request. Expects standard JSON payloads and produces `text/event-stream` using Project Reactor's `Flux<String>`.

* **The Engine (`AiService.java`)**: 
  Uses the `OllamaChatModel` from Spring AI. When evaluating prompts, the service fetches persisted system profiles ("Agents") from the database using the `ConfigService`. These database profiles are dynamically injected as `SystemMessage` headers to bias the AI's behavior before passing the context alongside the `UserMessage` to Ollama.

### Tool Calling & Model Context Protocol (MCP)
What empowers the Builder AI is the **`McpToolService.java`**. When the Builder AI is prompted, it is supplied with an array of Spring AI `@Tool` function callbacks. This capability allows the LLM to write directly to the game's database. 

It handles validation, constraints, and relational mappings autonomously. Its capabilities include:
* **Lookup Commands**: Retrieving enumerations of valid game constants such as wear locations, room types, or skill types.
* **Item Management**: Full creation, updates, and indexing of Items, including equipment statistics, properties, and stackability.
* **Mobile Management**: Generating NPCs, setting their abilities (strength, wisdom, etc.), and binding dynamic items directly into their inventory.
* **Room Connections**: Architecting physical space, generating descriptions, binding directional exits (North, South, Up, Down), and associating them physically in the world grid.
* **Store Management**: Linking merchants to commerce stores and populating valid item merchandising lists.

*Example:* A user types "Create a spooky graveyard room to the North of here, and put a skeleton in it holding a rusty sword." The model synthesizes the text, pauses its response, sequentially calls `createRoom`, `createItem`, and `createMobile`, and provides the exact DB mappings in real-time.

---

## 2. Dynamic NPC Conversational Engine

### Simulation Loop
Rather than hardcoded scripts, AIMUD NPCs evaluate real-time gameplay to decide how to converse or act. This is driven by `ConversationService.java`.

* **Triggers**: Real-time evaluation is triggered natively by the server tick (checking for idle NPCs every 10 seconds), or asynchronously when room events occur. 
* **Targeting**: The engine uses a tracking pool to ensure NPCs only compute responses when there are real, targeted situations—skipping logic if the NPC is dead, currently fighting, or if there are no human players in the room to hear them.

### Prompt Synthesis
When an NPC needs to determine what to do or say, the `ConversationService` builds an immense contextual snapshot behind the scenes to ship to the `hermes3` model:
1. **Identity & Environment**: It feeds the room's description, the NPC's name, class, intelligence, and charisma scores to give the model roleplay limits.
2. **Entity Scanning**: All players and other NPCs in the room are provided. It uses the `FactionService` to provide the model with a numeric integer of how much the NPC likes or hates each specific player, guaranteeing appropriate hostility or friendly banter.
3. **Action Availability Mapping**: The engine unpacks everything mechanically available to the NPC. Can it cast a fire spell? Sing a song? Use a disarm maneuver? Does it have standard emote permissions to wave or yawn? These are packaged as indexed numeric choices.
4. **Chat History**: The real-time, most recent dialogue broadcast to the room is injected into the context. 

### Execution and Safety Sandbox
To ensure reliability and security:
* The system utilizes the **`aiService.processPromptNoTools()`** method, which explicitly *detaches* the `McpToolService` from the Ollama session. An NPC *cannot* maliciously or accidentally invoke database modification methods during a simple conversation.
* The model is heavily constrained in its output format: It must format its response dynamically via strict string-integer parsing. 
* To resolve natural language structural hallucinations (like LLMs inventing names or adding quotation marks around output keys), it goes through a regex sanitizer before hitting the `commandService` to convert AI logic back into standard MUD commands (e.g., `say hello`, `cast fireball`, `emote glares at you`).
