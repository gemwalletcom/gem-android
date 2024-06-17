#!/bin/bash
set -e

java -jar ./bundletool-all-1.16.0.jar build-apks --bundle=../app/build/outputs/bundle/release/app-release.aab --output=app-release.apks --mode=universal
unzip -o app-release.apks -d app-release
