export WALLET_CORE_VERSION := "4.1.19"

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
    @cd core && cargo run --package generate --bin generate android ../gemcore/src/main/java/com/wallet/core

build-base-image:
	docker build -t gem-android-base -f Dockerfile.base . &> build.base.log

TAG := env("TAG", "main")
BUILD_MODE := env("BUILD_MODE", "")

build-app:
	docker build --build-arg TAG={{TAG}} \
	--build-arg BUILD_MODE={{BUILD_MODE}} \
	--progress=plain \
	-m 16g \
	-t gem-android-app \
	-f Dockerfile.app . &> build.app.log

core-upgrade:
	@git submodule update --recursive --remote

mod core
