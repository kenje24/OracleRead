# OracleRead 1.4.1 — build and verification notes

This directory contains the release's reproducible verification tools and evidence.

## Verify APKs

From the repository root, with Java 21 and Android SDK build-tools available:

```sh
python docs/releases/verify-release.py --version 1.4.1
```

The verifier checks all ABI splits and the universal APK, application ID, version name/code, and the OracleRead release certificate. It computes SHA-256 hashes without reading the private signing key. After publication, add `--remote` to compare GitHub asset sizes/digests and main/tag commit IDs:

```sh
python docs/releases/verify-release.py --version 1.4.1 --remote
```

The local keystore and its password properties are intentionally outside the repository. Do not commit them or sign public releases with the Android debug key. OracleRead's dedicated certificate is the same one introduced in 1.4.0, enabling in-place updates from 1.4.0.

## Regression checks

```sh
./gradlew spotlessCheck testDebugUnitTest verifySqlDelightMigration --max-workers=4
```

## Performance evidence

Emulator measurements are useful comparisons, not promises about every Android device. The before/after capture must use the same Android image, viewport, fixture data, animation settings, warm-up, idle duration and scroll sequence. Android's empty frame histogram uses `4950ms` as a sentinel when no frames were drawn; that does **not** mean a 4.95-second frame. Read `Total frames rendered` first.

The release screenshots use local QA chapters and original procedural cover artwork. They do not contain bundled or hosted manga.
