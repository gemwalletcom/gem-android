# Reproducible builds

This folder contains the tooling to rebuild tagged releases inside Docker and compare them to published APKs.

## How to verify
- Local/manual: `./verify_apk.py <git-tag-or-branch> <path-to-official-apk>` (or from here `just verify <tag> <apk>`). The script builds `gem-android-base` from the repo-root `Dockerfile`, then builds an app image from `reproducible/Dockerfile` and runs `clean :app:bundleGoogleRelease assembleUniversalRelease` inside the container.
- Map-id: R8 still randomizes map-id; we patch as needed with `fix_pg_map_id.py` internally. You can also run it manually: `./fix_pg_map_id.py <rebuilt.apk> <patched.apk> <official-map-id>`.
- Signing: We strip signing artifacts before diffing. After map-id patching, the verifier copies the official signing block onto the rebuilt APK via [apksigcopier](https://github.com/obfusk/apksigcopier) to confirm payload identity; if the signature-copied hash matches official, the run is treated as success without diff output.
- Outputs land in `artifacts/reproducible/<tag>/` (rebuilt/official APKs, `rebuilt_signed.apk`, and `diffoscope.html`).
- CI workflow: trigger the `Verify APK` workflow dispatch (`.github/workflows/verify-apk.yml`) with `tag`, `official_apk_url`, optional `stage`, and `base_image_tag`. Artifacts upload mirrors the local outputs.
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

## Known issues and path forward
- R8 map-id remains randomized in AGP 8.13.1; we post-process with `fix_pg_map_id.py` and confirm payload identity by copying the official signing block (apksigcopier) when signatures differ. True reproducibility still requires deterministic map-id support.
- Staying on Kotlin 2.2.x constrains AGP/R8 upgrades. A future AGP that bundles a map-id-capable R8 should remove the need for patching.
- AGP 9.0.0-beta03 + Gradle 9.2.1 was tested; R8 (9.0.27) rejects `-pg-map-id-seed` and Studio support is lacking, so we rolled back to AGP 8.13.1/Gradle 8.13-bin.
- Remaining differences (v1.3.62) are confined to `classes.dex` codegen in Google datatransport classes; `classes2/3.dex` match and `baseline.prof` only differs via per-dex CRCs.
- Path to full reproducibility:
  1) Upgrade to an AGP that supports Kotlin 2.2.x **and** bundles a map-id-capable R8 (e.g., R8 8.2+ via a future AGP release). AGP 9.0.0 (beta) is expected to include the flag, but current preview (R8 9.0.27) still rejects it.
  2) Re-enable deterministic map-id (seed or fixed id) in release builds.
  3) Re-run `./verify_apk.py <tag> <apk>` and confirm hashes match without signature copying.

## Interim status
- Map-id injection is disabled so builds continue to work with the current toolchain; reproducibility remains blocked on the R8/AGP upgrade.
- AGP/Gradle 9 preview attempt (rolled back):
  - AGP 9.0.0-beta03 + Gradle 9.2.1: R8 9.0.27 rejected `-pg-map-id-seed` (“Unknown option”); Android Studio stable does not support this combo.
  - Required preview flags (`android.nonFinalResIds=true`, built-in Kotlin/new DSL) were disabled again because they need broader build.gradle changes and Studio support.
  - Next viable step is waiting for an AGP that bundles a map-id-capable R8 (or overriding AGP’s R8) while keeping Kotlin 2.2.x compatibility.
