#!/bin/bash

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

# Configuration - Choose between Release or Debug
FRAMEWORK_NAME=${1:-Shared}
CONFIGURATION=${2:-Release}
FOLDER_NAME="$( echo "${CONFIGURATION}" | awk '{print tolower($0)}')"

# Step 1 - Build the XCFramework
(cd "$SCRIPT_DIR/.." ; ./gradlew ":shared:assemble${FRAMEWORK_NAME}${CONFIGURATION}XCFramework")

# Step 2 - Copy the XCFramework
cp -R "$SCRIPT_DIR/../shared/build/XCFrameworks/${FOLDER_NAME}/${FRAMEWORK_NAME}.xcframework" "$SCRIPT_DIR/../package"
