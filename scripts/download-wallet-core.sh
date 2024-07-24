#!/bin/bash
set -e

# get current abs path
base_path=$(dirname "$0")
libs_path=${base_path}/../libs
version=$1
repo_base=https://github.com/gemwalletcom/wallet-core-release/releases/download

if [ -z ${version} ]; then
    echo "Please provide version"
    echo "Usage: ./download-wallet-core.sh <version>"
    exit 1
fi

mkdir -p ${libs_path}

files=("wallet-core-${version}.aar" \
"wallet-core-${version}-sources.jar" \
"wallet-core-proto-${version}.jar" \
"wallet-core-proto-${version}-sources.jar" \
)

for file in "${files[@]}"; do
    if [ ! -f "${libs_path}/${file}" ]; then        
        echo "Downloading ${file}"
        output_file=${libs_path}/${file}
        output_file_sha1=${libs_path}/${file}.sha1

        # download files
        curl -sSL -o ${output_file} ${repo_base}/${version}/${file}
        curl -sSL -o ${output_file_sha1} ${repo_base}/${version}/${file}.sha1

        # check checksum
        if [[ $(shasum ${output_file} | awk '{print $1}') != $(cat ${output_file_sha1}) ]]; then
            echo "Checksum mismatch for ${file}"
            exit 1
        fi
    fi
done
