FROM gem-android-base:latest

ARG TAG
ARG BUILD_MODE
ARG GITHUB_USER
ARG GITHUB_TOKEN

# Set up entrypoint to ensure environment variables are loaded
ENTRYPOINT ["/bin/bash", "-c", "source $HOME/.bashrc && exec $0 \"$@\"", "--"]

# Clone the repository
RUN git clone --depth 1 --recursive --branch $TAG https://github.com/gemwalletcom/gem-android.git $HOME/gem-android

# Set the working directory
WORKDIR $HOME/gem-android

RUN make generate-models

RUN unsigned-release

CMD ["bash"]
