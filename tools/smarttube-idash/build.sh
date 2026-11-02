#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/../.."
: "${ANDROID_HOME:?}" "${APKEDITOR_SOURCE:?}" "${IDASH_KEYSTORE:?}" "${IDASH_STORE_PASSWORD:?}"
input=$(realpath "${1:?Official SmartTube32.68 stable APK}")
python3 - "$input" <<'PY'
import hashlib,sys
assert hashlib.sha256(open(sys.argv[1],'rb').read()).hexdigest()=='45a36e62110bcb866527ba8e9ab876f4122e1143647bbd0a97362d13803f6b94','Unreviewed input'
PY
[[ $(git -C "$APKEDITOR_SOURCE" rev-parse HEAD) == 5d94bc08565e30e3e3bb7accbd83138e4d6a8e45 ]]
b="$ANDROID_HOME/build-tools/36.0.0";jar="$ANDROID_HOME/platforms/android-35/android.jar";out="$PWD/build/smarttube-idash"
mkdir -p "$out/classes" "$out/bridge" "$out/dex" dist/companions
"$b/apksigner" verify --min-sdk-version 27 "$input"
javac --release 8 -cp "$APKEDITOR_SOURCE/libs/ARSCLib.jar:$jar" -d "$out/classes" tools/smarttube-idash/PatchSmartTube.java
java -cp "$out/classes:$APKEDITOR_SOURCE/libs/*" PatchSmartTube "$input" "$out/patched.apk"
javac --release 8 -cp "$jar" -d "$out/bridge" tools/smarttube-idash/bridge-src/com/shazwan/nexapanel/*.java
"$b/d8" --min-api 26 --lib "$jar" --output "$out/dex" "$out/bridge/com/shazwan/nexapanel/"*.class
python3 - "$out" <<'PY'
from pathlib import Path
import zipfile,sys
p=Path(sys.argv[1])
with zipfile.ZipFile(p/'patched.apk','a') as z:
 assert 'classes4.dex' not in z.namelist()
 z.write(p/'dex/classes.dex','classes4.dex',compress_type=zipfile.ZIP_DEFLATED)
PY
"$b/zipalign" -f -p 4 "$out/patched.apk" "$out/aligned.apk"
"$b/apksigner" sign --ks "$IDASH_KEYSTORE" --ks-key-alias idash --ks-pass env:IDASH_STORE_PASSWORD --out dist/companions/SmartTube-iDash.apk "$out/aligned.apk"
"$b/apksigner" verify --min-sdk-version 27 --max-sdk-version 27 dist/companions/SmartTube-iDash.apk
"$b/zipalign" -c -p 4 dist/companions/SmartTube-iDash.apk
python3 - "$input" <<'PY'
import sys,zipfile
with zipfile.ZipFile(sys.argv[1]) as a,zipfile.ZipFile('dist/companions/SmartTube-iDash.apk') as b:
 names=[n for n in a.namelist() if n.endswith(('.dex','.so'))]
 assert len(names)==11 and all(a.read(n)==b.read(n) for n in names)
 assert b.testzip() is None
print('Preserved original3DEX/8 native libraries; added bridge separately. Not a malware clearance.')
PY
