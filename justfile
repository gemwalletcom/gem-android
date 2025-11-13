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

unsigned-release:
    SKIP_SIGN=true ./gradlew :app:bundleGoogleRelease

extract-universal-apk:
    @./scripts/extract_aab_apk.sh

release:
    @./gradlew clean :app:bundleGoogleRelease assembleUniversalRelease assembleHuaweiRelease assembleSolanaRelease assembleSamsungRelease

localize:
    @sh core/scripts/localize.sh android ui/src/main/res

generate: generate-models

generate-models: install-typeshare
    @echo "==> Generate typeshare for Android"
    @cd core && cargo run --package generate --bin generate android ../gemcore/src/main/kotlin/com/wallet/core

build-base-image:
	DOCKER_BUILDKIT=1 docker build -t gem-android-base -f Dockerfile.base .

TAG := env("TAG", "main")
BUILD_MODE := env("BUILD_MODE", "")

build-app:
	DOCKER_BUILDKIT=1 docker build --build-arg TAG={{TAG}} \
	--build-arg SKIP_SIGN=true \
	--progress=plain \
	-m 32g \
	-t gem-android-app \
	-f Dockerfile.app .

core-upgrade:
	@git submodule update --recursive --remote


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
		bash -lc "./gradlew :app:assembleGoogleDebug --write-verification-metadata sha256"

mod core
