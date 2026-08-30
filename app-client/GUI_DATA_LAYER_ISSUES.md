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

## 3. No pagination in the GUI lists — FIXED (2026-08-30)

**Problem (confirmed):** `RestaurantsController` (both browse and owner mode), and `FavoritesController` all called
bare `findAll()` and dumped everything into a `ListView`. Only the CLI's `browseRestaurants()` paged.

**Fix applied, matching the decisions already made:**

- Added a "Load More" button (`btnCaricaAltri` / `loadMoreBar`) below the list in `Restaurants.fxml` and
  `Favorites.fxml`, hidden/unmanaged when there's nothing more to show.
- Scope: `RestaurantsController`'s browse mode, `RestaurantsController.inizializzaProprietario()` (the real,
  reachable "I Miei Ristoranti" - `MyRestaurantsController`, not used/called anywhere in the code, was not touched),
  and `FavoritesController`.
- Both controllers now keep the full result list in memory (`risultatiCompleti`) and a `visibleCount` cursor starting
  at `PAGE_SIZE`; the `ListView` only ever shows `risultatiCompleti.subList(0, visibleCount)`. Clicking "Load More"
  bumps `visibleCount` by `PAGE_SIZE` and re-renders. No DAO does real paged fetching, so this is client-side slicing
  of an already-fully-fetched list, same as the rest of the codebase.
- Page size: went with `PAGE_SIZE = 20`, matching the CLI's `browseRestaurants()` default, since no other value was
  specified. Open to changing it.

**Verification:** clean compile, plus a standalone `FXMLLoader.load()` smoke test (JavaFX `Application`, no full app
launch needed) confirmed both `Restaurants.fxml` and `Favorites.fxml` parse and bind to the updated controllers
without error - not just "it compiled."

---

Not on this list, by explicit instruction: `MyRestaurantsController` / `MyRestaurants.fxml` — not used/called anywhere
in the code; is that intentional or not? Its disposition (delete, leave, or wire in) is unresolved and deliberately
excluded here.
