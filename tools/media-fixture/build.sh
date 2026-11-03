#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/../.."
: "${ANDROID_HOME:?}" "${IDASH_STORE_PASSWORD:?}"
b="$ANDROID_HOME/build-tools/36.0.0";jar="$ANDROID_HOME/platforms/android-35/android.jar";out=build/media-fixture
mkdir -p "$out/classes" "$out/dex"
cat > "$out/AndroidManifest.xml" <<'XML'
<manifest xmlns:android="http://schemas.android.com/apk/res/android" package="dev.idash.mediafixture" android:versionCode="1"><uses-sdk android:minSdkVersion="27" android:targetSdkVersion="27"/><application android:label="Media fixture" android:debuggable="true" android:theme="@android:style/Theme.Material.NoActionBar"><activity android:name=".Main" android:exported="true" android:launchMode="singleTask"/></application></manifest>
XML
javac --release 8 -cp "$jar" -d "$out/classes" tools/media-fixture/Main.java
"$b/d8" --min-api 27 --lib "$jar" --output "$out/dex" "$out/classes/dev/idash/mediafixture/"*.class
"$b/aapt2" link -I "$jar" --manifest "$out/AndroidManifest.xml" -o "$out/base.apk"
(cd "$out/dex" && zip -q ../base.apk classes.dex)
"$b/zipalign" -f 4 "$out/base.apk" "$out/aligned.apk"
"$b/apksigner" sign --ks "${IDASH_KEYSTORE:?}" --ks-key-alias idash --ks-pass env:IDASH_STORE_PASSWORD --out "$out/fixture.apk" "$out/aligned.apk"
