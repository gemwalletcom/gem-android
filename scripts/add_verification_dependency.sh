#!/usr/bin/env bash
set -euo pipefail

DEPENDENCY=${1:-${ADDITIONAL_DEPENDENCY:-}}
if [[ -z "${DEPENDENCY}" ]]; then
  echo "Usage: $0 group:name:version[:classifier]@ext" >&2
  exit 1
fi

INIT_SCRIPT=$(mktemp)
trap 'rm -f "${INIT_SCRIPT}"' EXIT

GRADLE_REPO_SNIPPET=""
if [[ "${DEPENDENCY}" == gradle:gradle:* ]]; then
  GRADLE_REPO_SNIPPET=$(cat <<'REPO'
    repositories {
        ivy {
            name = "GradleDistributions"
            url = uri("https://services.gradle.org/distributions")
            patternLayout {
                artifact("[artifact]-[revision]-[classifier].[ext]")
            }
            metadataSources {
                artifact()
            }
        }
    }
REPO
)
fi

cat > "${INIT_SCRIPT}" <<EOF
gradle.rootProject {
    def additional = configurations.maybeCreate("dependencyVerificationAdditional")
    additional.canBeConsumed = false
    additional.canBeResolved = true
${GRADLE_REPO_SNIPPET}
    dependencies.add(additional.name, "${DEPENDENCY}")
    tasks.register("resolveDependencyVerificationAdditional") {
        group = "verification"
        description = "Resolves ${DEPENDENCY} so it is written to verification metadata."
        doLast {
            additional.files
        }
    }
}
EOF

./gradlew --no-daemon \
  --init-script "${INIT_SCRIPT}" \
  --write-verification-metadata sha256 \
  resolveDependencyVerificationAdditional
