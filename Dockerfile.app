# syntax=docker/dockerfile:1.4
# This Dockerfile is used to build the Android app (no signing).
ARG BASE_IMAGE=gem-android-base
ARG BASE_IMAGE_TAG=latest
FROM ${BASE_IMAGE}:${BASE_IMAGE_TAG}

ARG TAG=main
ARG SKIP_SIGN
ARG BUNDLE_TASK=":app:assembleUniversalRelease"
ARG GRADLE_OPTS="-Xmx8g -Dfile.encoding=UTF-8"
ARG BUILD_NUMBER=1
ARG BUILD_VERSION="1.0"
ARG R8_MAP_ID_SEED=""

RUN REPO_URL="https://github.com/gemwalletcom/gem-android.git" && \
    if git ls-remote --exit-code --heads "$REPO_URL" "$TAG" >/dev/null 2>&1; then \
        echo "Branch $TAG exists, cloning it..." && \
        git clone --depth 1 --recursive --branch "$TAG" "$REPO_URL" $HOME/gem-android; \
    elif git ls-remote --exit-code --tags "$REPO_URL" "$TAG" >/dev/null 2>&1; then \
        echo "Tag $TAG exists, cloning it..." && \
        git clone --depth 1 --recursive --branch "$TAG" "$REPO_URL" $HOME/gem-android; \
    else \
        echo "Branch/tag $TAG not found, using main branch..." && \
        git clone --depth 1 --recursive --branch main "$REPO_URL" $HOME/gem-android; \
    fi

WORKDIR $HOME/gem-android

ENV GRADLE_OPTS=${GRADLE_OPTS} \
    BUILD_NUMBER=${BUILD_NUMBER} \
    BUILD_VERSION=${BUILD_VERSION} \
    R8_MAP_ID_SEED=${R8_MAP_ID_SEED}

COPY --chown=root:root local.properties ./local.properties

RUN --mount=type=cache,target=/root/.gradle \
    --mount=type=cache,target=/root/.m2 \
    --mount=type=cache,target=/root/.cargo \
    export SKIP_SIGN=${SKIP_SIGN} && \
    ./gradlew ${BUNDLE_TASK} --no-daemon --build-cache

CMD ["bash"]
