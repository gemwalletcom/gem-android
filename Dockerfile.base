# syntax=docker/dockerfile:1.4
# This Dockerfile is used to setup the environment (JDK, SDK, NDK, just) for Android app building.

FROM debian:bookworm

ENV DEBIAN_FRONTEND=noninteractive
ENV HOME=/root
ENV ANDROID_HOME=/opt/android-sdk
ENV ANDROID_SDK_ROOT=/opt/android-sdk
ENV PATH=${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools:${PATH}

RUN dpkg --add-architecture amd64

RUN --mount=type=cache,target=/var/cache/apt,sharing=locked \
    --mount=type=cache,target=/var/lib/apt,sharing=locked \
    apt-get update && apt-get install -y \
    curl \
    unzip \
    zip \
    git \
    make \
    build-essential \
    pkg-config \
    openjdk-17-jdk-headless \
    libc6:amd64 \
    zlib1g:amd64

RUN curl --proto '=https' --tlsv1.2 -sSf https://just.systems/install.sh | bash -s -- --to /usr/local/bin

RUN --mount=type=cache,target=/tmp/android-dl \
    mkdir -p ${ANDROID_HOME}/cmdline-tools/latest && \
    if [ ! -f /tmp/android-dl/commandlinetools-linux-11076708_latest.zip ]; then \
        curl -o /tmp/android-dl/commandlinetools-linux-11076708_latest.zip \
        https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip; \
    fi && \
    unzip -q /tmp/android-dl/commandlinetools-linux-11076708_latest.zip -d /tmp/cmdline-tools && \
    mv /tmp/cmdline-tools/cmdline-tools/* ${ANDROID_HOME}/cmdline-tools/latest/ && \
    rm -rf /tmp/cmdline-tools

RUN --mount=type=cache,target=/root/.android \
    yes | ${ANDROID_HOME}/cmdline-tools/latest/bin/sdkmanager --licenses && \
    ${ANDROID_HOME}/cmdline-tools/latest/bin/sdkmanager \
        "platform-tools" \
        "platforms;android-35" \
        "build-tools;35.0.0" \
        "ndk;28.1.13356709"

CMD ["bash"]
