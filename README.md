# AIMud 🗡️ 🧙‍♂️

Welcome to **AIMud**! AIMud is a modern, full-stack, AI-driven Multi-User Dungeon (MUD) built completely from the ground up using **Spring Boot WebFlux** for an ultra-fast reactive backend engine and **Angular** for a premium, interactive frontend client.

AIMud actively integrates local Large Language Models via Ollama to generate immersive NPC behavior, natural language processing, dynamic world-building, and procedural interactions alongside traditional MUD mechanics like Spells, Songs, Prayers, Combat, and Exploration.

## 🤖 AI Integration Overview

Unlike traditional MUDs reliant heavily on static scripts, AIMud utilizes offline inferencing via **Ollama** running locally alongside the database. The system uses a dual-model approach to fundamentally change how game masters operate and how NPCs interact within the simulation:

1. **The Builder/Creator Agent (`llama3.2`)**: An administrative tool-calling LLM that can dynamically edit the database in real-time. It understands tool callbacks via the Model Context Protocol (MCP) and evaluates natural language commands to rapidly prototype rooms, generate NPCs, distribute items, and manage merchants.
2. **The NPC Conversational Engine (`hermes3`)**: A deterministic, sandboxed system evaluating the active game state frequently. Depending on player presence, faction hostility levels, chat history, and identity rules, NPCs autonomously choose to use abilities, communicate intelligently, or physically attack without requiring any pre-authored chat scripts.

For a full technical deep dive into these systems, see our dedicated [AI Integration Documentation](./docs/AI.md).

---

## 🛠️ Tech Stack & Requirements

To run this project locally, make sure you have the following installed on your machine:
- **Java 25+** (The project runs on the latest Java LTS build utilizing standard Records and modern idioms).
- **Node.js** (v18+ recommended) and **npm** for Angular frontend development.
- **Docker Desktop** (To orchestrate our Postgres database and our Ollama LLM container).

---

## 🚀 Setup & Execution

You can run the application fully containerized (best for trying it out), or run the infrastructure in Docker and the engine locally (best for development).

### Option A: Fully Dockerized Setup (Easiest)
Because our `docker-compose.yml` includes the Java backend as a defined service, you can build and start the entire stack (Postgres, Ollama, Open-WebUI, and the AIMud app) with a single command:
```bash
docker compose up --build -d
```
Once the containers are running and the initial model pull is complete, access the client by visiting **`http://localhost:8080/`**.

### Option B: Local Development Setup
If you are modifying code, you'll want to run the Spring Boot engine locally to avoid rebuilding the Docker image on every change.

**1. Spin up only the infrastructure**
Start *only* the database and AI backend containers so the app port (8080) remains free:

```bash
docker compose up -d postgres ollama
```

**2. Run the Engine**
When you run the Spring backend using Gradle, it automatically builds the Angular frontend (`buildFrontend` task) and serves it out of the static assets folder.

From the repository root (e.g., using PowerShell/Command Prompt on Windows):
```bash
./gradlew.bat bootRun
```
Access the client by visiting **`http://localhost:8080/`**.

### 3. (Optional) Frontend Development Mode
If you are iterating heavily on the UI, it's faster to run the Angular dev server directly. This proxies any `/api` and `/ws` network calls cleanly over to the backend engine running on port 8080.

Open a second terminal, jump into the frontend directory, and run the hot-reload server:
```bash
cd frontend
npm install
npm start
```
Access the dev server by visiting **`http://localhost:4200/`**.

---

## 🧪 Testing

To ensure stability across the engine loops, reactive streams, and database states, run the entire test suite using Gradle:
```bash
./gradlew.bat test
```
All Unit Tests and `IntegrationTests` utilize Mockito and `reactor-test` natively. 

---

## 📚 Documentation

Detailed documentation on classes, mechanics, scaling factors, abilities, and in-game magic can be found in our `/docs/` repository. 

* [**AI Integration Document**](./docs/AI.md) — Complete overview of AI model execution, tool-calling capabilities, and NPC conversational integration.
* [**Player Dashboard Guide**](./docs/player-dashboard.md) — Walkthrough of the Reactivity of the frontend module, tab controls, and standard active play components.
* [**Admin Dashboard Guide**](./docs/admin-dashboard.md) — Complete capabilities suite for MUD_ADMIN interactions, LLM configuration hooks, and real-time world-building.
* [**WebSocket & API Protocol**](./docs/websocket-protocol.md) — Breakdown of the JSON payload contract and real-time streaming architecture.
* [**Player Commands Reference**](./docs/commands.md) — Complete directory of all valid movements, combat maneuvers, magical commands, and roleplay emotes.
* [**Database Schema Overview**](./docs/database-schema.md) — Relational mappings, PostgreSQL layout, and explicit enumerated validations.
* [**World Building Guide**](./docs/world-building.md) — How to append new maps and entities either manually via Liquibase or natively via the AI Builder assistant.
* [**Magic Abilities & Spell Roster**](./docs/abilities.md) — A comprehensive table outlining every single Magic Spell, Bard Song, and Divine Prayer natively available grouped by required level.
* [**Combat & Utility Abilities**](./docs/combat_abilities.md) — A breakdown of all traditional, non-magical abilities (like Pickpocket, Bash, Backstab, etc.) that do not use level restrictions and scale from Rank 1 natively.

---

## 🤝 Architecture Notes
* **Data Flow**: Reactive (`Mono`/`Flux`) architecture using Spring WebFlux communicating locally via `docker-compose` routing. 
* **Live Game Loop**: Governed centrally via `TickService`, split into a multi-tiered virtual thread scheduler (`FastTick` at 1s, `SlowTick` at 5s) without persistent DB blocking. All states run primarily in memory (`MobileService.getAvailableCharacters`), broadcasting via Reactor Sinks to `/ws/game` (JSON) and `/ws/combat_log` (binary CBOR). 
* **Persistence & State**: Liquibase authoritative migrations are loaded seamlessly out of `db/changelog`.
