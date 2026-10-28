# OracleRead 1.4.1 performance measurements

Measured on 2026-10-28 on one Android 15 (API 35) x86_64 emulator, 1080x2400 at 420 dpi, Skia (OpenGL) on emulated
graphics. The library held 18 local test titles in two folders. Each run cold-starts the Library tab, waits 3 s, then
records `dumpsys gfxinfo framestats` for:

- **Idle:** 10 s without touching the screen, which measures the cost of ambient motion alone.
- **Scroll:** 8 swipes (4 down, 4 up) through the library grid.

An emulator is far slower than a real phone at drawing, so every frame time here is above a phone's. Read these as a
before/after comparison of the same work, not as the speed you will see on your device.

## Results

| Run | Idle frames in 10 s | Idle frame time p50 / p95 / p99 | Idle GPU p95 | Scroll frame time p50 / p95 / p99 |
|---|---|---|---|---|
| 1.4.0, Lantern folder | 606 | 22 / 25 / 29 ms | 21 ms | 26 / 34 / 53 ms |
| 1.4.1, Lantern folder | 303 | 17 / 18 / 19 ms | 14 ms | 24 / 32 / 46 ms |
| 1.4.1, None folder | 302 | 17 / 18 / 19 ms | 15 ms | 18 / 28 / 34 ms |

What changed for the idle screen:
- **Half the redraws.** 1.4.0 redrew the whole screen on every display frame (about 60 a second) just to move the
  background. 1.4.1 moves ambient effects 30 times a second, which still looks continuous for slow drifts.
- **Cheaper frames.** Each idle frame takes about a quarter less time (p50 22 → 17 ms), and the GPU's slowest frames
  are a third quicker (p95 21 → 14 ms).
- **Steadier.** The spread between typical and slowest idle frames shrank from 7 ms to 2 ms.

Scrolling improved modestly with the Lantern background (p99 53 → 46 ms) and clearly with None (p99 34 ms).

## Why it was slow, and what was fixed

1. **Every ambient effect rebuilt the library on every frame.** Folder backgrounds, the title shimmer and the streak
   flame read their animation value while the screen was being laid out, so each frame recomposed them and rebuilt
   their colours, gradients and shader. They now read time only while drawing, and their shapes and brushes are made
   once per size.
2. **Separate clocks multiplied the redraws.** An early 1.4.1 build gave the aurora its own slower clock while other
   effects kept running every frame. The two landed on different frames, so the screen still redrew about 60 times a
   second and idle frames got slower (p50 29 ms). All ambient effects now share one 30-per-second clock that ticks on
   the same frames (covered by `AmbientClockTest`).
3. **The aurora shader did more work than it showed.** It now uses 3 noise octaves instead of 4 and renders at half
   resolution into its own layer; its soft gradients look the same. The film grain over it stays full resolution.
4. **Hidden screens kept animating.** Ambient motion now stops when the app is in the background.
5. **Cover shading was rebuilt while scrolling.** Poster and grid gradients are now shared constants.

Choosing **None** for a folder removes its background and swaps the aurora for its static version, which is the
lightest setting.

Raw numbers for each run are kept with the release's QA evidence. The empty-histogram value `4950ms` in raw
`gfxinfo` output is Android's marker for "no frames drawn", not a slow frame.
