# Reproducible build plan

## Goal
Produce byte-identical APKs to the published releases using the verifier (`scripts/verify_apk.sh`), with differences limited to signing artifacts (which are already stripped).

## What we have in place
- Verifier script builds via Docker using the same Gradle task (`:app:assembleUniversalRelease`) and strips signing artifacts before comparing.
- `BUILD_VERSION`/`BUILD_NUMBER` are passed into the Docker build so the manifest matches the tagged release values.
- Docker images: base (`gem-android-base` from `Dockerfile.base`) with SDK/NDK/JDK; app (`gem-android-app-verify:<tag>` from `Dockerfile.app`) built per tag under test.
- Tooling currently in use: Gradle 8.13, AGP 8.13.2, Kotlin compiler 2.2.21 (KGP 1.9.24), KSP 2.2.21-2.0.4; R8 is the version bundled with this AGP (map-id flag not exposed).
- Map-id seeding plumbing exists in the verifier/Dockerfile, but is currently disabled in the build because the present R8/AGP toolchain rejects the flag.
- Compatibility reference: Android/AGP version matrix at https://developer.android.com/build/releases/gradle-plugin-roadmap.

## Current blocker
- R8 adds a random `pg-map-id` (and related hash) into dex and the baseline profile. The R8 bundled with our supported AGP does **not** expose a stable map-id flag, so dex/baseline.prof remain non-reproducible.
- Staying on Kotlin 2.2.x limits which AGP/R8 versions we can use today; the map-id flag is available only in newer R8 bundled with newer AGP.

## Path to reproducibility
1) Upgrade to an AGP that supports Kotlin 2.2.x **and** bundles a map-id-capable R8 (e.g., R8 8.2+ via a future AGP release). AGP 9.0.0 (in beta per https://developer.android.com/build/releases/agp-preview) should ship a map-id-capable R8, but requires Gradle 9.x and compatibility checks for Kotlin/Compose/KSP.
2) Re-enable deterministic map-id (seed or fixed id) in release builds.
3) Re-run `scripts/verify_apk.sh <tag> <apk>` and confirm hashes match.

## Interim status
- Map-id injection is disabled so builds continue to work with the current toolchain; reproducibility remains blocked on the R8/AGP upgrade.
- Once a compatible AGP/R8 is available, flip the map-id flag back on and rerun verification.
- AGP 9.0.0-beta03 exists (requires Gradle 9.x); adopting it now would mean jumping to preview tooling and reconciling Kotlin/Compose/KSP compatibility. We’re holding for a stable AGP/R8 that supports Kotlin 2.2.x and exposes the map-id flag.
