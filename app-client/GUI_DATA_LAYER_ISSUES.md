# GUI / data-layer issues — punch list

Ordered by dependence (fix top to bottom; #3 is independent and can move anywhere).

## 1. `Session` / DAO cache divergence in `cacheOnly()` — FIXED (2026-08-30)

**Problem (confirmed):** `JsonUserDAO.cacheOnly()` refused to overwrite an existing cache entry. On a second login of
the same account on the same machine,
`switchUser()` reloads that user's old local snapshot into the DAO cache before
`cacheOnly()` tries to insert the fresh RMI-authenticated object — the insert was silently rejected (return value
ignored by `LoginController`; `RegisterController` did check it and would surface an error, but that path was never
actually reachable in practice — new registrations get a fresh random UUID, so the "already cached" check never fired
for genuine duplicates, only for the re-login case).

**Fix applied:** `cacheOnly()` now overwrites the existing entry (upsert), same pattern as `update()` — removes the old
username mapping first, then puts the fresh object under both maps. Zero caller changes needed.

**Side effect worth flagging:** `cacheOnly()` now always returns `true` for a non-null user, so
`RegisterController.onRegistratiClick()`'s `if (!cached) { ...error... }` branch is not reachable anywhere anymore —
is that intentional (keep as a defensive guard) or should it be removed? Not touched either way.

**Also not touched:** `JsonUserDAO.save(String username, ...)` (a sibling method, not `cacheOnly()`) has the identical
never-overwrite pattern at lines ~260-266 of the pre-fix file. It's not used/called anywhere in this session's traced
paths (GUI goes through `authService.register()` + `cacheOnly()` directly) — is that method still needed, and if so,
should it get the same fix?

## 2. Session persistence is write-only — FIXED (2026-08-30)

**Problem (confirmed):** `SessionRepository.save(user)` wrote to `data/session.txt`
on every login/register. `loadUsername()` existed to read it back but had no caller anywhere in the codebase. Result:
the GUI always started logged out, regardless of a prior session. `GUIController.initialize()`'s
`if (Session.isLoggedIn())` restore check never ran as a consequence, since `Session` was never populated before it.

**Fix applied, matching the decisions already made:**

- Session file is now `data/session.ini`:
  ```
  [session]
  userId=<uuid>
  ```
  Storing the user's ID, not username — matches real storage keyed by user ID (`data/<userId>/user.json`).
- `loadUsername()` replaced by `loadUserId()` returning `Optional<UUID>`.
- `GUIController.initialize()` calls a new `restoreSession()` before the existing `if (Session.isLoggedIn())` check:
  if nobody's logged in yet, `loadUserId()` → `dataStore.switchUser(id)` → try `customerDAO.findById(id)`, then
  `ownerDAO.findById(id)` → whichever hits → `Session.login(user)`.

**Verification level:** clean compile + a real headless launch under `Xvfb` (no exceptions, no crash). No local
`data/session.ini` + cached user existed on this machine to exercise the actual restore-and-log-in path end to end,
so that specific path is verified by code review against the existing `switchUser`/`findById` pattern (same one
`LoginController` already uses), not by a live restore test. Flagging this explicitly rather than claiming more than
was checked.

## 3. No pagination in the GUI lists

**Problem (confirmed):** `RestaurantsController` (both browse and owner mode), and `FavoritesController` all call bare
`findAll()` and dump everything into a
`ListView`. Only the CLI's `browseRestaurants()` pages.

**Decisions already made:**

- UI approach: a "Load More" button at the end of the list, not CLI-style page (P/N) navigation.
- Scope: `RestaurantsController`'s browse mode, `RestaurantsController.inizializzaProprietario()`
  (the real, reachable "I Miei Ristoranti" — not `MyRestaurantsController`, which is not used/called anywhere in the
  code; is that intentional or not?), and `FavoritesController`.
- Technical correction: no DAO in this codebase does real paged fetching —
  `JsonRestaurantDAO.findAll(offset, limit)` fetches everything and slices in memory. So all three screens implement
  "Load More" the same simple way, no special-casing between them needed.

**Still open:** exact page size. CLI's `browseRestaurants()` uses 20; not confirmed as the value to reuse here.

---

Not on this list, by explicit instruction: `MyRestaurantsController` / `MyRestaurants.fxml` — not used/called anywhere
in the code; is that intentional or not? Its disposition (delete, leave, or wire in) is unresolved and deliberately
excluded here.
