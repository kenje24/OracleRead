# Mihon upstream sync — design

Date: 2026-10-23
Status: awaiting review
Sub-project 1 of 2. Sub-project 2 (cinematic / immersive UI redesign) gets its own spec after this ships.

## Goal

Bring OracleRead up to date with Mihon `main` (481 commits behind as of 2026-10-23) while keeping every
OracleRead feature, branding element and user's data intact. Ship it as OracleRead **1.4.0**, with no new
UI work, so the redesign can start from a current, tested base.

## Context

- Fork point (merge-base): `509eee5df` "Use app scoped CoroutineScope (#3403)", 2026-06-12.
- OracleRead adds 12 commits on top (v1.0 UI and attribution through v1.3.9).
- Mihon changes since the fork point: 1,153 files. Structural changes include:
  - Injekt → Metro DI (#3608)
  - Material Symbols icons generated via Valkyrie (#3873, #4057)
  - Kitsu GraphQL API (#3792)
  - nightly build type / distribution profile build flags
  - Compose download queue
  - an app-migration screen
  - database migrations 14–19, which rename tables (`mangas`→`manga`, `chapters`→`chapter`, …), merge
    favorite fields into `favorite_at`, and store unknown times as NULL instants.
- 94 files are changed on both sides.
- **The current `main` (v1.3.9) no longer builds from source.** It depends on
  `com.github.arkon.FlexibleAdapter:flexible-adapter:c8013533` from JitPack, and JitPack reports that build as
  `Error / No build artifacts found` (checked 2026-10-23). Upstream no longer uses FlexibleAdapter (the
  download queue moved to Compose), so the sync removes this dependency. Until then, the published release
  APKs are the only runnable v1.3.9.

## Approach

Merge, not rebase or rewrite.

1. Add remote `upstream` = `https://github.com/mihonapp/mihon.git` (done).
2. Branch `sync/mihon-2026-10` from `main`.
3. `git merge upstream/main` into it, as one merge commit.
4. Port OracleRead features in follow-up commits on the same branch, one commit per feature, so a
   regression can be traced to a single feature.

Rebasing was rejected because it replays 12 conflicting commits and needs a force-push of published
history. A fresh re-implementation was rejected because it drops the git ancestry, so later syncs would
start from zero again.

## Conflict policy

**Where the two disagree on structure, upstream wins.** OracleRead behavior is re-implemented on top of
upstream's structure, not line-merged into it.

| OracleRead feature | Treatment |
|---|---|
| Branding: package name, app name, launcher icon, About screen, `ATTRIBUTION.md`, update checker targeting `kenje24/OracleRead` | Keep. Re-apply over the new build-flag / distribution setup. |
| Default extension repos (Keiyoushi, Yūzōnō) and repo fetching fix (`7bc288c5f`) | Port onto the upstream extension store code. |
| Community (Supabase account / friends: `mihon.feature.community`, `mihon.data.community`, `mihon.domain.community`) | Port to Metro DI. UI unchanged. |
| Library: featured card, ambient background, folder themes, source shortcuts, reading streak, `NavigationBarMode` | Port as is, for parity. The redesign reworks them later. |
| Ambient themes (Fire Spirits, Moon Wisps, Sea Lanterns, Sakura Drift, Aurora Motes, Ink Rain, Star Dust) | Port. |
| Reliability: `ExtensionCompatibility`, `MangaUpdateCoordinator` / `SMangaUpdate`, `MangaRefreshPolicy`, `ReaderPageRetryState`, network helper changes | Check each against upstream first. If upstream solves the same problem, keep upstream's version and drop ours. Otherwise port it with its tests. |
| Backports of upstream (`memo` column, migrations 12–13, `extension_list_url`) | Replaced by upstream's version. |

Every "upstream equivalent kept, ours dropped" decision is recorded in the merge notes / changelog.

## Database and upgrade safety

- Take upstream migrations 12–19 verbatim. Add no OracleRead-only migration.
- The schema of OracleRead's 12/13 matches upstream's 12/13. The extra `UPDATE mangas SET initialized = 0`
  in OracleRead's 13 has already run on existing installs and is not needed on fresh ones, so it is dropped.
  Existing installs (schema version 14) therefore continue through upstream's migrations 14→19.
- Backup format: upstream's models win. A `.tachibk` made by v1.3.9 must still restore.

## Verification

### Build and tests

- `gradlew assembleDebug` must succeed.
- Unit tests must pass, both upstream's and OracleRead's: `ExtensionCompatibilityTest`,
  `MangaUpdateCoordinatorTest`, `TachiyomiX16BinaryCompatibilityTest`, `MangaRefreshPolicyTest`,
  `ReaderPageRetryStateTest`, `NetworkLegacyExtensionRepoTest`, `NetworkHelperTest`,
  `ExtensionRuntimeDependenciesTest`. A dropped feature drops its test, with the reason noted.

### Upgrade test on the emulator (AVD `oracleread`, Android 15, Google APIs image)

v1.3.9 cannot be rebuilt (see Context), so the "before" app is the published
`OracleRead-release-1.3.9-x86_64.apk` (package `io.github.kenje24.oracleread`). Local 1.4.0 builds are debug
builds (package `….oracleread.dev`, debug key), so they cannot install over the release. The database is
moved across instead. The Google APIs image allows `adb root`, so no release keystore is needed.

1. Install the v1.3.9 release APK.
2. Add the Keiyoushi repo and one extension.
3. Add several manga across two categories.
4. Read chapters partway, bookmark one, and add a note.
5. Create a backup.
6. Stop the app. With `adb root`, copy its `databases/` directory and shared preferences into the
   1.4.0 debug app's data directory (fixing ownership), then launch 1.4.0. Migrations 14→19 run on real
   v1.3.9 data.
7. Library, categories, read/progress state, bookmarks, history and notes must all be intact.
8. On a fresh 1.4.0 install, restore the v1.3.9 backup. Same checks.
9. Optional, if the owner supplies the release keystore locally (never committed): build a signed 1.4.0
   release and install it over v1.3.9, the exact path real users take.

Any data loss or launch crash blocks the release.

### Smoke checks on the emulator

- Library: categories, featured card, folder themes, source shortcuts.
- Browse: repos load, install an extension, search, open a manga.
- Reader: paged and webtoon modes, page retry.
- Settings: theme picker, including the ambient themes.
- About screen and attribution, Community sign-in screen, update checker.

## Release

- Bump to version 1.4.0 and add a `CHANGELOG.md` entry covering three things:
  - the upstream changes
  - OracleRead features ported
  - OracleRead pieces replaced by upstream's versions
- All work stays local on `sync/mihon-2026-10`. Nothing is pushed, merged into `main`, tagged or published
  without the owner's explicit go-ahead.

## Out of scope

- Any visual or UX redesign (sub-project 2).
- New features.
- Changes to the Supabase backend.
