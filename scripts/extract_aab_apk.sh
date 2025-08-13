#!/bin/bash
set -euo pipefail

BUNDLETOOL_VERSION="1.18.1"

get_base_path() {
    dirname "$0"
}

ensure_bundletool() {
    local base_path="$1"
    local bundletool_jar="${base_path}/bundletool-all-${BUNDLETOOL_VERSION}.jar"
    
    if [ ! -f "$bundletool_jar" ]; then
        echo "Downloading bundletool-all-${BUNDLETOOL_VERSION}.jar..."
        curl -L --fail -o "$bundletool_jar" "https://github.com/google/bundletool/releases/download/${BUNDLETOOL_VERSION}/bundletool-all-${BUNDLETOOL_VERSION}.jar"
    fi
    
    echo "$bundletool_jar"
}

aab_files_exist() {
    local base_path="$1"
    [ -n "$(find "${base_path}/../app/build/outputs/bundle" -name "*.aab" -type f -print -quit)" ]
}

get_output_name() {
    local aab_file="$1"
    local flavor_variant=$(basename $(dirname "$aab_file"))
    echo "app-${flavor_variant}"
}

process_aab_file() {
    local aab_file="$1"
    local bundletool_jar="$2"
    local base_path="$3"
    
    local output_name=$(get_output_name "$aab_file")
    
    echo "Processing: $aab_file -> ${output_name}"
    
    if [ -d "${base_path}/${output_name}" ]; then
        rm -rf "${base_path}/${output_name}"
    fi
    if [ -f "${base_path}/${output_name}.apks" ]; then
        rm "${base_path}/${output_name}.apks"
    fi
    
    java -jar "$bundletool_jar" build-apks \
        --bundle="$aab_file" \
        --output="${base_path}/${output_name}.apks" \
        --mode=universal
    
    unzip -o "${base_path}/${output_name}.apks" -d "${base_path}/${output_name}"
    rm "${base_path}/${output_name}.apks"
    
    echo "Extracted to: ${base_path}/${output_name}/"
}

main() {
    local base_path=$(get_base_path)
    local bundletool_jar=$(ensure_bundletool "$base_path")
    if ! aab_files_exist "$base_path"; then
        echo "No AAB files found in app/build/outputs/bundle/"
        exit 1
    fi
    
    while IFS= read -r -d '' aab_file; do
        process_aab_file "$aab_file" "$bundletool_jar" "$base_path"
    done < <(find "${base_path}/../app/build/outputs/bundle" -name "*.aab" -type f -print0)
}

main "$@"
