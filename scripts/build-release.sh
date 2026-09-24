#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
: "${SPATIAL_RELEASE_STORE_FILE:?Set the absolute path of your release keystore}"
: "${SPATIAL_RELEASE_STORE_PASSWORD:?Set the release keystore password}"
: "${SPATIAL_RELEASE_KEY_ALIAS:?Set the release key alias}"
: "${SPATIAL_RELEASE_KEY_PASSWORD:?Set the release key password}"
./gradlew :app:assembleRelease :app:testReleaseUnitTest :app:lintRelease -Pkotlin.compiler.execution.strategy=in-process "$@"
