# Mihon sync notes (2026-10)

OracleRead was merged with Mihon `main` at `d001e6f21` (481 upstream commits; fork point `509eee5df`). All
82 conflicts were resolved to upstream's version. OracleRead's features were then re-applied on top. This
file records each OracleRead change that was **replaced** by an upstream equivalent or **dropped**, with
the reason, so nothing disappears silently. Design: `docs/superpowers/specs/2026-10-23-mihon-sync-design.md`.

## Replaced by upstream

- REPLACED manga-update-coordinator: upstream's `mihon.domain.source.interactor.UpdateMangaFromRemote` (TachiyomiX 1.6 adaptation, #3243) routes every details/chapters fetch through `Source.getMangaUpdate` from all call sites, with the legacy fallback in `CatalogueSource`. The fork's per-manga mutex only wrapped the network fetch, not the chapter sync, so it never prevented a database race.
- REPLACED manga-refresh-policy: `MangaViewModel` decides chapter refresh inline. Upstream deliberately made update rules independent of `initialized` again (#4081), so `shouldRefreshChapters` had no caller left.
- REPLACED x16-binary-compat: upstream ships the TachiyomiX 1.6 API (`SMangaUpdate(manga, chapters)`, memo on `SManga`/`SChapter`) and keeps the public source API out of R8 (`source-api/consumer-proguard.pro`). The fork test asserted a no-argument constructor and nullable fields that the official 1.6 shape does not have.

- REPLACED extension-compat: upstream's `ExtensionLoader` now reads the `tachiyomix.*` metadata itself, rejects malformed APKs per extension (`loadExtensionCatching`), loads with `DelegateLastClassLoaderCompat` (#3874), and reloads safely when trust settings change. Upstream deliberately supports lib 1.4 and 1.6 only; the fork's extra 1.5 allowance is dropped with it.
- REPLACED network-helper-test: upstream removed `IgnoreGzipInterceptor`/`BrotliInterceptor` from the default client (#3458) and added zstd (#3465), so the fork test no longer applies. `ExtensionRuntimeDependenciesTest` is kept, because core/common still exposes `okhttp-zstd` to extensions.
- REPLACED legacy-repo-test: upstream's `ExtensionStoreService.fetchStore` follows `index.min.json` → `repo.json` and `index_v2` redirects itself (#3454), so the fork's model-level redirect test no longer describes the behaviour.

## Re-applied OracleRead behaviour

- Default extension stores (Keiyoushi, Yūzōnō): seeded once by `DefaultExtensionStoresMigration`, which runs on fresh installs too (upstream ships with no stores). A store the user removes stays removed.
- Stores saved by OracleRead 1.3.x as `…/index.min.json` keep loading: the legacy branch of `ExtensionStoreService.getExtensions` strips that suffix as well.

## Upstream fixes made while syncing

- `UpdateMangaFromRemote.invoke(manga, …)` accepted `fetchWindow` but did not pass it on, so smart-update windows were ignored on that path.
