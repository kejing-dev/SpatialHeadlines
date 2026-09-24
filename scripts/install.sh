#!/usr/bin/env bash
set -euo pipefail
if [[ $# -ne 2 ]]; then
  echo "Usage: $0 DEVICE_SERIAL /path/to/SpatialHeadlines-1.4.1-release.apk" >&2
  exit 2
fi
adb -s "$1" install -r "$2"
adb -s "$1" shell am start -n com.headline.news/.platform.LaunchActivity
