# AIMud 🗡️ 🧙‍♂️

Welcome to **AIMud**! AIMud is a modern, full-stack, AI-driven Multi-User Dungeon (MUD) built completely from the ground up using **Spring Boot WebFlux** for an ultra-fast reactive backend engine and **Angular** for a premium, interactive frontend client.

AIMud actively integrates local Large Language Models via Ollama to generate immersive NPC behavior, natural language processing, dynamic world-building, and procedural interactions alongside traditional MUD mechanics like Spells, Songs, Prayers, Combat, and Exploration.

---

## 🛠️ Tech Stack & Requirements

To run this project locally, make sure you have the following installed on your machine:
- **Java 25+** (The project runs on the latest Java LTS build utilizing standard Records and modern idioms).
- **Node.js** (v18+ recommended) and **npm** for Angular frontend development.
- **Docker Desktop** (To orchestrate our Postgres database and our Ollama LLM container).

---

## 🚀 Setup & Execution

### 1. Spin up the infrastructure
You need to have the database and the AI backend running locally before the server can initialize. Start the containers using our combined Docker Compose setup:
```bash
docker compose up -d
```
*(This pulls down and runs a local Postgres database and an Ollama instance).*

### 2. Run the Engine
The backend engine is tightly integrated with our frontend compilation task. When you run the Spring backend using Gradle, it automatically builds the Angular frontend (`buildFrontend` task) and serves it out of the static assets folder.

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

* [**Magic Abilities & Spell Roster**](./docs/abilities.md) — A comprehensive table outlining every single Magic Spell, Bard Song, and Divine Prayer natively available grouped by required level.
* [**Combat & Utility Abilities**](./docs/combat_abilities.md) — A breakdown of all traditional, non-magical abilities (like Pickpocket, Bash, Backstab, etc.) that do not use level restrictions and scale from Rank 1 natively.

---

## 🤝 Architecture Notes
* **Data Flow**: Reactive (`Mono`/`Flux`) architecture using Spring WebFlux communicating locally via `docker-compose` routing. 
* **Live Game Loop**: Governed centrally via `TickService` executing every 2.0s without persistent DB blocking. All states run primarily in memory (`CharacterService.availableCharacters`), broadcasting via Reactor Sinks to `/ws/game`. 
* **Persistence & State**: Liquibase authoritative migrations are loaded seamlessly out of `db/changelog`.
