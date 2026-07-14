# TASK_10 — Generate `server/openapi.yaml` from the code

Branch: `task/10-openapi-generation` · Depends on: TASK_3 · Protocol: [AGENT.md](AGENT.md)

## Goal

Make the code the **single source of truth** for the HTTP API documentation: the server's
routes carry their own OpenAPI documentation (summaries, parameters, request/response
schemas, examples, error responses, security), and `server/openapi.yaml` is **generated
from those route definitions** rather than hand-maintained. The generated document must
be as complete as possible — every operation fully described, with realistic examples —
and the checked-in `server/openapi.yaml` is a generated snapshot that CI verifies stays
in sync with the code.

This task runs **before** the token API (TASK_4) so that the generation infrastructure
and the **completeness bar** below are in place first: TASK_4 (and every later endpoint
task) documents its routes in code from the start, rather than hand-editing YAML that a
later task migrates. TASK_10 itself documents the only surface that exists at TASK_3
(`/health`, `/openapi.yaml`) and establishes the pattern.

This realizes the API-docs mandate in [AGENT.md](AGENT.md): after this task, "documenting
an endpoint" means annotating its route, and the Swagger document falls out automatically.

## Context

- TASK_3 created a **hand-written** `server/openapi.yaml`, packaged into the jar and
  served at `GET /openapi.yaml` (`server/src/main/.../Application.kt` loads it from
  resources). This task replaces that hand-maintenance with code-driven generation.
- HTTP surface to document at this point (the whole server API as of TASK_3):
  - `GET /health` — unauthenticated; `{ status, mongo }`; 200 with `mongo:false` when
    MongoDB is unreachable (the documented degraded case).
  - `GET /openapi.yaml` — the document itself (meta).
  - The bearer `syncSharedSecret` security scheme already declared in the document must
    be modelled in the generation setup so TASK_4's `/api/*` routes can apply it.
- TASK_4 then adds the token API (`PUT /api/users/tokens/apple-music`,
  `PUT /api/users/tokens/lastfm-session`, `GET /api/users/status`) and documents those
  routes **in code**, on this infrastructure, meeting the same completeness bar — it does
  not hand-edit `server/openapi.yaml`.
- **Recommended library:** the Ktor route-DSL generator
  [`io.github.smiley4:ktor-openapi`](https://github.com/SMILEY4/ktor-openapi-tools)
  (+ `ktor-swagger-ui` for the optional UI), which attaches documentation to routes and
  builds the schema from kotlinx-serialization types. Ktor's own `ktor-server-openapi`/
  `ktor-server-swagger` only *serve* a static spec and do not generate from routes, so
  they do not satisfy this goal. The library is a recommendation — if a better fit is
  found, update this spec in the task branch, note it in the PR, then implement.
- Add the chosen dependency to `gradle/libs.versions.toml` (new version + library
  entries) alongside the existing `ktor` entries.

## Requirements

1. **Routes are documented in code.** Every endpoint that exists at TASK_3 declares its
   OpenAPI documentation at the route (summary/description, tags, path/query/header
   parameters, response status(es) with schema, and the security requirement). Schemas
   come from the existing kotlinx-serialization types (`HealthResponse`), not re-declared
   by hand. The setup must make it straightforward for TASK_4 to add request-body schemas,
   header parameters, and reusable component schemas (`Session`, error) the same way.
2. **`server/openapi.yaml` is generated, not authored.** Provide a deterministic way to
   emit the document to `server/openapi.yaml` (e.g. a `:server:generateOpenApi` Gradle
   task or a test that writes it), documented in `server/README.md`. The served
   `GET /openapi.yaml` returns the **generated** document (drop the hand-written resource
   load); it stays valid OpenAPI 3.x and keeps the `application/yaml` content type.
3. **Drift guard in CI.** A `:server:test` check (or Gradle verification task wired into
   the build) regenerates the document and fails if it differs from the checked-in
   `server/openapi.yaml`, with a message telling the developer to regenerate. This
   preserves the "single checked-in document" mandate while keeping code authoritative.
4. **Completeness bar** — this is the standard the generated document must meet for
   **every operation**, and the bar TASK_4's endpoints must also satisfy. Each operation
   includes:
   - `summary` and `description`;
   - all parameters/headers (incl., where applicable, the bearer `Authorization` and
     `Music-User-Token`), each with a description and, where useful, an example;
   - request body schema **with at least one example** (where a body exists);
   - **every** response status the route can return (200 plus 400/401/404 as applicable),
     each with a schema and a realistic example (including the `/health` `mongo:false`
     degraded example);
   - the `securityScheme` (bearer `syncSharedSecret`) modelled in `components` and applied
     to secured routes, with `/health` and `/openapi.yaml` unauthenticated. **Implemented
     decision:** the `ktor-openapi` generator models the scheme in `components` (emitted
     whenever declared) but sets no document-level default security and emits **no**
     `security` node for unprotected routes (`protected = false`) — semantically identical to
     `security: []`. TASK_4 secures `/api/*` per-route via `protected = true` +
     `securitySchemeNames("syncSharedSecret")`; if TASK_4 instead introduces a **global**
     default scheme, it must then explicitly opt `/health` and `/openapi.yaml` back out.
   - reusable component schemas referenced via `$ref`, not inlined per operation.
   At TASK_3's surface this covers `/health` and `/openapi.yaml`; TASK_4 extends the same
   bar to the token payloads (`Session`, error) and `/api/*` security.
5. **Optional Swagger UI.** Serving interactive docs (e.g. at `GET /swagger`) is a
   nice-to-have; if added it must be unauthenticated and must not change the API.
6. **Mandate reconciliation.** Update `server/README.md` (how to regenerate; that the
   file is generated) and remove any now-stale "edit the yaml by hand" instructions from
   the server docs. The AGENT.md mandate already points here.
7. **Tests:** assert the generated document (a) is valid OpenAPI 3.x, (b) contains every
   path with its documented responses/examples/security, and (c) matches the checked-in
   snapshot (the drift guard). Keep the existing `/health` and `/openapi.yaml`
   `testApplication` behavior tests green.

## Non-goals

- No new endpoints and **no behavior change** to existing ones — documentation only.
- Not switching web frameworks; Ktor stays.
- No client-generation / SDK output from the spec (possible follow-up task).

## Validation

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home
./gradlew ktlintCheck
./gradlew :server:generateOpenApi   # (or the equivalent) — regenerates server/openapi.yaml
./gradlew :server:test              # includes the drift guard + completeness assertions
./gradlew :composeApp:assembleDebug # repo stays green
docker compose build
docker compose up -d --wait && curl -fsS localhost:8080/openapi.yaml && docker compose down
```

The served and checked-in documents must be identical and validate as OpenAPI 3.x.
Delete `spec/PROGRESS.md`, then open a PR to `feature/kotlin-server/base`.
