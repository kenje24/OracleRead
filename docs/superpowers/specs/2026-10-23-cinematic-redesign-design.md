# OracleRead cinematic redesign — design

Date: 2026-10-23
Status: in implementation
Sub-project 2 of 2. Builds on the Mihon sync (`2026-10-23-mihon-sync-design.md`).

## Intent

The owner wants OracleRead to stop feeling like Mihon. The direction they chose is **cinematic / immersive**:
dark-first, cover art as the hero, large artwork, glass-like surfaces and smooth motion, closer to a
streaming app than a file browser.

Constraints carried over:
- Same assets: name, launcher icon and logo, lineage/attribution, extension repos.
- All features and workflows keep working.
- Existing themes stay selectable.
- Reading speed and accessibility are not traded away: contrast, reduced motion, content descriptions.

## Identity

The palette is derived from the existing logo assets: slate blue `#5675A0` and ivory `#EEE7D7`. A warm
"lantern" gold is added as the cinematic accent.

### Default theme: "OracleRead" (new `AppTheme.ORACLE`, the new default)

Dark:

| role | value |
|---|---|
| background / surface | `#0A0D13` |
| surface containers (lowest → highest) | `#06080C` · `#10141C` · `#151A23` · `#1B212C` · `#232A37` |
| on-surface / on-surface-variant | `#ECE6D8` / `#A7AEBC` |
| primary (lantern gold) | `#E9C987` (on `#2A1F05`, container `#3D3218` / on `#F7E2B0`) |
| secondary (logo slate) | `#8FAAD3` (container `#24324A` / on `#D6E2F7`) |
| tertiary (ember) | `#E79C8A` (container `#4A2620` / on `#FFDAD2`) |
| outline / outline-variant | `#3A4252` / `#262D3A` |

Light ("daylight paper"):

| role | value |
|---|---|
| background | `#F6F2EA` |
| on-surface | `#1A1C21` |
| primary | `#7A5A12` |
| secondary | `#3E5F8E` |
| tertiary | `#9A4A3B` |

### Cover-driven color

The manga detail screen and the library hero derive a scheme from the cover's dominant color, using
`materialkolor` (already a Mihon dependency). The derived scheme is applied only inside that surface, so the
rest of the app stays consistent.

### Type

| roles | face | details |
|---|---|---|
| display / headline | **Fraunces**, a soft serif (variable `opsz`/`wght`/`SOFT`) | weight 600–650 |
| title / body / label | **Manrope** (variable `wght`) | titles 700/600, body 400/500, labels 600 with +0.02em tracking |

Both are bundled under the SIL OFL; the licenses ship next to the fonts.

### Shape and space

- Corner radii: extraSmall 8, small 12, medium 18, large 26, extraLarge 34.
- Covers use 14dp corners.
- Spacing grid is 4dp. Page gutter is 20dp; section gap is 28dp.

### Motion

- Spring-based (`MaterialTheme.motionScheme` expressive) and fade-through between tabs.
- The ambient particle themes keep working.
- Every looping or ambient animation is disabled when the system animator duration scale is 0 (reduced
  motion).

## Components (`eu.kanade.presentation.oracle`)

| component | purpose |
|---|---|
| `CinematicBackdrop` | Blurred, scaled cover image (API 31+ `Modifier.blur`, below that a heavy scrim) with a vertical gradient into the background. |
| `OracleDock` | Floating pill navigation bar: translucent container, hairline outline, the selected item expands with its label. Tablets get the same language as a rail. |
| `OracleHero` | Full-width continue-reading hero: backdrop, poster, display-font title, chapter/progress line, Resume button. Replaces the fork's FeaturedMangaCard. |
| `OraclePoster` | Cover card: 14dp corners, bottom gradient, title below, unread/download pills. |
| `OracleSectionHeader` | Display-font section title with optional action. |
| `OraclePillTabs` | Category tabs as pills: folder accent color, folder icon and count, so color is never the only cue. |
| `OracleGlass` | Translucent elevated container with a hairline border, for bars, sheets and chips. |
| `OracleLargeTopBar` | Transparent large-title top bar in the display face that condenses on scroll. |

## Surfaces

| surface | redesign |
|---|---|
| Navigation shell (`HomeScreen`) | `OracleDock` replaces the Material NavigationBar. Content draws behind it. Rail on wide screens. The compact/5-tab `NavigationBarMode` is kept. |
| Library | Large "Library" title; reading-streak chip; `OracleHero` for the most recent in-progress title (featured preference kept); source-shortcut row; pill category tabs with folder themes; poster grid; ambient folder background. |
| Manga detail | Full-bleed `CinematicBackdrop` from the cover with the cover-derived scheme; floating poster; display title; author/status/source meta chips; large Start/Continue button; action row (library, tracking, WebView, share, community); expandable synopsis; genre pills; chapter rows as cards with clear read and downloaded states. |
| Updates / History | Day-grouped timelines with display-font day headers and larger covers, in glass cards. |
| Browse | Pill segmented tabs; sources and extensions as cards with icon tiles; prominent search field. |
| More / Settings / About | Hero header with the OracleRead logo and name; settings grouped in rounded cards. About carries the credits, the lineage (Tachiyomi → Mihon → OracleRead), the legal notice, the OracleRead repo and the Mihon link. |
| Reader chrome | Glass top and bottom bars, rounded page slider, pill buttons. Reading surfaces are untouched. |
| Onboarding | Cinematic welcome: logo with a soft glow on the night background, display-font headline, steps in glass cards. |

## Verification

- R1: design system exists and `ORACLE` is the default theme.
- R2: each surface file uses the new components.
- R3: fresh emulator screenshots of each surface, differing from the v1.3.9 baseline.
- R4: docs updated.
- M1: owner review.
- S6/S7 stay green.
