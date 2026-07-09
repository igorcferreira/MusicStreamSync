# TASK_3 — `:server` module scaffold + Docker environment

Branch: `task/3-server-scaffold` · Depends on: — · Protocol: [AGENT.md](AGENT.md)

## Goal

Stand up the `:server` Gradle module (Ktor server, JVM) with configuration, health
check, the single Swagger document, and the Docker environment that all later server
tasks build on. **Docker is the primary dev/deploy environment** for this module.

## Context

- Register the module in `settings.gradle.kts` (follow the existing `include(":…")`
  list).
- The version catalog (`gradle/libs.versions.toml`) has Ktor 3.5.1 **client** artifacts
  only. Add server artifacts on the same `ktor` version ref:
  `ktor-server-core`, `ktor-server-netty`, `ktor-server-content-negotiation`,
  `ktor-serialization-kotlinx-json`, plus `logback-classic` and
  `mongodb-driver-kotlin-coroutine` (and `ktor-server-test-host` for tests).
- Dependencies: `:shared` (JVM target from TASK_1 — if TASK_1 isn't merged yet, this
  task may scaffold without depending on `:shared` and leave a TODO wired in TASK_5),
  `:lastfmapi`, `:arkana` (JVM-ready; provides `teamId`/`keyId`/`privateKey`/
  `lastFMAPIKey`/`lastFMAPISecret` — requires `arkana -l kotlin` before building, see
  root `CLAUDE.md`).
- Build convention: JDK 17, ktlint (`alias(libs.plugins.ktlint)` like the other
  modules).

## Requirements

1. **Module:** `server/build.gradle.kts` — Kotlin JVM (not multiplatform), application
   plugin with a `mainClass`, ktlint applied, JVM toolchain/target 17.
2. **Application:** Ktor server (Netty) entry point with:
   - JSON content negotiation (kotlinx-serialization).
   - Config from environment variables (with defaults where sane):
     `PORT` (default 8080), `MONGODB_URI` (default
     `mongodb://mongodb:27017/musicstreamsync`), `SYNC_SHARED_SECRET` (no default —
     fail fast with a clear message when unset), `SYNC_INTERVAL_MINUTES` (default 5;
     consumed by TASK_6).
   - Structured logging via logback (console appender, no file appender).
3. **MongoDB client:** created at startup from `MONGODB_URI` (official Kotlin coroutine
   driver), exposed to routes via DI-by-constructor (no service-locator framework).
4. **`GET /health`:** returns 200 with `{ "status": "ok", "mongo": true|false }`, where
   `mongo` reflects a live ping to the database. Unauthenticated.
5. **Swagger document:** create `server/openapi.yaml` (OpenAPI 3.x) — servers, the
   bearer shared-secret `securityScheme` (used from TASK_4 on), and the `/health` path.
   Serve the file at `GET /openapi.yaml`. This is the **single** API document all later
   tasks extend (see the mandate in [AGENT.md](AGENT.md)).
6. **Docker:**
   - `server/Dockerfile`: multi-stage — stage 1 builds with a Gradle + JDK 17 image
     (`gradle :server:installDist` or shadow jar; note the Arkana generation step),
     stage 2 runs on a slim JRE 17 image as a non-root user.
   - `docker-compose.yml` (repo root): services `server` (build from `server/Dockerfile`,
     ports `8080:8080`, env from `.env`/environment) and `mongodb` (official `mongo`
     image, named volume for `/data/db`). Server depends_on mongodb.
   - Secrets/config flow through env vars; document them in `server/README.md` along
     with `docker compose up` usage.
7. **Tests:** `:server:test` with Ktor `testApplication` covering `/health` (mongo up →
   `mongo: true` via a fake/ping stub; mongo unreachable → still 200 with
   `mongo: false` or 503 — pick one, document it in `openapi.yaml`).

## Non-goals

- No token endpoints, no `UserStore` (TASK_4).
- No sync engine or scheduler (TASK_5/6).

## Validation

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home
./gradlew ktlintCheck
./gradlew :server:test
./gradlew :composeApp:assembleDebug        # repo stays green
docker compose build
docker compose up -d && curl -fsS localhost:8080/health && curl -fsS localhost:8080/openapi.yaml && docker compose down
```

Delete `spec/PROGRESS.md`, then open a PR to `feature/kotlin_server`.
