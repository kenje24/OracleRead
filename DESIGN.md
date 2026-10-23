# OracleRead Design

OracleRead's visual language is **cinematic and immersive**: a dark ink-night canvas, cover art as the hero, glass
surfaces floating over artwork, and serif title cards. This file describes the system as it is built. The design
rationale is in `docs/superpowers/specs/2026-10-23-cinematic-redesign-design.md`, and the product principles are in
`PRODUCT.md`.

## Brand assets

The OracleRead logo (`app/src/main/res/drawable/ic_mihon.xml`) and the launcher icon
(`ic_launcher_{background,foreground,monochrome}.xml`) are unchanged. The palette is derived from them:
- slate blue `#5675A0`
- ivory `#EEE7D7`

## Color

The default theme is `AppTheme.ORACLE`. It is defined in two places: `OracleColorScheme` for Compose, and
`colors_oracle.xml` with `Theme.Tachiyomi.Oracle` for View-based screens such as the reader. New installs start in
dark mode.

| Role | Dark (primary experience) | Light ("daylight paper") |
|---|---|---|
| Background / surface | `#0A0D13` ink night | `#F6F2EA` |
| Surface containers | `#06080C` `#10141C` `#151A23` `#1B212C` `#232A37` | `#FFFFFF` `#F1ECE2` `#ECE6DB` `#E6E0D4` `#E0D9CC` |
| On surface / variant | `#ECE6D8` / `#A7AEBC` | `#1A1C21` / `#4A4F5A` |
| Primary (lantern gold) | `#E9C987` | `#7A5A12` |
| Secondary (logo slate) | `#8FAAD3` | `#3E5F8E` |
| Tertiary (ember) | `#E79C8A` | `#9A4A3B` |
| Outline / variant | `#5C6577` / `#262D3A` | `#7A7F8A` / `#CFC8BA` |

**Cover-driven color.** `rememberCoverColorScheme` and `CoverColorTheme` extract the dominant, saturation-weighted
color of a cover and tint only the accents: primary, secondary and tertiary plus their containers. Surfaces stay on
the night canvas, so every manga page has its own accent without the app losing its identity. Used on manga detail
and the library hero.

Every legacy theme (Catppuccin, Nord, Tako, the ambient themes and the rest) remains selectable. The type, shape and
component system applies under all of them.

## Type

| Roles | Face | Notes |
|---|---|---|
| display\*, headline\* | **Fraunces** (variable, SIL OFL) | SemiBold, `SOFT 60`, optical size 72 for display and 28 for headline; slight negative tracking |
| title\*, body\*, label\* | **Manrope** (variable, SIL OFL) | titles Bold/SemiBold, body Regular/Medium, labels SemiBold/Bold with positive tracking |

Source: `presentation-core/.../theme/OracleTypography.kt`. The fonts live in `presentation-core/src/main/res/font/`
and their licenses in `presentation-core/src/main/assets/licenses/`.

## Shape and space

- Shape scale: extraSmall 8, small 12, medium 18, large 26, extraLarge 34.
- Covers use `OracleCoverShape` (14dp).
- Spacing is a 4dp grid (`OracleSpacing`): page gutter 20dp, section gap 28dp.

## Components (`app/src/main/java/eu/kanade/presentation/oracle/`)

| Component | Use |
|---|---|
| `CinematicBackdrop` | Full-bleed blurred, enlarged cover (blur on Android 12+, a heavier scrim before that) fading into the background. Clipped to its bounds. |
| `OracleGlass` | Translucent `surfaceContainerHigh` with a hairline `outlineVariant` border: bars, chips, cards and tiles over artwork. |
| `OracleDock` / `OracleDockItem` | Floating pill navigation on phones; the selected item becomes a lantern pill with its label. Space: `OracleDockSpace`. |
| `OracleRail` / `OracleRailItem` | The same language as a vertical rail on tablets, with the logo on top. |
| `OracleLargeTopBar` | Transparent tab title in `displaySmall` serif, with a subtitle and actions. |
| `OracleSectionHeader` | Headline-serif section title with supporting text and an optional action. |
| `OracleHero` | Continue-reading hero: backdrop, poster, title, progress, Resume. Cover-tinted. |
| `OraclePoster` / `OraclePill` | Cover cards with a vignette, status pills and a lantern selection ring. |
| `OraclePillTabs` | Scrolling pill tabs; folders show their accent, icon and count. |

## Texture and light (`OracleTexture.kt`)

These are native Compose versions of React Bits effects. React Bits itself is web-only (React/CSS/WebGL), so it
cannot run in the app.

| Effect | React Bits counterpart | Implementation |
|---|---|---|
| `OracleAuroraBackdrop` | Aurora / Silk | AGSL `RuntimeShader` on Android 13+: two aurora curtains in primary and secondary, bent by fbm "silk" noise, with an ember glow from tertiary at the bottom. Before Android 13: soft static radial gradients. Sits behind every home tab. |
| `Modifier.oracleGrain` | Noise | A seamless 192px monochrome grain tile drawn as a repeating `ImageShader`: over backdrops at about 5.5% and behind glass content at 4%, so text stays clean. |
| `OracleShinyText` | ShinyText / GradientText | A lantern-light sweep across serif titles (`OracleLargeTopBar`), every 5.2 s. |
| `oracleHairline()` | gradient borders | Glass edges lit from the top-left in primary, fading into `outlineVariant`. |

Home tab scaffolds read `LocalScaffoldContainerColor` (provided as transparent by `HomeScreen`), so the backdrop shows
through. Screens pushed on top keep an opaque background. The shader time, the title sweep and every ambient loop
hold still under reduced motion.

## In-progress features

`OracleInProgressPill` (an hourglass and "In progress" on `tertiaryContainer`) marks features that are visible but
not available yet: OracleRead Account, Community, and the profile and community quick actions. Tapping one only
explains that it is coming in a future update.

## Surfaces

- **Navigation.** `HomeScreen` shows the floating dock, or the rail on tablets. Compact mode (3 tabs, Browse
  expands) and full mode (5 tabs) are kept.
- **Library.** A serif title with the reading streak, folder pills, the ambient folder background, the
  continue-reading hero, source shortcuts and a poster grid. Pin, featured and folder styles all work.
- **Manga detail.** Cover backdrop, cover-derived accent, large poster, serif title, glass action tiles (library,
  interval, tracking, WebView, and Community marked "In progress") and the chapter list.
- **Updates, history, browse, more, settings, about, onboarding and reader chrome** use the same headers, glass cards
  and pills. The reader page itself is never restyled.

## Motion and reduced motion

- Expressive springs from `MaterialExpressiveTheme`, and a fade-through between tabs.
- Hero and accent colors ease in over 450 ms.
- The ambient folder particles, the reading-streak flame and other loops check `rememberReducedMotion()` (the system
  animator duration scale). When animations are off they render one still frame.

## Accessibility

- Contrast: primary `#E9C987` on `#0A0D13` is about 12:1, and on-surface-variant `#A7AEBC` is about 8.6:1. The
  functional outline `#5C6577` is ≥3:1.
- Dock and rail items expose their tab role, selected state and badge descriptions.
- Section and screen titles are marked as headings.
- Folder pills always pair color with an icon and a name.
