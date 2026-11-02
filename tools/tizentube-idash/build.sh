#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/../.."
: "${ANDROID_HOME:?}" "${APKEDITOR_SOURCE:?Pinned APKEditor source directory}" "${IDASH_KEYSTORE:?}" "${IDASH_STORE_PASSWORD:?}"
input=$(realpath "${1:?Path to reviewed TizenTube-NexaDash-Next-arm32.apk}")
python3 - "$input" <<'PY'
import hashlib,json,sys
from pathlib import Path
expected=json.loads(Path('dist/companions/artifact.json').read_text())['original_input_sha256']
assert hashlib.sha256(Path(sys.argv[1]).read_bytes()).hexdigest()==expected,'Input differs from reviewed companion'
PY
[[ $(git -C "$APKEDITOR_SOURCE" rev-parse HEAD) == 5d94bc08565e30e3e3bb7accbd83138e4d6a8e45 ]]
b="$ANDROID_HOME/build-tools/36.0.0";out="$PWD/build/tizentube-idash"
mkdir -p "$out/classes" dist/companions
javac --release 8 -cp "$APKEDITOR_SOURCE/libs/ARSCLib.jar:$ANDROID_HOME/platforms/android-35/android.jar" -d "$out/classes" tools/tizentube-idash/PatchIdashTizen.java
java -cp "$out/classes:$APKEDITOR_SOURCE/libs/*" PatchIdashTizen "$input" "$out/patched.apk"
"$b/zipalign" -f -p 4 "$out/patched.apk" "$out/aligned.apk"
"$b/apksigner" sign --ks "$IDASH_KEYSTORE" --ks-key-alias idash --ks-pass env:IDASH_STORE_PASSWORD --out dist/companions/TizenTube-iDash-arm32.apk "$out/aligned.apk"
"$b/apksigner" verify --min-sdk-version 27 --max-sdk-version 27 dist/companions/TizenTube-iDash-arm32.apk
"$b/zipalign" -c -p 4 dist/companions/TizenTube-iDash-arm32.apk
python3 - "$input" <<'PY'
import sys,zipfile
with zipfile.ZipFile(sys.argv[1]) as a,zipfile.ZipFile('dist/companions/TizenTube-iDash-arm32.apk') as b:
 names=[n for n in a.namelist() if n.endswith(('.dex','.so'))]
 assert len(names)==6 and all(a.read(n)==b.read(n) for n in names)
 assert b.testzip() is None
print('Preserved original4DEX/2ARM32 libraries')
PY
