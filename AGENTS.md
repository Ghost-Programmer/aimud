# AGENTS.md

## Project snapshot

- Full-stack AI MUD app: Spring Boot WebFlux backend + Angular frontend.
- Backend source: `src/main/java/com/aimud/aimud`; frontend source: `frontend/src/app`.
- Runtime dependencies are local Postgres + Ollama from `docker-compose.yml`.

## Architecture and data flow

- HTTP API is reactive (`Mono`/`Flux`) and mostly in `controller/*` -> `service/*` -> `repository/*`.
- Authentication flow: `/api/users/login` returns JWT, frontend stores it in `localStorage` key `token` (`frontend/src/app/interceptors/auth.interceptor.ts`).
- Security is configured in `src/main/java/com/aimud/aimud/config/SecurityConfiguration.java` with explicit public routes and JWT filter insertion.
- Live game loop is scheduled in `src/main/java/com/aimud/aimud/service/TickService.java` with a multi-tiered virtual thread scheduler (`FastTick` at 1s for combat/actions, `SlowTick` at 5s for regen/weather); active player state is in-memory (`MobileService.getAvailableCharacters`), not DB-only.
- Real-time updates go through Reactor sinks (`CommunicationService`) to `/ws/game` for JSON events and `/ws/combat_log` for binary CBOR events (`GameWebSocketHandler` and `CombatLogWebSocketHandler`).
- WebSocket payload contract for standard events is JSON `{ type, id, data }` where `type` is `character|text|logout|party`; broadcast text uses `id: -1`. High-throughput combat logs use compressed binary CBOR payloads.

## AI + MCP integration

- AI streaming endpoint: `POST /api/ai/prompt` with `text/event-stream` (`AiController`, `AiService`).
- Frontend consumes SSE manually via `fetch` reader (`frontend/src/app/services/ai.service.ts`), expecting `data:` lines.
- AI system prompt is persisted in `server_settings.ai_system_prompt` and surfaced by `ConfigService`.
- MCP tools are Spring AI `@Tool` methods in `src/main/java/com/aimud/aimud/service/McpToolService.java` for rooms/items/effects.

## Persistence and schema patterns

- Liquibase is authoritative (`src/main/resources/db/changelog/db.changelog-master.yaml` -> `changes/000-consolidated.sql`).
- Default world/config seed data (rooms, classes, races, server settings) lives in the consolidated SQL changeset.
- Soft-delete is used for races/classes (`deleted` flag), so list endpoints filter rather than hard-delete (`ConfigService`).
- Some relations are managed with explicit SQL (`CharacterService.updateInventory` uses `DatabaseClient` on `character_inventory`).
- **Memory-Centric World State:** We are NOT persisting things across server restarts for non-player entities. Therefore, we do not save rooms, items, or NPC Mobiles; we only save Player mobiles. Any changes to the world state (items dropped in rooms, containers, NPC states) exist only in memory and reset upon server restart. Initial item nesting (e.g. containers) is achieved via `container_item_id` in the `items` table.

## Developer workflows (project-specific)

- Backend build implicitly builds frontend: `processResources` depends on Gradle `buildFrontend` task (`build.gradle`).
- Frontend output copied into backend static assets from `frontend/dist/frontend/browser` at build time.
- Angular dev server uses proxy rules in `frontend/proxy.conf.json` for `/api` and `/ws` to `localhost:8080`.
- Use these common commands from repo root:
  - `./gradlew.bat bootRun`
  - `./gradlew.bat test`
  - `docker compose up -d`
  - `cd frontend; npm install; npm start`

## Conventions to follow when editing

- Keep reactive return types through controller/service boundaries; avoid introducing blocking calls in request flow.
- Existing API responses are mixed: typed entities and ad-hoc maps (e.g., `RoomController`, `UserController`) - match local style in touched file.
- New player commands should implement `commands.Command`, be annotated with `@MudCommand(name = "...")`, and rely on auto-registration in `CommandService`.
- Note package typo is intentional in current code: `com.aimud.aimud.annontation` (do not silently rename).
- Frontend service URLs are relative (`/api/...`) and rely on proxy/static hosting; avoid hardcoded hosts.
- All classes and methods should have comprehensive Javadoc comments.
- Keep README.md up to date with any changes to the project.
- Keep AGENTS.md up to date with any changes to the project.
- Keep docker-compose.yml up to date with any changes to the project.
- Keep documents in the docs folder up to date with any changes to the project.
