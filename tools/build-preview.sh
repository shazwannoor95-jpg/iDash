#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
: "${ANDROID_HOME:?Set ANDROID_HOME}" "${IDASH_KEYSTORE:?Set IDASH_KEYSTORE}" "${IDASH_STORE_PASSWORD:?Set IDASH_STORE_PASSWORD}"
"${IDASH_GRADLE:-./gradlew}" :app:assembleDebug "$@"
b="$ANDROID_HOME/build-tools/36.0.0"
mkdir -p dist
"$b/apksigner" sign --ks "$IDASH_KEYSTORE" --ks-key-alias idash --ks-pass env:IDASH_STORE_PASSWORD --out dist/iDash-0.1.5-preview.apk app/build/outputs/apk/debug/app-debug.apk
"$b/apksigner" verify --verbose --min-sdk-version 26 dist/iDash-0.1.5-preview.apk
"$b/zipalign" -c 4 dist/iDash-0.1.5-preview.apk
sha256sum dist/iDash-0.1.5-preview.apk
