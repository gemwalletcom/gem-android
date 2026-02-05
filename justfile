list:
    just --list

bootstrap: install-rust install-toolchains install-ndk

install-rust:
    @echo Install Rust
    just core install-rust

install-toolchains:
    @echo Install toolchains for uniffi
    just core gemstone install-android-targets

install-typeshare:
    @echo "==> Install typeshare-cli"
    just core install-typeshare

install-ndk:
    just core gemstone install-ndk

build-test:
    @./gradlew assembleGoogleDebugAndroidTest --build-cache

test:
    @./gradlew connectedGoogleDebugAndroidTest

mobsfscan:
    @command -v uv >/dev/null || { \
        echo "uv is not installed. Install it via 'curl -LsSf https://astral.sh/uv/install.sh | sh'."; \
        exit 1; }
    uv tool run mobsfscan -- --type android --config .mobsf --exit-warning

release:
    @./gradlew clean :app:bundleGoogleRelease assembleUniversalRelease assembleHuaweiRelease assembleSolanaRelease assembleSamsungRelease

localize:
    @sh core/scripts/localize.sh android ui/src/main/res

generate: generate-models

generate-models: install-typeshare
    @echo "==> Generate typeshare for Android"
    @cd core && cargo run --package generate --bin generate android ../gemcore/src/main/kotlin/com/wallet/core

build-base-image:
	DOCKER_BUILDKIT=1 DOCKER_DEFAULT_PLATFORM={{DOCKER_PLATFORM}} docker build --platform {{DOCKER_PLATFORM}} --no-cache -t gem-android-base {{justfile_directory()}}

TAG := env("TAG", "main")
BUNDLE_TASK := env("BUNDLE_TASK", "clean :app:bundleGoogleRelease assembleUniversalRelease")
DOCKER_PLATFORM := env("DOCKER_PLATFORM", "linux/amd64")
OUTPUTS_DIR := env("OUTPUTS_DIR", "")
GRADLE_WORKERS_MAX := env("GRADLE_WORKERS_MAX", "4")

build-app-image:
	#!/usr/bin/env bash
	set -euo pipefail
	BUNDLE_TASK="{{BUNDLE_TASK}}"
	base_tag=$(cat reproducible/base_image_tag.txt)
	tag="{{TAG}}"
	if ! docker pull ghcr.io/gemwalletcom/gem-android-base:${base_tag} >/dev/null 2>&1; then
		echo "Base image ghcr.io/gemwalletcom/gem-android-base:${base_tag} not found; building locally..." >&2
		DOCKER_BUILDKIT=1 DOCKER_DEFAULT_PLATFORM="{{DOCKER_PLATFORM}}" docker build --platform "{{DOCKER_PLATFORM}}" -t ghcr.io/gemwalletcom/gem-android-base:${base_tag} .
	fi
	DOCKER_BUILDKIT=1 DOCKER_DEFAULT_PLATFORM="{{DOCKER_PLATFORM}}" docker build --platform "{{DOCKER_PLATFORM}}" \
		--build-arg TAG="${tag}" \
		--build-arg SKIP_SIGN=true \
		--build-arg BUNDLE_TASK="${BUNDLE_TASK}" \
		--build-arg BASE_IMAGE=ghcr.io/gemwalletcom/gem-android-base \
		--build-arg BASE_IMAGE_TAG="${base_tag}" \
		-t gem-android-app-verify \
		-f ./reproducible/Dockerfile \
		.

build-app-in-docker:
	#!/usr/bin/env bash
	set -euo pipefail
	BUNDLE_TASK="{{BUNDLE_TASK}}"
	TAG="{{TAG}}" just build-app-image
	container_name="gem-android-app-build"
	gradle_cache=$(mktemp -d)
	maven_cache=$(mktemp -d)
	outputs_dir="{{OUTPUTS_DIR}}"
	trap 'rm -rf "${gradle_cache}" "${maven_cache}"; docker rm -f ${container_name} >/dev/null 2>&1 || true' EXIT
	docker rm -f ${container_name} >/dev/null 2>&1 || true
	DOCKER_DEFAULT_PLATFORM="{{DOCKER_PLATFORM}}" docker run --platform "{{DOCKER_PLATFORM}}" --name ${container_name} \
		-e SKIP_SIGN=true \
		-e BUNDLE_TASK="${BUNDLE_TASK}" \
		-v "${gradle_cache}":/root/.gradle \
		-v "${maven_cache}":/root/.m2 \
		gem-android-app-verify \
		bash -lc 'cd /root/gem-android && ./gradlew ${BUNDLE_TASK} --no-daemon --build-cache -Dorg.gradle.workers.max={{GRADLE_WORKERS_MAX}}'
	if [ -n "${outputs_dir}" ]; then
		mkdir -p "${outputs_dir}"
		docker cp "${container_name}":/root/gem-android/app/build/outputs/. "${outputs_dir}/"
	fi

core-upgrade:
	@git submodule update --recursive --remote

bump:
	@sh ./scripts/bump.sh

generate-verification-metadata:
	#!/usr/bin/env bash
	set -euo pipefail
	GPR_USERNAME=${GPR_USERNAME:-$(grep "gpr.username=" local.properties 2>/dev/null | cut -d'=' -f2 || echo "")}
	GPR_TOKEN=${GPR_TOKEN:-$(grep "gpr.token=" local.properties 2>/dev/null | cut -d'=' -f2 || echo "")}
	docker run --rm \
		-v {{justfile_directory()}}:/workspace \
		-w /workspace \
		-e GPR_USERNAME=${GPR_USERNAME} \
		-e GPR_TOKEN=${GPR_TOKEN} \
		gem-android-base \
		bash -lc './scripts/generate_verification_metadata.sh'

add-verification-dependency dependency:
	#!/usr/bin/env bash
	set -euo pipefail
	GPR_USERNAME=${GPR_USERNAME:-$(grep "gpr.username=" local.properties 2>/dev/null | cut -d'=' -f2 || echo "")}
	GPR_TOKEN=${GPR_TOKEN:-$(grep "gpr.token=" local.properties 2>/dev/null | cut -d'=' -f2 || echo "")}
	docker run --rm \
		-v {{justfile_directory()}}:/workspace \
		-w /workspace \
		-e GPR_USERNAME=${GPR_USERNAME} \
		-e GPR_TOKEN=${GPR_TOKEN} \
		-e ADDITIONAL_DEPENDENCY={{dependency}} \
		gem-android-base \
		bash -lc './scripts/add_verification_dependency.sh "$ADDITIONAL_DEPENDENCY"'

verify TAG APK:
	python3 ./reproducible/verify_apk.py {{TAG}} {{APK}}

mod core
