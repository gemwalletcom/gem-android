#!/usr/bin/env bash
set -euo pipefail

if [[ $# -eq 0 ]]; then
  if [[ -z "${ADDITIONAL_DEPENDENCIES:-}" ]]; then
    echo "Usage: $0 dependency [dependency ...]" >&2
    echo "Each dependency should be in Gradle notation, e.g. group:name:version[:classifier]@ext" >&2
    exit 1
  fi
fi

deps_file=$(mktemp)
init_script=$(mktemp)
cleanup() {
  rm -f "${deps_file}" "${init_script}"
}
trap cleanup EXIT

if [[ $# -gt 0 ]]; then
  for dep in "$@"; do
    [[ -n "${dep}" ]] || continue
    printf '%s\n' "${dep}" >> "${deps_file}"
  done
else
  printf '%s\n' "${ADDITIONAL_DEPENDENCIES}" > "${deps_file}"
fi

need_gradle_repo=false
if grep -q '^gradle:gradle:' "${deps_file}"; then
  need_gradle_repo=true
fi

cat > "${init_script}" <<'EOF'
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.attributes.DocsType
import org.gradle.api.attributes.Usage
import org.gradle.api.attributes.java.TargetJvmEnvironment

gradle.rootProject {
    def depsFilePath = project.findProperty("additionalDependenciesFile")
    if (!depsFilePath) {
        throw new GradleException("The -PadditionalDependenciesFile property is required.")
    }
    def depsFile = new File(depsFilePath)
    if (!depsFile.exists()) {
        throw new GradleException("Dependency list file does not exist: " + depsFile)
    }
    def dependencyNotations = depsFile.readLines().findAll { it?.trim() }

    repositories {
        google()
        mavenCentral()
        maven {
            name = "GradlePluginPortal"
            url = uri("https://plugins.gradle.org/m2/")
        }
    }
EOF

if [[ "${need_gradle_repo}" == "true" ]]; then
  cat >> "${init_script}" <<'EOF'
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
EOF
fi

cat >> "${init_script}" <<'EOF'
    def configs = dependencyNotations.collect { notation ->
        def matcher = (notation =~ /([^:]+):([^:]+):([^:@]+)(?::([^:@]+))?(?:@(.+))?/)
        if (!matcher.matches()) {
            throw new GradleException("Unsupported dependency notation: $notation")
        }
        def classifier = matcher.group(4)
        def isDocClassifier = classifier != null && (classifier.contains("sources") || classifier.contains("javadoc"))

        def conf = configurations.detachedConfiguration(dependencies.create(notation))
        conf.canBeConsumed = false
        def usage = objects.named(Usage, Usage.JAVA_RUNTIME)
        conf.attributes.attribute(Usage.USAGE_ATTRIBUTE, usage)
        if (!isDocClassifier) {
            conf.attributes.attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category, Category.LIBRARY))
            conf.attributes.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements, LibraryElements.JAR))
            conf.attributes.attribute(TargetJvmEnvironment.TARGET_JVM_ENVIRONMENT_ATTRIBUTE, objects.named(TargetJvmEnvironment, TargetJvmEnvironment.STANDARD_JVM))
        }
        conf
    }

    tasks.register("resolveDependencyVerificationAdditional") {
        group = "verification"
        description = "Resolves additional dependencies so they are added to verification metadata."
        doLast {
            configs.each { it.files }
        }
    }
}
EOF

./gradlew --no-daemon \
  -PadditionalDependenciesFile="${deps_file}" \
  --init-script "${init_script}" \
  --write-verification-metadata sha256 \
  resolveDependencyVerificationAdditional
