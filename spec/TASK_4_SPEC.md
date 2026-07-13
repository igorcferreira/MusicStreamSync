# TASK_4 — Multi-user token-sync API + MongoDB persistence

Branch: `task/4-token-sync-api` · Depends on: TASK_2, TASK_3 · Protocol: [AGENT.md](AGENT.md)

## Goal

Give the native apps a server API to register users and push their credentials: the
Apple Music **Music-User-Token** and the Last.fm **session**. Persist per-user state in
MongoDB. This API is the contract TASK_7 (shared client use case) consumes.

## Context

- Server scaffold, Mongo client, env config, and `server/openapi.yaml` exist (TASK_3).
- `Session` is `@Serializable` with `name`, `key`, and `subscriber` (defaulted to 0 by
  TASK_2 — the wire shape documented there is authoritative) and importable into a
  `LastFMClient` (TASK_2).
- **User identity:** Apple provides no server-side login; the Music-User-Token is the
  only credential. The public Apple Music API has no obvious "get user id" endpoint, so
  this task must fix the mechanism:
  1. *Investigate first:* whether any Apple Music API response reachable with the token
     yields a stable per-user identifier (e.g. a `meta`/library identifier; the
     storefront endpoint `GET /v1/me/storefront` identifies the region, not the user).
  2. *Fallback (must be implemented regardless, used when no API id exists):* a
     token-derived identifier — SHA-256 hash of the Music-User-Token. Document the
     consequence: if Apple rotates the token, the hash changes and the upload creates a
     new user document; mitigate by having the apps send their previous token hash (or
     a client-generated install id) so the server can migrate the document. Record the
     chosen mechanism in this spec file (update it in the task branch) and in
     `server/openapi.yaml` descriptions.
  3. *Multi-device caveat:* Music-User-Tokens are minted **per device**, so the same
     person on Android + iOS yields two token hashes → two user documents syncing the
     same accounts (duplicate-scrobble risk). The investigation must explicitly
     consider keying — or at least deduplicating/merging — users by the Last.fm session
     `name` once it is pushed (the one stable cross-device identifier the server
     receives), and document the residual risk if not adopted.

## Requirements

1. **`UserStore` repository** (constructor-injected `MongoDatabase`, coroutine driver),
   `users` collection keyed by user id, document fields:
   - `userId` (string, see identity mechanism), `musicUserToken` (string),
     `lastFmSession` (the TASK_2 `Session` wire shape, nullable until pushed),
     `syncCursor` (nullable **ordered list of entryIds** — the top-K prefix cursor
     defined in TASK_5, written by TASK_5),
     `tokenStale` (bool, default false), `createdAt`/`updatedAt`,
     `lastSync` (nullable: `{ at, result, scrobbledCount, error? }`),
     `syncLog` (capped list of recent run summaries).
   - Suspend CRUD used by this task + TASK_5/6: upsert user, get by id, list all,
     update cursor, mark token stale, append sync-run log.
   - **Update semantics:** all writes are field-level (`$set` / `$setOnInsert`), never
     whole-document replaces. A token push must never touch `syncCursor`, `lastSync`,
     or `syncLog` — the API handlers race the scheduler loop (TASK_6), and per-field
     atomic updates are the concurrency answer.
2. **Auth:** all `/api/*` routes require `Authorization: Bearer <SYNC_SHARED_SECRET>`
   (constant-time comparison). 401 otherwise. The acting user is resolved from the
   `Music-User-Token` header sent with each request (the identity mechanism above) —
   no separate account/password system.
3. **Endpoints** (JSON, documented in `server/openapi.yaml` in this same task):
   - `PUT /api/users/tokens/apple-music` — headers: bearer secret; body:
     `{ "musicUserToken": "...", "previousTokenHash": "...?" }`. Resolves/derives the
     user id, upserts the user document (migrating from `previousTokenHash` when
     present), clears `tokenStale`. 200 → `{ "userId": "..." }`.
   - `PUT /api/users/tokens/lastfm-session` — headers: bearer secret +
     `Music-User-Token`; body: the serialized `Session` per TASK_2's wire shape
     (`{ "name": "...", "key": "...", "subscriber": 0? }`).
     404 if the user isn't registered yet. 200 on stored.
   - `GET /api/users/status` — headers: bearer secret + `Music-User-Token`. Returns
     `{ "userId", "registered": true, "hasLastFmSession", "tokenStale", "lastSync": {...}? }`;
     404 when unknown.
4. **Validation/errors:** malformed bodies → 400 with a JSON error shape (define it once
   in `openapi.yaml` and reuse); secrets never logged; Music-User-Token only ever logged
   as its hash.
5. **OpenAPI:** `server/openapi.yaml` updated with the three paths, request/response
   schemas (reuse the `Session` shape), the bearer scheme, and error responses. Task is
   not done until the document matches the routes.
   > Note: TASK_10 later regenerates this document from the route definitions. Keep the
   > hand-written entries complete and accurate (schemas, examples, error responses) so
   > that migration is a faithful snapshot.
6. **Tests:** Ktor `testApplication` route tests against an in-memory `UserStore` fake
   (interface + fake) for auth, happy paths, 400/401/404; plus `UserStore` integration
   tests against a real Mongo (Testcontainers, or the compose `mongodb` service with a
   test profile) covering upsert/migration/cursor update.

## Non-goals

- No sync execution (TASK_5/6) — this task only stores state.
- No client-side code (TASK_7).
- No admin/list-users endpoint (add a follow-up task if needed).

## Validation

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home
./gradlew ktlintCheck
./gradlew :server:test
docker compose build
docker compose up -d
# smoke: register a user, push a session, read status (use a test secret from .env)
curl -fsS -X PUT localhost:8080/api/users/tokens/apple-music \
  -H "Authorization: Bearer $SYNC_SHARED_SECRET" -H "Content-Type: application/json" \
  -d '{"musicUserToken":"smoke-token"}'
docker compose down
```

Delete `spec/PROGRESS.md`, then open a PR to `feature/kotlin-server/base`.
