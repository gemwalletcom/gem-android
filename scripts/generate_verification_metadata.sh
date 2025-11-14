#!/usr/bin/env bash
set -euo pipefail

# Keep Gradle distribution hashes in verification metadata so dependency verification
# continues to work after upgrading Gradle.
GRADLE_VERSION=$(sed -En 's#.*/gradle-([0-9.]+)-.*#\1#p' gradle/wrapper/gradle-wrapper.properties | head -n1)
if [[ -z "${GRADLE_VERSION}" ]]; then
  echo "Unable to determine Gradle version from gradle/wrapper/gradle-wrapper.properties" >&2
  exit 1
fi

./gradlew --no-daemon \
  --write-verification-metadata sha256 \
  --dependency "gradle:gradle:${GRADLE_VERSION}:bin@zip" \
  --dependency "gradle:gradle:${GRADLE_VERSION}:src@zip"

./gradlew :app:assembleGoogleDebug --write-verification-metadata sha256
