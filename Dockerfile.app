# syntax=docker/dockerfile:1.4
# This Dockerfile is used to build the Android app (no signing).
ARG BASE_IMAGE=gem-android-base
ARG BASE_IMAGE_TAG=latest
FROM ${BASE_IMAGE}:${BASE_IMAGE_TAG}

ARG TAG=main
ARG SKIP_SIGN
ARG BUNDLE_TASK=":app:assembleUniversalRelease"
ARG GRADLE_OPTS="-Xmx8g -Dfile.encoding=UTF-8"
ARG R8_MAP_ID_SEED=""

RUN git clone --depth 1 --recursive --branch "$TAG" https://github.com/gemwalletcom/gem-android.git $HOME/gem-android

WORKDIR $HOME/gem-android

ENV GRADLE_OPTS=${GRADLE_OPTS} \
    R8_MAP_ID_SEED=${R8_MAP_ID_SEED}

COPY --chown=root:root local.properties ./local.properties

RUN --mount=type=cache,target=/root/.gradle \
    --mount=type=cache,target=/root/.m2 \
    export SKIP_SIGN=${SKIP_SIGN} && \
    ./gradlew ${BUNDLE_TASK} --no-daemon --build-cache

CMD ["bash"]
