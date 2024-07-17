FROM gem-android-base:latest

ARG TAG

# Set up entrypoint to ensure environment variables are loaded
ENTRYPOINT ["/bin/bash", "-c", "source $HOME/.bashrc && exec $0 \"$@\"", "--"]

# Clone the repository
RUN git clone --recursive --depth 1 --branch $TAG git@github.com:gemwalletcom/gem-android.git $HOME

# Create the local.properties file
RUN echo "gpr.user=$GITHUB_USER" >> $HOME/gem-android/local.properties && \
    echo "gpr.key=$GITHUB_TOKEN" >> $HOME/gem-android/local.properties

# Set the working directory
WORKDIR $HOME/gem-android

RUN make generate

RUN unsigned-release

# Commandline access
CMD ["bash"]
