ANDROID_HOME ?= ~/Library/Android/sdk
SDK_MANAGER = ${ANDROID_HOME}/cmdline-tools/latest/bin/sdkmanager

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

core-upgrade:
	git submodule update --recursive --remote

bootstrap: install install-toolchains install-ndk generate

# Android

install-ndk:
	${SDK_MANAGER} "ndk;26.1.10909125"

build-test:
	./gradlew assembleAndroidTest --build-cache

test:
	./gradlew connectedAndroidTest

unsigned-release:
	export SKIP_SIGN=true && ./gradlew assembleRelease :app:bundleRelease

extract-universal-apk:
	./scripts/extract_aab_apk.sh

release:
	./gradlew clean buildCargoNdkRelease assembleRelease :app:bundleRelease

localize:
	@sh scripts/localize.sh android

generate: generate-models generate-stone

generate-models: install-typeshare
	@echo "Generate typeshare for Android"
	@cd core && cargo run --package generate --bin generate android ../gemcore/src/main/java/com/wallet/core

generate-stone:
	@echo "Generate Gemstone lib, default build mode is $(BUILD_MODE)"
	@cd core/gemstone && make bindgen-kotlin BUILD_MODE=$(BUILD_MODE)
	@cp -Rf core/gemstone/generated/kotlin/uniffi gemcore/src/main/java
	@touch local.properties
ifeq ($(BUILD_MODE),release)
	./gradlew buildCargoNdkRelease --info
else
	./gradlew buildCargoNdkDebug --info
endif

build-base-image:
	docker build -t gem-android-base -f Dockerfile.base .

build-app:
	docker build --build-arg TAG=$(TAG) \
	--build-arg BUILD_MODE=$(BUILD_MODE) \
	--build-arg GITHUB_USER=$(GITHUB_USER) \
	--build-arg GITHUB_TOKEN=$(GITHUB_TOKEN) \
	-t gem-android -f Dockerfile.app . 
