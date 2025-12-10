# Reproducible builds

This folder contains the tooling to rebuild tagged releases inside Docker and compare them to published APKs.

## How to verify
- Use `./verify_apk.py <git-tag-or-branch> <path-to-official-apk>`, or from here `just verify <tag> <apk>`.
- The script builds `gem-android-base` from the repo-root `Dockerfile`, then builds an app image from `reproducible/Dockerfile` and runs `clean :app:bundleGoogleRelease assembleUniversalRelease` (matching release) inside the container.
- Until AGP/R8 expose deterministic map-id, always patch the rebuilt APK with `./fix_pg_map_id.py <rebuilt.apk> <patched.apk> <official-map-id>` before comparing (diff_dexdump.py is a pure viewer and does not patch).
- Signing artifacts are stripped before comparison; hashes must match afterward for the build to be considered reproducible.
- You can also grab the latest CI-built APK from the Docker workflow artifact `gem-android-apk` on GitHub Actions.
- Outputs land in `artifacts/reproducible/<tag>/` (rebuilt/official APKs, hashes, and `diffoscope.html`).
- Optional helper: `./fix_pg_map_id.py <apk-in> <apk-out> <pg-map-id>` rewrites long R8 map-ids in `classes*.dex` and `baseline.prof` and recomputes checksums so you can compare what still differs after aligning the map-id.
- Quick dexdump diff: `./diff_dexdump.py <official-apk> <rebuilt-apk> [--out-dir DIR] [--dexdump PATH]` runs `dexdump -d` on both `classes.dex` and writes `dexdump.diff` plus the individual dumps to the output directory (default `artifacts/reproducible/dexdump`). Requires `dexdump` in PATH or via `--dexdump`. This script does not patch map-id; use `fix_pg_map_id.py` separately if needed.

## Prerequisites
- Docker
- Java (for `jarsigner`/`zipalign` dependencies), unzip, curl
- `diffoscope` (optional, used when present)
  - pass extra flags via `DIFFOSCOPE_ARGS`, e.g. `DIFFOSCOPE_ARGS="--exclude-directory-metadata=yes"` to cut noise

## Tooling snapshot
- Gradle **8.13-bin**, AGP **8.13.1**, Kotlin compiler/KGP **2.2.21/1.9.24**, KSP **2.2.21-2.0.4**; R8 is the version bundled with this AGP (map-id flag not exposed).
- Map-id seeding plumbing exists in the verifier and Dockerfiles but is disabled because the current R8/AGP toolchain rejects the flag.
- Compatibility reference: Android/AGP version matrix at https://developer.android.com/build/releases/gradle-plugin-roadmap.
  - AGP preview/R8 notes: https://developer.android.com/build/releases/agp-preview
  - R8 option discovery: the AGP-bundled R8 does not expose a CLI; to inspect supported flags you must download a standalone R8 release and run `java -cp r8*.jar com.android.tools.r8.R8 --help`.
  - Root cause: the AGP-bundled R8 lags behind standalone R8; standalone releases already accept `-pg-map-id-seed`, but AGP 9.0.0-beta03 still ships an R8 that rejects it. We need an AGP that bundles a newer R8 (or a custom override) before deterministic map-id is usable.

## Current issues
- R8 map-id is still randomized in AGP 8.13.1, but we can patch it post-build with `fix_pg_map_id.py`; reproducibility is no longer blocked on R8 if we post-process.
- Staying on Kotlin 2.2.x limits which AGP/R8 versions we can use today; a future AGP with a map-id flag would remove the need for patching.
- AGP 9.0.0-beta03 + Gradle 9.2.1 was tested; the run failed because R8 (9.0.27) does not recognize `-pg-map-id-seed`, and Studio compatibility is lacking. We rolled back to AGP 8.13.1/Gradle 8.13-bin.
- Remaining differences (v1.3.62) are confined to `classes.dex` codegen in Google datatransport classes; `classes2/3.dex` match and `baseline.prof` only differs via per-dex CRCs.

## Path to reproducibility
1) Upgrade to an AGP that supports Kotlin 2.2.x **and** bundles a map-id-capable R8 (e.g., R8 8.2+ via a future AGP release). AGP 9.0.0 (in beta per https://developer.android.com/build/releases/agp-preview) is expected to include the flag, but current preview (with R8 9.0.27) still rejects it.
2) Re-enable deterministic map-id (seed or fixed id) in release builds.
3) Re-run `./verify_apk.py <tag> <apk>` and confirm hashes match.

## Interim status
- Map-id injection is disabled so builds continue to work with the current toolchain; reproducibility remains blocked on the R8/AGP upgrade.
- AGP/Gradle 9 preview attempt (rolled back):
  - AGP 9.0.0-beta03 + Gradle 9.2.1: R8 9.0.27 rejected `-pg-map-id-seed` (“Unknown option”); Android Studio stable does not support this combo.
  - Required preview flags (`android.nonFinalResIds=true`, built-in Kotlin/new DSL) were disabled again because they need broader build.gradle changes and Studio support.
  - Next viable step is waiting for an AGP that bundles a map-id-capable R8 (or overriding AGP’s R8) while keeping Kotlin 2.2.x compatibility.
- Current diff scope (v1.3.62):
   - After aligning map-id and using the release task sequence (`clean :app:bundleGoogleRelease assembleUniversalRelease`), `classes2.dex` and `classes3.dex` match the official build; `baseline.prof` differs only via per-dex CRCs.
   - Remaining delta is confined to `classes.dex`, specifically codegen/layout in Google datatransport classes (`CctBackendFactory.create`, `MetadataBackendRegistry.get`)—see `artifacts/reproducible/dexdump/dexdump.diff` from `diff_dexdump.py`. The `baseline.prof` diff is a consequence of the `classes.dex` CRC differences.
   - The build is therefore “mostly” reproducible; the last gap appears to be R8/desugaring nondeterminism or input-order differences in those transport classes.
