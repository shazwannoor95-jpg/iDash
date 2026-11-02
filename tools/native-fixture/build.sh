#!/bin/bash
set -euo pipefail
cd "$(dirname "$0")/../.."
: "${ANDROID_HOME:?}" "${IDASH_KEYSTORE:?}" "${IDASH_STORE_PASSWORD:?}"
OUT="$PWD/build/native-tests";BT="$ANDROID_HOME/build-tools/36.0.0";JAR="$ANDROID_HOME/platforms/android-35/android.jar"
mkdir -p "$OUT/classes" "$OUT/dex"
javac --release 8 -cp "$JAR" -d "$OUT/classes" tools/native-fixture/src/com/shazwan/idash/test/*.java
"$BT/d8" --min-api 26 --lib "$JAR" --output "$OUT/dex" "$OUT/classes/com/shazwan/idash/test/"*.class
for PART in a; do
 cat > "$OUT/$PART.xml" <<MANIFEST
<manifest xmlns:android="http://schemas.android.com/apk/res/android" package="com.shazwan.idash.test.$PART" android:versionCode="1" android:versionName="1.0"><uses-sdk android:minSdkVersion="26" android:targetSdkVersion="27"/><application android:label="iDash Native Test $PART" android:theme="@android:style/Theme.Material.NoActionBar" android:allowBackup="false"><activity android:name="com.shazwan.idash.test.Main" android:exported="true" android:resizeableActivity="true" android:allowEmbedded="true"><intent-filter><action android:name="android.intent.action.MAIN"/><category android:name="android.intent.category.LAUNCHER"/></intent-filter></activity><service android:name="com.shazwan.idash.test.Bridge" android:permission="com.shazwan.idash.permission.CONTROL" android:exported="true"/></application></manifest>
MANIFEST
 "$BT/aapt2" link -o "$OUT/$PART-base.apk" -I "$JAR" --manifest "$OUT/$PART.xml"
 (cd "$OUT/dex" && zip -q "$OUT/$PART-base.apk" classes.dex)
 "$BT/zipalign" -f 4 "$OUT/$PART-base.apk" "$OUT/$PART-aligned.apk"
 "$BT/apksigner" sign --ks "$IDASH_KEYSTORE" --ks-key-alias idash --ks-pass env:IDASH_STORE_PASSWORD --out "$OUT/iDash-Native-Test-$PART.apk" "$OUT/$PART-aligned.apk"
 "$BT/apksigner" verify "$OUT/iDash-Native-Test-$PART.apk"
done
