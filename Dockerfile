# syntax=docker/dockerfile:1.4
# Pinned Android build environment on top of Gradle.

ARG GRADLE_IMAGE=gradle:8.13-jdk17
ARG CMDLINE_TOOLS_VERSION=11076708
ARG ANDROID_API_LEVEL=35
ARG ANDROID_BUILD_TOOLS_VERSION=35.0.0
ARG ANDROID_NDK_VERSION=28.1.13356709
ARG JUST_VERSION=1.45.0

FROM ${GRADLE_IMAGE}

USER root

ARG TARGETARCH
ARG CMDLINE_TOOLS_VERSION
ARG ANDROID_API_LEVEL
ARG ANDROID_BUILD_TOOLS_VERSION
ARG ANDROID_NDK_VERSION
ARG JUST_VERSION

ENV HOME=/root
ENV ANDROID_HOME=/opt/android-sdk
ENV ANDROID_SDK_ROOT=/opt/android-sdk
ENV ANDROID_SDK_URL=https://dl.google.com/android/repository/commandlinetools-linux-${CMDLINE_TOOLS_VERSION}_latest.zip
ENV PATH=${ANDROID_HOME}/cmdline-tools/bin:${ANDROID_HOME}/platform-tools:${PATH}

# Runtime deps for build-tools/aapt2; on arm64 install amd64 libs so x86_64-only aapt2 runs under emulation.
COPY scripts/setup-multiarch-apt.sh /usr/local/bin/setup-multiarch-apt
RUN chmod +x /usr/local/bin/setup-multiarch-apt && /usr/local/bin/setup-multiarch-apt

# Install just from a pinned release.
RUN case ${TARGETARCH} in \
        amd64) JUST_TARGET="x86_64-unknown-linux-musl" ;; \
        arm64) JUST_TARGET="aarch64-unknown-linux-musl" ;; \
        *) echo "Unsupported architecture: ${TARGETARCH}" && exit 1 ;; \
    esac && \
    curl -fL \
        "https://github.com/casey/just/releases/download/${JUST_VERSION}/just-${JUST_VERSION}-${JUST_TARGET}.tar.gz" \
        -o /tmp/just.tar.gz && \
    tar -xzf /tmp/just.tar.gz -C /tmp && \
    mv /tmp/just /usr/local/bin/just && \
    rm -rf /tmp/just*

RUN mkdir -p "${ANDROID_HOME}" /root/.android && \
    cd "${ANDROID_HOME}" && \
    curl -o sdk.zip "${ANDROID_SDK_URL}" && \
    unzip -q sdk.zip && \
    rm sdk.zip

RUN --mount=type=cache,target=/root/.android \
    yes | ${ANDROID_HOME}/cmdline-tools/bin/sdkmanager --sdk_root=${ANDROID_HOME} --licenses && \
    ${ANDROID_HOME}/cmdline-tools/bin/sdkmanager --sdk_root=${ANDROID_HOME} \
        "platform-tools" \
        "platforms;android-${ANDROID_API_LEVEL}" \
        "build-tools;${ANDROID_BUILD_TOOLS_VERSION}" \
        "ndk;${ANDROID_NDK_VERSION}"

CMD ["bash"]
