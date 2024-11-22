WALLET_CORE_VERSION ?= 4.1.19

install:
	@echo Install Rust
	@curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh -s -- -y
	@. ~/.cargo/env
	@cargo install just

install-typeshare:
	@echo Install typeshare-cli
	@cd core && just install-typeshare

install-toolchains:
	@echo Install toolchains for uniffi
	@cd core/gemstone && just install-android-targets

bootstrap: install install-toolchains install-ndk install-wallet-core

# Android

install-ndk:
	@cd core/gemstone && just install-ndk

install-wallet-core:
	@./scripts/download-wallet-core.sh ${WALLET_CORE_VERSION}

build-test:
	./gradlew assembleGoogleDebugAndroidTest --build-cache

test:
	./gradlew connectedGoogleDebugAndroidTest

unsigned-release:
	export SKIP_SIGN=true && ./gradlew :app:bundleGoogleRelease

extract-universal-apk:
	./scripts/extract_aab_apk.sh

release:
	./gradlew clean :app:bundleGoogleRelease assembleUniversalRelease assembleHuaweiRelease assembleSolanaRelease

localize:
	@sh scripts/localize.sh android

generate: generate-models generate-stone

generate-models: install-typeshare
	@echo "Generate typeshare for Android"
	@cd core && cargo run --package generate --bin generate android ../gemcore/src/main/java/com/wallet/core

generate-stone:
	@echo "Generate Gemstone lib, default build mode is ${BUILD_MODE}"
	@cd core/gemstone && BUILD_MODE=${BUILD_MODE} just build-android

build-base-image:
	docker build -t gem-android-base -f Dockerfile.base . &> build.base.log

build-app:
	docker build --build-arg TAG=${TAG} \
	--build-arg BUILD_MODE=${BUILD_MODE} \
	--progress=plain \
	-m 16g \
	-t gem-android-app \
	-f Dockerfile.app . &> build.app.log

core-upgrade:
	git submodule update --recursive --remote
