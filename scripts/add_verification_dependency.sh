#!/usr/bin/env bash
set -euo pipefail

DEPENDENCY=${1:-${ADDITIONAL_DEPENDENCY:-}}
if [[ -z "${DEPENDENCY}" ]]; then
  echo "Usage: $0 group:name:version[:classifier]@ext" >&2
  exit 1
fi

./scripts/add_verification_dependencies.sh "${DEPENDENCY}"
