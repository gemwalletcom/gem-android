#!/bin/bash
set -euo pipefail

git pull

version=$(grep "versionName = " app/build.gradle.kts | sed 's/.*versionName = "//' | sed 's/".*//')
build=$(grep "versionCode = " app/build.gradle.kts | sed 's/.*versionCode = //' | sed 's/[^0-9].*//')

IFS='.' read -r major minor patch <<< "$version"
new_patch=$((patch + 1))
new_version="${major}.${minor}.${new_patch}"
new_build=$((build + 1))

echo "New version: $new_version (build $new_build)"

# Update version in build.gradle.kts
sed -i '' "s/versionCode = [0-9]*/versionCode = $new_build/" app/build.gradle.kts
sed -i '' "s/versionName = \".*\"/versionName = \"$new_version\"/" app/build.gradle.kts

git add app/build.gradle.kts
git commit -S -m "Bump to $new_version ($new_build)"
#git tag -s "$new_version" -m "$new_version"
#git push
#git push origin "$new_version"

echo "✅ Bumped to $new_version (build $new_build)"