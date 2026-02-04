#!/usr/bin/env bash
set -euo pipefail

arch="$(dpkg --print-architecture)"
extra=""

if [ "$arch" = "arm64" ]; then
  dpkg --add-architecture amd64

  if ! grep -q '^Architectures:' /etc/apt/sources.list.d/ubuntu.sources; then
    sed -i '/^Types: deb$/a Architectures: arm64' /etc/apt/sources.list.d/ubuntu.sources
  fi

  . /etc/os-release
  codename="${VERSION_CODENAME:-${UBUNTU_CODENAME:-}}"
  if [ -z "$codename" ]; then
    echo "Missing Ubuntu codename in /etc/os-release" >&2
    exit 1
  fi

  cat > /etc/apt/sources.list.d/ubuntu-amd64.sources <<EOF
Types: deb
URIs: http://archive.ubuntu.com/ubuntu
Suites: ${codename} ${codename}-updates ${codename}-backports
Components: main universe restricted multiverse
Architectures: amd64
Signed-By: /usr/share/keyrings/ubuntu-archive-keyring.gpg

Types: deb
URIs: http://archive.ubuntu.com/ubuntu
Suites: ${codename}-security
Components: main universe restricted multiverse
Architectures: amd64
Signed-By: /usr/share/keyrings/ubuntu-archive-keyring.gpg
EOF

  extra="libc6:amd64 libstdc++6:amd64 zlib1g:amd64 libtinfo6:amd64 libgcc-s1:amd64"
fi

apt-get update
apt-get install -y --no-install-recommends \
  libc6:${arch} \
  libstdc++6:${arch} \
  zlib1g:${arch} \
  libtinfo6:${arch} \
  ca-certificates \
  ${extra}
rm -rf /var/lib/apt/lists/*
