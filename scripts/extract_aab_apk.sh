#!/bin/bash
set -e

# get current abs path
base_path=$(dirname "$0")

rm -rf ${base_path}/app-release*
java -jar ${base_path}/bundletool-all-1.16.0.jar build-apks --bundle=${base_path}/../app/build/outputs/bundle/release/app-release.aab --output=${base_path}/app-release.apks --mode=universal
unzip -o ${base_path}/app-release.apks -d ${base_path}/app-release
