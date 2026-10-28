# OracleRead

OracleRead is a free and open source community manga reader for Android 8.0 and above.

OracleRead is a community-driven fork of Mihon focused on providing a clean, stable, and modern manga reading experience.

Project lineage:

Tachiyomi -> Mihon -> OracleRead

## What makes OracleRead different

OracleRead has a **cinematic, immersive** look of its own:

- the cover art is the star of every screen
- a dark ink-night canvas with lantern-gold accents
- serif title cards
- glass controls
- a floating navigation dock
- a continue-reading hero in the library
- manga pages set on their own blurred cover art

The design system is documented in [DESIGN.md](DESIGN.md) and the product principles in [PRODUCT.md](PRODUCT.md).

OracleRead also adds:

- a Community space and OracleRead accounts (in progress, coming in a future update)
- reader auto-retry
- a reading streak
- pinned manga
- folder styles with ambient backgrounds (or none)
- source shortcuts
- hiding decimal and duplicate chapters
- a Latest button above Resume
- a compact navigation mode

It stays in sync with Mihon; the latest sync is described in [docs/sync-notes.md](docs/sync-notes.md).

## What's new in 1.4.1

Full notes: [docs/releases/2026-10-28-v1.4.1.md](docs/releases/2026-10-28-v1.4.1.md).

- **Hide duplicate chapters**, per manga or globally. Nothing is deleted; the copy you're reading or have read is kept.
- **Latest** opens the newest chapter, right above Resume.
- **Incognito and the reading streak** moved to the right of the greeting.
- **Folder backgrounds**: None, plus Ink Tide, Constellations, Eclipse Halo and Silk Ribbons.
- **Smoother home**: ambient motion shares one 30-per-second clock and stops when hidden.

| Library | Latest + Resume | Duplicates hidden |
|---|---|---|
| ![Library](docs/screenshots/v1.4.1/library.png) | ![Latest and Resume](docs/screenshots/v1.4.1/latest-resume.png) | ![Duplicates hidden](docs/screenshots/v1.4.1/duplicates-hidden.png) |

| None | Ink Tide | Constellations | Eclipse Halo | Silk Ribbons |
|---|---|---|---|---|
| ![None](docs/screenshots/v1.4.1/ambience-none.png) | ![Ink Tide](docs/screenshots/v1.4.1/ambience-ink_tide.png) | ![Constellations](docs/screenshots/v1.4.1/ambience-constellations.png) | ![Eclipse Halo](docs/screenshots/v1.4.1/ambience-eclipse.png) | ![Silk Ribbons](docs/screenshots/v1.4.1/ambience-silk_ribbons.png) |

### Previous release

1.4.0 synced with Mihon and introduced the cinematic redesign. See the 1.4.0 entry in [CHANGELOG.md](CHANGELOG.md).

## Extension Repository

OracleRead includes the Keiyoushi and Yūzōnō extension repositories by default:

<https://github.com/keiyoushi/extensions>

<https://github.com/yuzono/manga-repo>

## Credits

OracleRead is built upon the Mihon open-source project.

Special thanks to:

- Mihon Open Source Project
- Original Tachiyomi contributors
- Extension developers
- Open source contributors

Links:

- Mihon GitHub: <https://github.com/mihonapp/mihon>
- OracleRead Repository: <https://github.com/kenje24/OracleRead.git>

## Legal Disclaimer

OracleRead does not host, store, or distribute any content.

All content is provided by third-party sources and extensions.

OracleRead is an independent open-source project and is not affiliated with content providers.

## License

Copyright 2015 Javier Tomas

Copyright 2024 Mihon Open Source Project

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

<http://www.apache.org/licenses/LICENSE-2.0>

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
