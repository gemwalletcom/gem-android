# Reproducible builds

This folder contains the tooling to rebuild tagged releases inside Docker and compare them to published APKs.

## Principle
- Build inside Docker using the repo-root base image `Dockerfile` and `reproducible/Dockerfile`, running the release task sequence (`clean :app:bundleGoogleRelease assembleUniversalRelease`) with Gradle/Maven caches cleared each run.
- Base image publishing is done by the `Publish Base Image` workflow to build/push `ghcr.io/gemwalletcom/gem-android-base:<base-tag>` (where `<base-tag>` lives in `reproducible/base_image_tag.txt`), for `linux/amd64` and `linux/arm64`.
- Verification defaults to `ghcr.io/gemwalletcom/gem-android-base:<base-tag>` and runs on `linux/amd64` (override with `VERIFY_DOCKER_PLATFORM` if you must).
- Require `local.properties` for GitHub packages; use the same base image used for releases.
- Strip signing artifacts, patch map-id when present, then copy the official signing block onto the rebuilt APK with [apksigcopier](https://github.com/obfusk/apksigcopier) to confirm payload identity without exposing keys.
- Keep outputs under `artifacts/reproducible/<tag>/` and only run diffoscope if hashes still differ after signature copy.

## Prerequisites
- Docker
- unzip, curl
- uv for tool installs, plus `apksigcopier` and `diffoscope`: `uv tool install apksigcopier diffoscope`
- Android SDK build-tools `dexdump` for `diff_dexdump.py` (e.g., `${ANDROID_HOME}/build-tools/<ver>/dexdump`)
- Tooling snapshot: Gradle 8.13-bin, AGP 8.13.1, Kotlin compiler/KGP 2.2.21/1.9.24, KSP 2.2.21-2.0.4; R8 is the AGP-bundled version (map-id flag not exposed).

## Step-by-step verification
1) Auth + credentials:
   - Ensure `local.properties` has GitHub package creds (e.g., `gpr.username`, `gpr.token`) and obtain the official APK path (or URL for CI).
   - Use the same token to log in to GHCR so the base image pull succeeds (otherwise it will rebuild locally). Example using `gpr.token` from `local.properties`:
     ```bash
     grep gpr.token ../local.properties | cut -d'=' -f2- | docker login ghcr.io -u "$(grep gpr.username ../local.properties | cut -d'=' -f2-)" --password-stdin
     ```
     Or, with an exported token: `echo <github-token> | docker login ghcr.io -u <github-username> --password-stdin` (token needs `read:packages`).
2) Run: `./verify_apk.py <git-tag-or-branch> <path-to-official-apk> [--stage all|build|diff]`. Outputs: `official.apk`, `rebuilt.apk`, `r8_patched.apk` (when needed), `rebuilt_signed.apk`, `diffoscope.html` under `artifacts/reproducible/<tag>/`.
3) CI: trigger the `Verify APK` workflow dispatch (`.github/workflows/verify-apk.yml`) with `tag`, `official_apk_url`, optional `stage`, and optional `base_image_tag`; artifacts upload mirrors local outputs. If `base_image_tag` is empty, the workflow reads `reproducible/base_image_tag.txt`.
4) `verify_apk.py` will `docker pull` the base image by default (set `VERIFY_PULL_BASE=false` to skip). It then reuses a local image or builds if the pull fails.
5) Optional manual map-id patch: `./fix_pg_map_id.py <apk-in> <apk-out> <pg-map-id>`.
6) Optional dexdump diff: `./diff_dexdump.py <official-apk> <rebuilt-apk> [--out-dir DIR] [--dexdump PATH] [--tag TAG]` to write per-dex dumps/diffs (defaults to `artifacts/reproducible/<tag>/dexdump` when `--tag` is provided).

## Known issues
- AGP 8.13.1 (bundled R8) randomizes map-id; we patch via `fix_pg_map_id.py` and confirm payload identity by copying the official signing block (apksigcopier). Deterministic map-id support is still required for strict reproducibility.
- Kotlin 2.2.x constrains AGP/R8 upgrades; AGP 9.0.0-beta03 + Gradle 9.2.1 (R8 9.0.27) rejects `-pg-map-id-seed` and lacks Studio support, so we remain on AGP 8.13.1/Gradle 8.13-bin.

## Path forward
1) Upgrade to an AGP that supports Kotlin 2.2.x and bundles a map-id-capable R8.
2) Re-enable deterministic map-id (seed or fixed id) in release builds.
3) Re-run `./verify_apk.py <tag> <apk>` and confirm hashes match without signature copying.
