FROM --platform=linux/amd64 gem-android-base:latest

ARG TAG
ARG BUILD_MODE=release

# Set up entrypoint to ensure environment variables are loaded
ENTRYPOINT ["/bin/bash", "-c", "source $HOME/.bashrc && exec $0 \"$@\"", "--"]

# Clone the repository
RUN git clone --recursive --depth 1 --branch $TAG https://github.com/gemwalletcom/gem-android.git $HOME/gem-android

# Set the working directory
WORKDIR $HOME/gem-android

# Generated models and kotlin bindgen are commited to the repository, so no need to generate here
# gemstone is built by cargo-ndk along with gradle

RUN make install-wallet-core

RUN touch local.properties && make unsigned-release

CMD ["bash"]