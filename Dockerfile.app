# This Dockerfile is used to build the Android app (no signing).
ARG BASE_IMAGE_TAG=latest
FROM gem-android-base:${BASE_IMAGE_TAG}

# Arguments for current tag and skip sign
ARG TAG=main
ARG SKIP_SIGN

# Check if branch/tag exists and clone accordingly
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

# Copy local.properties from the build context. For local builds, this is your local file. CI builds, this file is created by a previous workflow step.
COPY --chown=root:root local.properties ./local.properties

# Build the application
RUN export SKIP_SIGN=${SKIP_SIGN} && just unsigned-release

CMD ["bash"]
