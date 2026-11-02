#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/../.."
: "${ANDROID_HOME:?}" "${IDASH_KEYSTORE:?}" "${IDASH_STORE_PASSWORD:?}"
b="$ANDROID_HOME/build-tools/36.0.0";jar="$ANDROID_HOME/platforms/android-35/android.jar"
mkdir -p build/system-theme dist/system-theme
for part in settings systemui; do
 if [[ $part == settings ]]; then target=com.android.settings; else target=com.android.systemui; fi
 cat > "build/system-theme/$part.xml" <<EOF
<manifest xmlns:android="http://schemas.android.com/apk/res/android" package="com.shazwan.idash.overlay.$part" android:versionCode="1" android:versionName="0.1-experimental"><uses-sdk android:minSdkVersion="27" android:targetSdkVersion="27"/><overlay android:targetPackage="$target" android:isStatic="false" android:priority="1"/><application android:hasCode="false" android:allowBackup="false" android:label="iDash $part palette · experimental"/></manifest>
EOF
 "$b/aapt2" compile --dir "tools/system-theme/res-$part" -o "build/system-theme/$part-res.zip"
 "$b/aapt2" link -o "build/system-theme/$part.apk" -I "$jar" --manifest "build/system-theme/$part.xml" "build/system-theme/$part-res.zip"
 "$b/zipalign" -f 4 "build/system-theme/$part.apk" "build/system-theme/$part-aligned.apk"
 "$b/apksigner" sign --ks "$IDASH_KEYSTORE" --ks-key-alias idash --ks-pass env:IDASH_STORE_PASSWORD --out "dist/system-theme/iDash-$part-overlay.apk" "build/system-theme/$part-aligned.apk"
 "$b/apksigner" verify --min-sdk-version 27 "dist/system-theme/iDash-$part-overlay.apk"
 "$b/zipalign" -c 4 "dist/system-theme/iDash-$part-overlay.apk"
done
