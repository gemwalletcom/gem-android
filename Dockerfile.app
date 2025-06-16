# This Dockerfile is used to build the Android app (no signing).
ARG BASE_IMAGE_TAG=latest
FROM gem-android-base:${BASE_IMAGE_TAG}

# Arguments for current tag and skip sign
ARG TAG
ARG SKIP_SIGN

# Clone the repository
RUN git clone --depth 1 --recursive --branch $TAG https://github.com/gemwalletcom/gem-android.git $HOME/gem-android

WORKDIR $HOME/gem-android

# Copy local.properties from the build context. For local builds, this is your local file. CI builds, this file is created by a previous workflow step.
COPY --chown=root:root local.properties ./local.properties

# Build the application
RUN export SKIP_SIGN=${SKIP_SIGN} && just unsigned-release

CMD ["bash"]
