FROM gem-android-base:latest

ARG TAG
ARG BUILD_MODE=release
ARG SKIP_SIGN=true

# Set up entrypoint to ensure environment variables are loaded
ENTRYPOINT ["/bin/bash", "-c", "source $HOME/.bashrc && exec $0 \"$@\"", "--"]

# Clone the repository
RUN git clone --depth 1 --recursive --branch $TAG https://github.com/gemwalletcom/gem-android.git $HOME/gem-android

# Set the working directory
WORKDIR $HOME/gem-android

# Generated models and kotlin bindgen are commited to the repository, so no need to generate here
# gemstone is built by cargo-ndk along with gradle

RUN make install-wallet-core

RUN touch local.properties && SKIP_SIGN=${SKIP_SIGN} make release

CMD ["bash"]
