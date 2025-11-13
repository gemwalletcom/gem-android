#!/usr/bin/env bash
set -euo pipefail

usage() {
  echo "Usage: $0 <git-tag-or-branch> <path-to-official-apk>" >&2
  exit 1
}

[[ $# -eq 2 ]] || usage

TAG="$1"
OFFICIAL_APK="$2"

if [[ ! -f "$OFFICIAL_APK" ]]; then
  echo "Official APK not found: $OFFICIAL_APK" >&2
  exit 1
fi

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

need_cmd() {
  command -v "$1" >/dev/null 2>&1 || {
    echo "Missing required command: $1" >&2
    exit 1
  }
}

need_cmd docker
need_cmd java
need_cmd unzip
need_cmd curl

LOCAL_PROPERTIES="${ROOT_DIR}/local.properties"
if [[ ! -f "$LOCAL_PROPERTIES" ]]; then
  echo "local.properties is required to access private dependencies." >&2
  exit 1
fi

abs_official="$(cd "$(dirname "$OFFICIAL_APK")" && pwd)/$(basename "$OFFICIAL_APK")"

sanitize() {
  echo "$1" | sed 's/[^a-zA-Z0-9_.-]/-/g'
}

TAG_SAFE="$(sanitize "$TAG")"
[[ -n "$TAG_SAFE" ]] || TAG_SAFE="latest"
WORK_DIR="${ROOT_DIR}/artifacts/reproducible/${TAG_SAFE}"
rm -rf "$WORK_DIR"
mkdir -p "$WORK_DIR"
cp "$abs_official" "${WORK_DIR}/official.apk"

sha256_file() {
  local file="$1"
  if command -v sha256sum >/dev/null 2>&1; then
    sha256sum "$file" | awk '{print $1}'
  else
    shasum -a 256 "$file" | awk '{print $1}'
  fi
}

BASE_IMAGE="${VERIFY_BASE_IMAGE:-gem-android-base}"
BASE_TAG="${VERIFY_BASE_TAG:-latest}"
APP_IMAGE_TAG="$(echo "$TAG" | tr '[:upper:]' '[:lower:]' | sed 's/[^a-z0-9_.-]/-/g')"
[[ -n "$APP_IMAGE_TAG" ]] || APP_IMAGE_TAG="latest"
APP_IMAGE="${VERIFY_APP_IMAGE:-gem-android-app-verify}:${APP_IMAGE_TAG}"

ensure_base_image() {
  if ! docker image inspect "${BASE_IMAGE}:${BASE_TAG}" >/dev/null 2>&1; then
    echo "Building base image ${BASE_IMAGE}:${BASE_TAG}..."
    docker build -t "${BASE_IMAGE}:${BASE_TAG}" -f Dockerfile.base .
  else
    echo "Using existing base image ${BASE_IMAGE}:${BASE_TAG}"
  fi
}

build_app_image() {
  echo "Building app image for tag ${TAG}..."
  docker build \
    -t "${APP_IMAGE}" \
    --build-arg TAG="${TAG}" \
    --build-arg SKIP_SIGN=true \
    --build-arg BASE_IMAGE_TAG="${BASE_TAG}" \
    -f Dockerfile.app .
}

extract_aab() {
  local container_id
  container_id=$(docker create "${APP_IMAGE}")
  cleanup() {
    docker rm -f "$container_id" >/dev/null 2>&1 || true
  }
  trap cleanup EXIT
  docker cp "$container_id":/root/gem-android/app/build/outputs/bundle/googleRelease "${WORK_DIR}/googleRelease"
  cleanup
  trap - EXIT
}

ensure_bundletool() {
  local version="1.18.1"
  local jar="${ROOT_DIR}/scripts/bundletool-all-${version}.jar"
  if [[ ! -f "$jar" ]]; then
    echo "Downloading bundletool-all-${version}.jar..."
    curl -L --fail -o "$jar" "https://github.com/google/bundletool/releases/download/${version}/bundletool-all-${version}.jar"
  fi
  echo "$jar"
}

ensure_base_image
build_app_image
extract_aab

AAB_PATH="$(find "${WORK_DIR}/googleRelease" -name "*.aab" -type f -print -quit)"
if [[ -z "$AAB_PATH" ]]; then
  echo "Failed to locate AAB file under ${WORK_DIR}/googleRelease" >&2
  exit 1
fi

BUNDLETOOL_JAR="$(ensure_bundletool)"
java -jar "$BUNDLETOOL_JAR" build-apks \
  --bundle="$AAB_PATH" \
  --output="${WORK_DIR}/rebuilt.apks" \
  --mode=universal \
  >/dev/null

unzip -q "${WORK_DIR}/rebuilt.apks" -d "${WORK_DIR}/rebuilt_apks"
if [[ ! -f "${WORK_DIR}/rebuilt_apks/universal.apk" ]]; then
  echo "bundletool did not produce a universal APK" >&2
  exit 1
fi
cp "${WORK_DIR}/rebuilt_apks/universal.apk" "${WORK_DIR}/rebuilt.apk"

REBUILT_HASH="$(sha256_file "${WORK_DIR}/rebuilt.apk")"
OFFICIAL_HASH="$(sha256_file "${WORK_DIR}/official.apk")"

echo "Rebuilt APK SHA-256 : ${REBUILT_HASH}"
echo "Official APK SHA-256: ${OFFICIAL_HASH}"

if cmp -s "${WORK_DIR}/rebuilt.apk" "${WORK_DIR}/official.apk"; then
  echo "Success: APKs match."
  exit 0
fi

echo "Mismatch: APKs differ." >&2
if [[ "${VERIFY_ALLOW_MISMATCH:-false}" == "true" ]]; then
  exit 0
fi
exit 2
