# MusicStreamSync — `:server`

Kotlin/JVM Ktor service that will sync each registered user's Apple Music play
history into Last.fm (spec suite in [`spec/`](../spec)). This module is developed,
tested, and deployed **with Docker** (see the Docker mandate in
[`spec/AGENT.md`](../spec/AGENT.md)).

## Configuration (environment variables)

| Variable | Default | Purpose |
| --- | --- | --- |
| `PORT` | `8080` | HTTP port. |
| `MONGODB_URI` | `mongodb://mongodb:27017/musicstreamsync` | MongoDB connection string; the path segment selects the database (`musicstreamsync` when absent). |
| `SYNC_SHARED_SECRET` | **none — required** | Bearer secret the mobile apps use against the token API (TASK_4+). Startup fails fast when unset. |
| `SYNC_INTERVAL_MINUTES` | `5` | Sync loop interval (consumed by TASK_6). |

## Running with Docker (primary environment)

```bash
# One-time: Arkana needs the secrets env file at the repo root
cp .env.sample .env   # then fill in the values, and add SYNC_SHARED_SECRET=<secret>

docker compose up --build --wait       # --wait blocks until healthchecks pass
curl -fsS localhost:8080/health        # {"status":"ok","mongo":true}
curl -fsS localhost:8080/openapi.yaml  # the API document
```

Both services declare healthchecks and `server` waits for `mongodb` to become
healthy (`depends_on: condition: service_healthy`), so `--wait` returns only once
`/health` is actually answering.

`docker compose` starts two services: `server` (built from
[`server/Dockerfile`](Dockerfile)) and `mongodb` (official image, named volume for
`/data/db`). `SYNC_SHARED_SECRET` is read from the shell environment or from `.env`
at the repo root — the same file Arkana uses, so one file carries both.

The image build regenerates the Arkana secrets module inside the container
(`gem install arkana` + `arkana -l kotlin`), so `.env` must be present in the build
context. The build stage runs **JDK 21** because the generated `arkana` module pins
`jvmToolchain(21)` and no toolchain resolver is configured; the runtime stage is a
slim **JRE 21** running as a non-root user.

## API document

The single OpenAPI document for this server is [`server/openapi.yaml`](openapi.yaml),
served at `GET /openapi.yaml` (as `application/yaml`). **It is generated from the
route definitions, not hand-edited** — the routes in
[`Application.kt`](src/main/kotlin/dev/igorcferreira/musicstreamsync/server/Application.kt)
carry their own OpenAPI documentation (summaries, schemas, examples, security) via the
[`ktor-openapi`](https://github.com/SMILEY4/ktor-openapi-tools) plugin, and the code is
the single source of truth.

To document an endpoint, annotate its route. When the HTTP surface changes, regenerate
the checked-in snapshot:

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home
./gradlew :server:generateOpenApi   # rewrites server/openapi.yaml from the code
```

`./gradlew :server:test` includes a drift guard that fails if `server/openapi.yaml` is
out of sync with the routes, so CI catches a stale snapshot. Do not edit
`server/openapi.yaml` by hand — it will be overwritten on the next regeneration.

## Health

`GET /health` (unauthenticated) always answers `200` while the process is up;
`"mongo"` reflects a live database ping and turns `false` when MongoDB is
unreachable — degradation is signaled in the payload rather than via 503.

## Building without Docker

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home
./gradlew :server:test          # unit tests (no MongoDB needed)
./gradlew :server:run           # needs SYNC_SHARED_SECRET and a reachable MongoDB
```
