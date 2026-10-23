# Product

## Register

product

## Users

OracleRead is for Android manga readers who manage libraries, install third-party extensions, browse sources, and
read long-form chapters on phones and tablets. They mostly come back to the same loop: open the library, find a
title, resume reading, organize folders, or discover sources.

## Product Purpose

OracleRead is a community-driven Mihon fork focused on a clean, stable and modern manga reading experience, with an
identity of its own. Success means:
- keeping Mihon compatibility and reliability (tracked closely: see `docs/sync-notes.md`)
- making the app feel unmistakably like OracleRead
- proper attribution
- polished reader and library workflows

## Brand Personality

**Cinematic, immersive, calm.** OracleRead treats manga like a streaming app treats film:
- the cover art is the hero
- surfaces are dark and quiet so artwork carries the color
- titles are set like title cards
- motion is smooth and unhurried

It is atmospheric without being loud, and it never slows reading down. The full visual language is in
`DESIGN.md`.

## Anti-references

- Do not hide the project lineage (Tachiyomi → Mihon → OracleRead).
- Avoid marketing-style screens inside core workflows.
- Avoid cluttered controls.
- Avoid motion that competes with reading.
- Avoid changes that make inherited Mihon/Tachiyomi behavior harder to recognize. Workflows stay familiar even
  though the look is new.
- Avoid flat, generic Material defaults that make OracleRead indistinguishable from other forks.

## Design Principles

- **Art first.** Covers stage the screen: backdrops, posters and accents come from the artwork. Chrome recedes into
  glass.
- **Reading comes first.** The reader page is never restyled; only the controls around it are.
- **Familiar workflows, new feel.** Every Mihon feature keeps working where users expect it; only the presentation
  changes.
- **Lineage stays visible.** Tachiyomi, Mihon and OracleRead stay credited in the app (About) and in the docs.
- **Fast access beats deep settings** for common states like incognito, continue reading and source shortcuts.
- **Motion is ambient and optional.** Every looping or ambient animation stops when the system turns animations
  off.

## Accessibility & Inclusion

- Text and functional UI meet WCAG AA contrast on the dark canvas. Light "daylight paper" mode is available.
- Motion respects the system's reduced-motion setting.
- Icon-only actions have content descriptions.
- Folder identity never relies on color alone: every folder pill also shows its icon and name.
