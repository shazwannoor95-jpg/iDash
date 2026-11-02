# Preview validation

Build target: iDash0.1.3, package com.shazwan.idash, minAPI26/target35. Java/native Android views; no bundled native code, therefore no ARM64-only dependency. Actual ARM32/head-unit runtime and1GB performance are still untested.

Build checks: assembleDebug, assembleDebugAndroidTest and lintDebug passed using JDK17, Gradle8.9 and SDK35. Lint: zero errors, two warnings (intentional landscape activity and modern Android data-extraction declaration). Signature v2/v3 and ZIP alignment verified for the downloadable, independently signed iDash preview.

UI checks use an installed iDash previews and matching-signed instrumentation on a disposable API27 x86_64 emulator,1024×600 at160dpi. Coverage includes actual app discovery, on-screen bounds/touch heights, local clock, calendar/power visibility, dock-to-library navigation, search/empty/reset, Back, favorite assignment/rebuild/restart persistence, Control Center and external Android Settings return. The test source is in app/src/androidTest/java/com/shazwan/idash/SmokeChecks.java. The original0.1.0 run passed30 assertions; the current result is recorded in dist/artifact.json. See dist/artifact.json for the artifact hashes and detailed test boundaries.

Run on a disposable emulator, not a head unit containing personal settings:

```bash
./gradlew :app:assembleDebug :app:assembleDebugAndroidTest :app:lintDebug
adb -s emulator-5554 install -r app/build/outputs/apk/debug/app-debug.apk
adb -s emulator-5554 install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb -s emulator-5554 shell am instrument -w com.shazwan.idash.test/com.shazwan.idash.SmokeChecks
```

Both APKs must have the same signer. The published preview is signed separately from local debug builds; use a clean emulator or sign the tests with the same private iDash key for testing that exact preview. The tests intentionally change their target app's favorites/widget preferences. Do not run them against an owner's configured installation.

An emulator System UI ANR obscured the first attempted README capture after the initial26-assertion run had passed. That exact system dialog was closed before further testing and screenshots. It was not treated as an iDash crash, nor as a physical performance result.

Screenshots show real emulator output. No tile/weather/media data or real car hardware behavior is simulated. There are no FPS, CPU, memory, all-app-launch or physical-device performance certifications in this preview.

Installation smoke check on a head unit should keep the existing launcher, launch iDash normally first, assign the desired actual apps and check touch, clock, app return and volume behavior before choosing it as the default home app.

The expanded test runner first hit a cross-app INJECT_EVENTS restriction and then an accessibility-root timing race. It now uses UiAutomation Back for the external app and waits for a non-null window root. These were runner changes; no application privileges were added. The original0.1.0 final30-assertion run passed on that exact preview. Coordinate-driven Control Center also opened correctly; a frame captured too early was replaced after presentation settled on the slow software emulator.

0.1.1 changes Favorites and dock to Bluetooth → Files → YouTube in one centered row. The existing suite now also checks the exact order, absence of removed shortcuts and migration of the prior Bluetooth assignment. Bluetooth/Files use named preference keys; an old Video assignment is reused for YouTube only when its package is YouTube or TizenTube. Earlier library/Control Center screenshots are labeled0.1.0; the dashboard image is refreshed for0.1.1.

0.1.1 verification: build/lint/signature/alignment pass. Both UI runs reached29 assertions, including the requested order, absence of removed favorites, migration and persistence. The full suite failed on Control Center accessibility visibility while an emulator System UI ANR was observed in front; the isolated retry used stock Home. Do not report a full33-assertion pass for0.1.1. The known system dialog was dismissed after the test stopped to inspect the dashboard.

After the0.1.1 layout precursor run, the final build adds vibrant original vector marks and the two MRiOS PNGs documented in MRIOS_REVIEW.md. Build/lint/signature/alignment passed again; that final signed APK was installed and visually inspected on API27 and its dashboard screenshot updated. The full functional suite was not rerun on this asset/palette revision.

0.1.2: the exact signed preview passed33 focused API27 layout/theme assertions (`-e layoutOnly true`). This run checks the lower favorites row, imported icon/wallpaper counts, bounded background decode, discovery/search, assignment migration and persistence. It intentionally skips unchanged Control Center and external Settings checks; do not label it a full37-assertion pass. All60 imported image files match the source manifest hashes. Build, lint, APK signature and alignment passed.

After the0.1.2 focused run passed, the known emulator System UI ANR obscured the first dashboard capture. That specific system dialog was dismissed after testing and the dashboard was recaptured. This is not a resolution or certification of the emulator System UI issue.

0.1.3: the exact signed APK passed43 focused API27 assertions. New coverage: dashboard shortcuts exist only once in the dock; Power is absent; JAKIM widget is present;365 official cache rows parse;2026-11-02 is available;2027 dates are not filled from2026; Malay date/month and Malaysia midnight boundary parse correctly; zone selection loads off the UI thread and JAKIM visibility persists. All four bundled zone files match the recorded source hashes. Build/lint/signature/alignment passed. Live HTTP refresh and physical-head-unit behavior remain untested. The known emulator System UI ANR was visible after the suite; it was dismissed after testing before capturing the dashboard. The screenshot uses explicitly selected SGR02 test preferences; fresh installs ask the user to choose a zone.
