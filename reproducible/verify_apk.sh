#!/usr/bin/env bash
set -euo pipefail

# ============================================================================
# Helper Functions
# ============================================================================

usage() {
  echo "Usage: $0 <git-tag-or-branch> <path-to-official-apk>" >&2
  exit 1
}

need_cmd() {
  command -v "$1" >/dev/null 2>&1 || {
    echo "Missing required command: $1" >&2
    exit 1
  }
}

sanitize() {
  echo "$1" | sed 's/[^a-zA-Z0-9_.-]/-/g'
}

sha256_file() {
  local file="$1"
  if command -v sha256sum >/dev/null 2>&1; then
    sha256sum "$file" | awk '{print $1}'
  else
    shasum -a 256 "$file" | awk '{print $1}'
  fi
}

strip_signing_artifacts() {
  local dir="$1"
  local meta="${dir}/META-INF"
  if [[ -d "$meta" ]]; then
    rm -f "${meta}"/*.RSA "${meta}"/*.DSA "${meta}"/*.EC "${meta}"/*.SF "${meta}"/*.MF "${meta}"/*.DSIG \
      "${meta}"/CERT.* "${meta}"/CHANGES.* "${meta}"/MANIFEST.MF "${meta}"/SIGNATURE.SF 2>/dev/null || true
    rm -f "${meta}/com/android/metadata" 2>/dev/null || true
  fi
  find "$dir" -name "stamp-cert-sha256" -delete >/dev/null 2>&1 || true
  find "$dir" -name "*.idsig" -delete >/dev/null 2>&1 || true
}

resolve_ref() {
  local ref="$1"
  local repo_url="$2"

  if git ls-remote --exit-code --heads "$repo_url" "$ref" >/dev/null 2>&1 \
    || git ls-remote --exit-code --tags "$repo_url" "$ref" >/dev/null 2>&1; then
    echo "$ref"
    return 0
  fi

  echo "Failed to find branch/tag '$ref' in $repo_url" >&2
  return 1
}

# ============================================================================
# Docker Build Functions
# ============================================================================

ensure_base_image() {
  local base_image="$1"
  local base_tag="$2"

  if ! docker image inspect "${base_image}:${base_tag}" >/dev/null 2>&1; then
    echo "Building base image ${base_image}:${base_tag}..."
    docker build -t "${base_image}:${base_tag}" .
  else
    echo "Using existing base image ${base_image}:${base_tag}"
  fi
}

build_app_image() {
  local app_image="$1"
  local tag="$2"
  local base_image="$3"
  local base_tag="$4"
  local gradle_task="$5"
  local map_id_seed="$6"

  echo "Building app image for tag ${tag} using task ${gradle_task}..."
  docker build \
    -t "${app_image}" \
    --build-arg TAG="${tag}" \
    --build-arg SKIP_SIGN=true \
    --build-arg BASE_IMAGE="${base_image}" \
    --build-arg BASE_IMAGE_TAG="${base_tag}" \
    --build-arg BUNDLE_TASK="${gradle_task}" \
    --build-arg R8_MAP_ID_SEED="${map_id_seed}" \
    -f reproducible/Dockerfile .
}

build_outputs_in_container() {
  local app_image="$1"
  local container_name="$2"
  local gradle_task="$3"
  local map_id_seed="$4"
  local gradle_cache="$5"
  local maven_cache="$6"

  docker rm -f "$container_name" >/dev/null 2>&1 || true
  docker run --name "$container_name" \
    -e SKIP_SIGN=true \
    -e BUNDLE_TASK="$gradle_task" \
    -e R8_MAP_ID_SEED="$map_id_seed" \
    -v "${gradle_cache}":/root/.gradle \
    -v "${maven_cache}":/root/.m2 \
    "$app_image" \
    bash -lc 'cd /root/gem-android && ./gradlew ${BUNDLE_TASK} --no-daemon --build-cache'
}

extract_apk_outputs() {
  local container_name="$1"
  local work_dir="$2"
  local apk_subdir="$3"

  local dest="${work_dir}/apk"
  rm -rf "$dest"
  mkdir -p "$dest"
  docker cp "${container_name}:/root/gem-android/${apk_subdir}/." "$dest"
}

# ============================================================================
# Diffoscope Report
# ============================================================================

run_diffoscope_report() {
  local rebuilt_apk="$1"
  local official_copy="$2"
  local work_dir="$3"

  if [[ "${VERIFY_SKIP_DIFFOSCOPE:-false}" == "true" ]]; then
    echo "Skipping diffoscope because VERIFY_SKIP_DIFFOSCOPE=true." >&2
    return
  fi

  if ! command -v diffoscope >/dev/null 2>&1; then
    echo "diffoscope not found; install it or set VERIFY_SKIP_DIFFOSCOPE=true to silence this message." >&2
    return
  fi

  local diff_dir="${work_dir}/diffoscope"
  local rebuilt_dir="${diff_dir}/rebuilt"
  local official_dir="${diff_dir}/official"
  rm -rf "$diff_dir"
  mkdir -p "$rebuilt_dir" "$official_dir"

  unzip -qq -o "$rebuilt_apk" -d "$rebuilt_dir"
  unzip -qq -o "$official_copy" -d "$official_dir"

  strip_signing_artifacts "$rebuilt_dir"
  strip_signing_artifacts "$official_dir"

  local report="${work_dir}/diffoscope.html"
  if diffoscope --html "$report" "$rebuilt_dir" "$official_dir"; then
    echo "diffoscope report (no differences) written to ${report}"
  else
    local status=$?
    if [[ $status -eq 1 ]]; then
      echo "diffoscope detected differences; see ${report}" >&2
    else
      echo "diffoscope exited with status ${status}. Report (if any): ${report}" >&2
    fi
  fi
}

# ============================================================================
# Main Function
# ============================================================================

main() {
  # Validate arguments
  [[ $# -eq 2 ]] || usage

  local tag="$1"
  local official_apk="$2"

  if [[ ! -f "$official_apk" ]]; then
    echo "Official APK not found: $official_apk" >&2
    exit 1
  fi

  # Setup environment
  local root_dir
  root_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
  cd "$root_dir"

  # Check required commands
  need_cmd docker
  need_cmd java
  need_cmd unzip
  need_cmd curl

  # Check local.properties
  local local_properties="${root_dir}/local.properties"
  if [[ ! -f "$local_properties" ]]; then
    echo "local.properties is required to access GitHub packages." >&2
    exit 1
  fi

  # Setup paths
  local abs_official
  abs_official="$(cd "$(dirname "$official_apk")" && pwd)/$(basename "$official_apk")"

  local tag_safe
  tag_safe="$(sanitize "$tag")"
  [[ -n "$tag_safe" ]] || tag_safe="latest"

  local map_id_seed="${VERIFY_R8_MAP_ID_SEED:-${tag#v}}"
  local repo_url="${VERIFY_REPO_URL:-https://github.com/gemwalletcom/gem-android.git}"
  local resolved_tag
  resolved_tag="$(resolve_ref "$tag" "$repo_url")" || exit 1

  local work_dir="${root_dir}/artifacts/reproducible/${tag_safe}"
  rm -rf "$work_dir"
  mkdir -p "$work_dir"

  local official_copy="${work_dir}/official.apk"
  local rebuilt_apk="${work_dir}/rebuilt.apk"
  cp "$abs_official" "$official_copy"

  # Configure build parameters
  local base_image="${VERIFY_BASE_IMAGE:-gem-android-base}"
  local base_tag="${VERIFY_BASE_TAG:-latest}"
  local gradle_task="${VERIFY_GRADLE_TASK:-:app:assembleUniversalRelease}"
  local apk_subdir="${VERIFY_APK_SUBDIR:-app/build/outputs/apk/universal/release}"

  local app_image_tag
  app_image_tag="$(echo "$tag" | tr '[:upper:]' '[:lower:]' | sed 's/[^a-z0-9_.-]/-/g')"
  [[ -n "$app_image_tag" ]] || app_image_tag="latest"
  local app_image="${VERIFY_APP_IMAGE:-gem-android-app-verify}:${app_image_tag}"
  local app_container="gem-android-app-build-${tag_safe}"
  local gradle_cache="${VERIFY_GRADLE_CACHE:-}"
  local maven_cache="${VERIFY_M2_CACHE:-}"
  local cleanup_gradle=""
  local cleanup_maven=""

  if [[ -z "$gradle_cache" ]]; then
    gradle_cache="$(mktemp -d)"
    cleanup_gradle="$gradle_cache"
  else
    mkdir -p "$gradle_cache"
  fi

  if [[ -z "$maven_cache" ]]; then
    maven_cache="$(mktemp -d)"
    cleanup_maven="$maven_cache"
  else
    mkdir -p "$maven_cache"
  fi

  cleanup() {
    docker rm -f "$app_container" >/dev/null 2>&1 || true
    [[ -n "$cleanup_gradle" ]] && rm -rf "$cleanup_gradle"
    [[ -n "$cleanup_maven" ]] && rm -rf "$cleanup_maven"
  }
  trap cleanup EXIT

  # Build Docker images
  ensure_base_image "$base_image" "$base_tag"
  echo "Build parameters: R8_MAP_ID_SEED=${map_id_seed}"
  build_app_image "$app_image" "$resolved_tag" "$base_image" "$base_tag" "$gradle_task" "$map_id_seed"
  build_outputs_in_container "$app_image" "$app_container" "$gradle_task" "$map_id_seed" "$gradle_cache" "$maven_cache"

  # Extract APK from Docker image
  extract_apk_outputs "$app_container" "$work_dir" "$apk_subdir"

  local apk_from_build
  apk_from_build="$(find "${work_dir}/apk" -name "*.apk" -type f -print -quit)"
  if [[ -z "$apk_from_build" ]]; then
    echo "Failed to locate APK inside ${apk_subdir}" >&2
    exit 1
  fi
  cp "$apk_from_build" "$rebuilt_apk"

  # Compare hashes
  local rebuilt_hash
  local official_hash
  rebuilt_hash="$(sha256_file "$rebuilt_apk")"
  official_hash="$(sha256_file "$official_copy")"

  echo "Rebuilt APK SHA-256 : ${rebuilt_hash}"
  echo "Official APK SHA-256: ${official_hash}"

  # Check if APKs match
  if cmp -s "$rebuilt_apk" "$official_copy"; then
    echo "Success: APKs match."
    exit 0
  fi

  # APKs differ - generate diffoscope report
  echo "Mismatch: APKs differ." >&2
  run_diffoscope_report "$rebuilt_apk" "$official_copy" "$work_dir"

  if [[ "${VERIFY_ALLOW_MISMATCH:-false}" == "true" ]]; then
    exit 0
  fi
  exit 2
}

# ============================================================================
# Entry Point
# ============================================================================

main "$@"
