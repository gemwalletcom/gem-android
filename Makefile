ANDROID_HOME ?= ~/Library/Android/sdk
SDK_MANAGER = ${ANDROID_HOME}/cmdline-tools/latest/bin/sdkmanager
WALLET_CORE_VERSION ?= 4.1.5
NDK_VERSION ?= 26.1.10909125

install:
	@echo Install Rust
	@curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh -s -- -y
	@. ~/.cargo/env

install-typeshare:
	@echo Install typeshare-cli
	@cargo install typeshare-cli --version 1.6.0

install-toolchains:
	@echo Install toolchains for uniffi
	@cd core/gemstone && make prepare-android

bootstrap: install install-toolchains install-ndk install-wallet-core

# Android

install-ndk:
	${SDK_MANAGER} "ndk;${NDK_VERSION}"

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
	./gradlew clean buildCargoNdkGoogleRelease assembleGoogleRelease :app:bundleGoogleRelease

localize:
	@sh scripts/localize.sh android

generate: generate-models generate-stone

generate-models: install-typeshare
	@echo "Generate typeshare for Android"
	@cd core && cargo run --package generate --bin generate android ../gemcore/src/main/java/com/wallet/core

generate-stone:
	@echo "Generate Gemstone lib, default build mode is ${BUILD_MODE}"
	@cd core/gemstone && make bindgen-kotlin BUILD_MODE=${BUILD_MODE}
	@cp -Rf core/gemstone/generated/kotlin/uniffi gemcore/src/main/java
	@touch local.properties
ifeq (${BUILD_MODE},release)
	./gradlew buildCargoNdkGoogleRelease --info
else
	./gradlew buildCargoNdkGoogleDebug --info
endif

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