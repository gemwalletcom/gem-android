# Reproducible builds

This folder contains the tooling to rebuild tagged releases inside Docker and compare them to published APKs.

## How to verify
- Use `./verify_apk.sh <git-tag-or-branch> <path-to-official-apk>`, or from here `just verify <tag> <apk>`.
- The script builds `gem-android-base` from the repo-root `Dockerfile`, then builds an app image from `reproducible/Dockerfile` and runs `:app:assembleUniversalRelease` inside the container.
- Signing artifacts are stripped before comparison; hashes must match afterward for the build to be considered reproducible.
- You can also grab the latest CI-built APK from the Docker workflow artifact `gem-android-apk` on GitHub Actions.

## Prerequisites
- Docker
- Java (for `jarsigner`/`zipalign` dependencies), unzip, curl
- `diffoscope` (optional, used when present)

## Tooling snapshot
- Gradle **8.13-bin**, AGP **8.13.1**, Kotlin compiler/KGP **2.2.21/1.9.24**, KSP **2.2.21-2.0.4**; R8 is the version bundled with this AGP (map-id flag not exposed).
- Map-id seeding plumbing exists in the verifier and Dockerfiles but is disabled because the current R8/AGP toolchain rejects the flag.
- Compatibility reference: Android/AGP version matrix at https://developer.android.com/build/releases/gradle-plugin-roadmap.
  - AGP preview/R8 notes: https://developer.android.com/build/releases/agp-preview
  - R8 option discovery: the AGP-bundled R8 does not expose a CLI; to inspect supported flags you must download a standalone R8 release and run `java -cp r8*.jar com.android.tools.r8.R8 --help`.
  - Root cause: the AGP-bundled R8 lags behind standalone R8; standalone releases already accept `-pg-map-id-seed`, but AGP 9.0.0-beta03 still ships an R8 that rejects it. We need an AGP that bundles a newer R8 (or a custom override) before deterministic map-id is usable.

## Current blocker
- R8 adds a random `pg-map-id` (and related hash) into dex and the baseline profile. The R8 bundled with our supported AGP does **not** expose a stable map-id flag, so dex/baseline.prof remain non-reproducible.
- Staying on Kotlin 2.2.x limits which AGP/R8 versions we can use today; the map-id flag is available only in newer R8 bundled with newer AGP.
- AGP 9.0.0-beta03 + Gradle 9.2.1 was tested; the run failed because R8 (9.0.27) does not recognize `-pg-map-id-seed`, and Studio compatibility is lacking. We rolled back to AGP 8.13.1/Gradle 8.13-bin.
- Dex/resources are expected to differ today (see e.g. `artifacts/reproducible/1.3.55/diffoscope.html`) because R8 still injects non-deterministic `pg-map-id` data.

## Path to reproducibility
1) Upgrade to an AGP that supports Kotlin 2.2.x **and** bundles a map-id-capable R8 (e.g., R8 8.2+ via a future AGP release). AGP 9.0.0 (in beta per https://developer.android.com/build/releases/agp-preview) is expected to include the flag, but current preview (with R8 9.0.27) still rejects it.
2) Re-enable deterministic map-id (seed or fixed id) in release builds.
3) Re-run `./verify_apk.sh <tag> <apk>` and confirm hashes match.

## Interim status
- Map-id injection is disabled so builds continue to work with the current toolchain; reproducibility remains blocked on the R8/AGP upgrade.
- AGP/Gradle 9 preview attempt (rolled back):
  - AGP 9.0.0-beta03 + Gradle 9.2.1: R8 9.0.27 rejected `-pg-map-id-seed` (“Unknown option”); Android Studio stable does not support this combo.
  - Required preview flags (`android.nonFinalResIds=true`, built-in Kotlin/new DSL) were disabled again because they need broader build.gradle changes and Studio support.
  - Next viable step is waiting for an AGP that bundles a map-id-capable R8 (or overriding AGP’s R8) while keeping Kotlin 2.2.x compatibility.
