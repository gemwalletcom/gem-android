#!/bin/bash
set -e

BUNDLETOOL_VERSION="1.18.1"

get_base_path() {
    dirname "$0"
}

ensure_bundletool() {
    local base_path="$1"
    local bundletool_jar="${base_path}/bundletool-all-${BUNDLETOOL_VERSION}.jar"
    
    if [ ! -f "$bundletool_jar" ]; then
        echo "Downloading bundletool-all-${BUNDLETOOL_VERSION}.jar..."
        curl -L -o "$bundletool_jar" "https://github.com/google/bundletool/releases/download/${BUNDLETOOL_VERSION}/bundletool-all-${BUNDLETOOL_VERSION}.jar"
    fi
    
    echo "$bundletool_jar"
}

find_aab_files() {
    local base_path="$1"
    find "${base_path}/../app/build/outputs/bundle" -name "*.aab" -type f
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
    
    rm -rf "${base_path}/${output_name}"*
    
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
    local aab_files=$(find_aab_files "$base_path")
    
    if [ -z "$aab_files" ]; then
        echo "No AAB files found in app/build/outputs/bundle/"
        exit 1
    fi
    
    for aab_file in $aab_files; do
        process_aab_file "$aab_file" "$bundletool_jar" "$base_path"
    done
}

main "$@"
