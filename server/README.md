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

docker compose up --build
curl -fsS localhost:8080/health        # {"status":"ok","mongo":true}
curl -fsS localhost:8080/openapi.yaml  # the API document
```

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
served at `GET /openapi.yaml`. Any task that adds or changes an endpoint must update
it in the same change.

## Health

`GET /health` (unauthenticated) always answers `200` while the process is up;
`"mongo"` reflects a live database ping and turns `false` when MongoDB is
unreachable — degradation is signaled in the payload rather than via 503.

## Building without Docker

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home
./gradlew :server:test          # unit tests (no MongoDB needed)
./gradlew :server:run           # needs SYNC_SHARED_SECRET and a reachable MongoDB
```
