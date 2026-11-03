# SmartTube candidate provenance · 2026-11-02

Official repository: https://github.com/yuliskov/SmartTube

Stable source tag32.68s:94553dd7151e8a911926ef2c9e3ef30fca007b48. The application source is MIT (docs/licenses/SmartTube-MIT.txt); bundled dependencies retain their respective licenses. This adaptation is not an upstream build or endorsement.

Input URL: https://github.com/yuliskov/SmartTube/releases/download/latest/smarttube_stable.apk

Input version32.68/code2458, package org.smarttube.stable, minSDK17/target34. SHA256:45a36e62110bcb866527ba8e9ab876f4122e1143647bbd0a97362d13803f6b94. Official input certificate SHA256:109735aa24fe85e2e1623ebb3b1ecc4bf4263f4c6374e4411d646bfa2eda482a. The moving latest URL is not a reproducibility pin: the build tool rejects any different input hash.

The official README read during this review explicitly reports that the maintainer's development environment was compromised and some builds may be affected; it states the environment was restored and new builds scanned, and warns old keys may have been compromised. Those are upstream statements, not independently verified security results. No VirusTotal clearance or independent binary audit was established here. Re-signing does not remove malicious code. The derivative remains an experimental candidate; it is not silently installed, auto-downloaded or executed by iDash.

Static evidence: valid input APK signature; three original DEX and eight .so files (four each armeabi-v7a/arm64-v8a); portable manifest minimum compatible with API27. Output preserves every original DEX/native library exactly and adds a separately compiled bridge. A matching iDash signature permission protects its input service. All screens use a common isolated task affinity and standard launch mode because Splash navigates to other Activities using NEW_TASK. This manifest change does not prove display continuity through all upstream navigation paths.

Rebuild with JDK17, SDK35/build-tools36, pinned APKEditor5d94bc08565e30e3e3bb7accbd83138e4d6a8e45 and your own iDash matching signing key:

```bash
export APKEDITOR_SOURCE=/path/to/pinned/APKEditor
# Set ANDROID_HOME, IDASH_KEYSTORE, IDASH_STORE_PASSWORD privately; alias idash.
bash tools/smarttube-idash/build.sh /path/to/pinned/smarttube_stable.apk
```

Patch source and complete added bridge source are included in tools/smarttube-idash. The upstream app is binary-preserved, not rebuilt from source here. Result metadata/hashes are in dist/companions/artifact.json. ARM playback cannot be certified on the x86 API27 fixture emulator. Physical setup, account flow, navigation, actual video/audio and memory remain pending. No private signing material is included.

For iDash0.1.7 packaging, the official stable URL and README were fetched again on2026-11-02. The input remained32.68/code2458 with the same SHA256 and certificate recorded above; the compromised-build-environment announcement remains. The existing byte-preserving iDash companion is therefore reused, not presented as a newly released upstream build or newly cleared binary.
