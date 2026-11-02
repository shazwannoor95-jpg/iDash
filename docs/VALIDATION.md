# Preview validation

Build target: iDash0.1.7, package com.shazwan.idash, minAPI26/target35. Java/native Android views; no bundled native code, therefore no ARM64-only dependency. Actual ARM32/head-unit runtime and1GB performance are still untested.

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

0.1.4: exact final signed preview passed45 focused layout/theme checks, including the three header controls and a hidden/unallocated default panel. Build and lint passed with0 errors and30 warnings inherited from the programmatic panel port (SDK guards, view constructors, localization and touch accessibility). All four font assets match their source hashes; SF Pro regular is also used by the app/dialog theme.

Native panel verification:11 assertions passed with an actual separate-package/UID test app using the same iDash signing key. Its Activity ran on the requested non-primary display and a scaled SurfaceView gesture incremented its own counter. The test invokes the onStop callback to verify display/input-thread cleanup, then hides/disposes the host. Showing the web panel leaves WebView unallocated until explicit Start. This is a fixture and callback-cleanup check, not physical TizenTube playback, live YouTube, pixel-perfect rendering, actual cross-app task-switch certification or an all-app host.

Initial panel instrumentation entry was not registered by the default test manifest; explicit -PidashRunner=com.shazwan.idash.PanelChecks builds its runner. The stock non-writable emulator then failed the advertised-feature gate before starting a native session. The existing writable-system overlay advertises android.software.activities_on_secondary_displays, matching the TEST9278B owner report, and was used for the passing panel suite. Production feature/signature guards stayed intact. A known emulator System UI ANR was present; that specific dialog was dismissed only after the suite, before final screenshots.

Run the suites serially. Build/install the default SmokeChecks test APK for -e layoutOnly true first. Then rebuild :app:assembleDebugAndroidTest -PidashRunner=com.shazwan.idash.PanelChecks, sign/install its test APK with the same key and run that runner. Build the emulator-only fixture with tools/native-fixture/build.sh and install its a variant on the disposable emulator. Never substitute fixture success for ARM32 TizenTube or head-unit results. The distributed companion preserved all original4DEX/2native files and passed API27 signature/alignment verification; physical setup/playback/audio remain untested.

The final search field initially clipped its hint under the imported font; padding/gravity were corrected. The exact final release was then rebuilt/re-signed and both45 layout checks and11 native-panel fixture checks passed again. No live web video or ARM32 TizenTube playback was introduced into that evidence.

The final panel rectangle starts above the left clock card, using the freed upper-right heading area from the annotated layout; left widget positioning is preserved. After this viewport adjustment the exact distributed APK again passed45 layout assertions and11 separate-UID panel assertions. Tests remain scoped as described above.


0.1.5 replaces the active native TizenTube target with a separately namespaced SmartTube candidate, and ports the firmware-specific FM adapter and bounded Saga image viewer. The three requested apps remain dock-only. Current results/hashes are in dist/artifact.json. FM failure without OEM service, no autoplay, mode disposal, car decode/reset/wrapping and bitmap release are covered by PanelChecks. A real separate-UID fixture covers native display confirmation, forwarded touch and release; it is not SmartTube playback evidence. Run PanelChecks by building instrumentation with -PidashRunner=com.shazwan.idash.PanelChecks and signing both target/tests with the same key. Do not install OEM radio fixture doubles on a real head unit.

SmartTube input/output signatures, alignment, manifest and original DEX/native byte preservation are checked separately. The downloaded official stable32.68 APK is not independently cleared after the upstream build-environment compromise; see SMARTTUBE_REVIEW.md. No ARM runtime, stream/login flow or physical audio result is claimed. The owner's previously successful NexaDash FM test is evidence for the original adapter, not a completed iDash hardware test.

An intermediate FM touch-size assertion exposed44dp controls inside48dp rows after margins. Transport/tuning/preset rows were enlarged to52dp, retaining48dp actual targets; the final PanelChecks suite includes a measured transport-height assertion. The intermediate assertion failure was not treated as a passing release result.

Final0.1.5 exact signed preview:45 focused layout/theme assertions and21 panel assertions PASS on API27. Build/lint (zero errors,37 warnings), v2/v3 signing and ZIP alignment pass. ARM SmartTube and physical iDash FM remain untested.

0.1.6 verification targets the revised glass, thumbnail gallery, custom image import,60dp dock and pinned prayer card. WallpaperChecks uses a generated solid-color image on a disposable emulator to verify exact-byte copying, bounded decode, backdrop update, invalid/oversized input preservation, gallery entries/dismissal and panel-view preservation. It does not import owner images. An early dismissal assertion ran before Android's posted OnDismiss callback; it now waits for idle before checking cleanup. SmokeChecks adds deterministic next-prayer/after-midnight/missing-coverage cases plus on-screen card/dock checks, including a1024×480/font-scale1.3 profile. Current results/hashes are recorded in dist/artifact.json. The native player/FM adapter are unchanged from0.1.5; their prior fixture pass is historical, not a new physical test.

Final0.1.6:53 focused layout/prayer assertions PASS at1024×600 and53 PASS at1024×480/font-scale1.3;13 wallpaper assertions PASS on the exact signed preview. Build/lint, signature and alignment pass. Physical performance remains unmeasured. The inline owner red/orange wallpaper is not bundled; Choose from Files supports that image from the device.

Follow-up to owner screenshot: the independent pinned prayer card left a short clock scroll viewport, visibly cutting the clock card. The source now places clock, prayers and calendar in one scroll flow, with prayers before calendar. On short screens scroll to see lower content. This follow-up is not included in the published0.1.6 APK; its earlier pinned-card tests do not certify this new layout.

0.1.7 release fixes the separate scroll/pinned-card cut-off shown by the owner. Clock, JAKIM and calendar now share one scroll container, with JAKIM before calendar; the scroll bar is enabled. Short screens may require scrolling to lower cards. Updated checks assert shared parent and non-overlapping clock/prayer bounds, while retaining dock touch checks and prayer date/countdown coverage. Current results are in dist/artifact.json. Native/FM/car and wallpaper pipelines are unchanged; their earlier suite results remain historical.

The official SmartTube stable URL was downloaded again:32.68/code2458 and original SHA25645a36e62110bcb866527ba8e9ab876f4122e1143647bbd0a97362d13803f6b94, identical to the companion input. No new upstream version was found at that official stable link. Its security warning remains. This is static provenance/ABI/minimum-SDK evidence, not physical playback or malware certification. tools/package-bundle.py verifies each signed APK hash before ZIP packaging and excludes private inputs.

Final0.1.7 exact signed APK:54 focused layout/prayer assertions PASS at1024×600 and54 PASS at1024×480/font-scale1.3. Build/lint (zero errors,37 warnings), signing/alignment pass. No new physical-player certification.
