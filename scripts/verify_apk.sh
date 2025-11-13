#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat <<'EOF'
Build the Gem Wallet Android bundle inside the Docker reproducible environment,
extract the universal APK, download the APK published on gemwallet.com, and
compare their SHA-256 hashes.

Usage: scripts/verify_apk.sh [options]

Options:
  --bundle-task <task>   Gradle task to run (default: bundleGoogleRelease).
                         Pass the task name exactly as you would to Gradle
                         (e.g. bundleUniversalRelease).
  --variant <dir>        Variant directory under app/build/outputs/bundle that
                         pairs with the bundle task (default: googleRelease).
  --apk-url <url>        Official APK URL to compare against
                         (default: https://apk.gemwallet.com/gem_wallet_latest.apk).
  --base-image <name>    Docker base image tag (default: gem-android-base:latest).
  --output-dir <path>    Where verification artifacts are stored
                         (default: artifacts/reproducible/googleRelease).
  --rebuild-base         Force rebuilding the base Docker image.
  -h, --help             Show this help.
EOF
}

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

BUNDLE_TASK="bundleGoogleRelease"
VARIANT_DIR="googleRelease"
APK_URL="https://apk.gemwallet.com/gem_wallet_latest.apk"
BASE_IMAGE="gem-android-base:latest"
OUTPUT_ROOT="${ROOT_DIR}/artifacts/reproducible"
REBUILD_BASE="false"

while [[ $# -gt 0 ]]; do
  case "$1" in
    --bundle-task)
      [[ $# -lt 2 ]] && { echo "Missing value for $1" >&2; exit 1; }
      BUNDLE_TASK="$2"
      shift 2
      ;;
    --variant)
      [[ $# -lt 2 ]] && { echo "Missing value for $1" >&2; exit 1; }
      VARIANT_DIR="$2"
      shift 2
      ;;
    --apk-url)
      [[ $# -lt 2 ]] && { echo "Missing value for $1" >&2; exit 1; }
      APK_URL="$2"
      shift 2
      ;;
    --base-image)
      [[ $# -lt 2 ]] && { echo "Missing value for $1" >&2; exit 1; }
      BASE_IMAGE="$2"
      shift 2
      ;;
    --output-dir)
      [[ $# -lt 2 ]] && { echo "Missing value for $1" >&2; exit 1; }
      OUTPUT_ROOT="$2"
      shift 2
      ;;
    --rebuild-base)
      REBUILD_BASE="true"
      shift
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "Unknown option: $1" >&2
      usage
      exit 1
      ;;
  esac
done

if [[ "$OUTPUT_ROOT" != /* ]]; then
  OUTPUT_ROOT="${ROOT_DIR}/${OUTPUT_ROOT}"
fi
OUTPUT_DIR="${OUTPUT_ROOT%/}/${VARIANT_DIR}"
rm -rf "$OUTPUT_DIR"
mkdir -p "$OUTPUT_DIR"

need_cmd() {
  command -v "$1" >/dev/null 2>&1 || {
    echo "Missing required command: $1" >&2
    exit 1
  }
}

need_cmd docker
need_cmd curl

sha256_file() {
  local file="$1"
  if command -v sha256sum >/dev/null 2>&1; then
    sha256sum "$file" | awk '{print $1}'
  elif command -v shasum >/dev/null 2>&1; then
    shasum -a 256 "$file" | awk '{print $1}'
  else
    echo "Neither sha256sum nor shasum is available" >&2
    exit 1
  fi
}

ensure_base_image() {
  if [[ "$REBUILD_BASE" == "true" ]]; then
    docker rmi "$BASE_IMAGE" >/dev/null 2>&1 || true
  fi

  if ! docker image inspect "$BASE_IMAGE" >/dev/null 2>&1; then
    echo "Building base image $BASE_IMAGE..."
    docker build -t "$BASE_IMAGE" -f Dockerfile.base .
  else
    echo "Using existing base image $BASE_IMAGE"
  fi
}

run_in_base() {
  local cmd="$1"
  docker run --rm \
    -v "${ROOT_DIR}":/workspace \
    -w /workspace \
    --user "$(id -u)":"$(id -g)" \
    -e HOME=/tmp \
    -e GRADLE_USER_HOME=/workspace/.gradle \
    -e SKIP_SIGN=true \
    -e ORG_GRADLE_PROJECT_gprUser="${ORG_GRADLE_PROJECT_gprUser:-}" \
    -e ORG_GRADLE_PROJECT_gprKey="${ORG_GRADLE_PROJECT_gprKey:-}" \
    -e BUILD_NUMBER="${BUILD_NUMBER:-}" \
    -e BUILD_VERSION="${BUILD_VERSION:-}" \
    "$BASE_IMAGE" \
    bash -lc "$cmd"
}

verify_local_properties() {
  if [[ ! -f "${ROOT_DIR}/local.properties" ]]; then
    echo "local.properties not found. Please create it with your SDK/GitHub packages credentials." >&2
    exit 1
  fi
}

ensure_base_image
verify_local_properties

if [[ "$BUNDLE_TASK" != :* ]]; then
  GRADLE_TASK=":app:${BUNDLE_TASK}"
else
  GRADLE_TASK="$BUNDLE_TASK"
fi

echo "Running Gradle task ${GRADLE_TASK}"
run_in_base "./gradlew clean ${GRADLE_TASK}"

AAB_DIR="${ROOT_DIR}/app/build/outputs/bundle/${VARIANT_DIR}"
if [[ ! -d "$AAB_DIR" ]] || ! find "$AAB_DIR" -maxdepth 1 -name "*.aab" -quit >/dev/null; then
  echo "No AAB files found in ${AAB_DIR}. Did the bundle task succeed?" >&2
  exit 1
fi

echo "Extracting universal APKs using bundletool"
run_in_base "scripts/extract_aab_apk.sh"

APK_SOURCE="${ROOT_DIR}/scripts/app-${VARIANT_DIR}/universal.apk"
if [[ ! -f "$APK_SOURCE" ]]; then
  echo "Expected APK ${APK_SOURCE} was not produced" >&2
  exit 1
fi

REBUILT_APK="${OUTPUT_DIR}/rebuilt.apk"
cp "$APK_SOURCE" "$REBUILT_APK"

OFFICIAL_APK="${OUTPUT_DIR}/official.apk"
echo "Downloading official APK from ${APK_URL}"
curl -L --fail --output "$OFFICIAL_APK" "$APK_URL"

REB_HASH=$(sha256_file "$REBUILT_APK")
OFF_HASH=$(sha256_file "$OFFICIAL_APK")

printf "Rebuilt APK SHA-256 : %s (%s)\n" "$REB_HASH" "$REBUILT_APK"
printf "Official APK SHA-256 : %s (%s)\n" "$OFF_HASH" "$OFFICIAL_APK"

if cmp -s "$REBUILT_APK" "$OFFICIAL_APK"; then
  echo "Success: APK binaries match byte-for-byte."
  exit 0
fi

echo "Mismatch: APKs differ." >&2
exit 2
