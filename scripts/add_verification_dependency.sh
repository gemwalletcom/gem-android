#!/usr/bin/env bash
set -euo pipefail

DEPENDENCY=${1:-${ADDITIONAL_DEPENDENCY:-}}
if [[ -z "${DEPENDENCY}" ]]; then
  echo "Usage: $0 group:name:version[:classifier]@ext" >&2
  exit 1
fi

./gradlew --no-daemon \
  --write-verification-metadata sha256 \
  --dependency "${DEPENDENCY}"
