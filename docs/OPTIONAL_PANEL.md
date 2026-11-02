# Optional media panel · iDash0.1.5

The header groups **Panel**, **Customize** and **Control Center**. Panel is hidden and unallocated on a fresh launch. Tap Panel to open the upper-right dashboard rectangle, independent of the left heading/widgets; tap Panel again or Close to dispose it. The dock shortcuts remain small. No mode automatically starts playback or an external app.

## YouTube web

The default mode adapts NexaDash's DashWebPlayer: explicit Start, mobile YouTube/search, Back, Home, Stop, bounded link handling and the existing fullscreen/custom-view return path. Creating the panel does not create a WebView or perform a network request. It releases the browser on pause/hide. Site/login/codec/provider/network restrictions may affect playback; a UI fixture is not a live YouTube verification.

## Radio FM · antenna

Uses the existing MediaTek FmtestService3.1 or GalaRadio RadioService2.3.24 from the owner's firmware. This is antenna FM, not Internet radio. Choose Auto or the matching source and press Start FM. Tune, station list and six hold-to-save presets are native iDash controls. The internal backend waits for firmware initialization/station-list completion before tuning; band/area/sensitivity settings stay firmware-owned. Stop/hide/background closes only the FM session requested by this controller. It never destroys the OEM service. Options → diagnostics shows the command/result report.

The owner previously confirmed the equivalent NexaDash FM adapter worked. This iDash port still needs physical audio/tuning confirmation. Absent, inaccessible or different-version services produce an explanatory error. A generic Android device does not acquire FM hardware through this APK.

## Kereta

Eight rendered image angles of the owner's red Proton Saga. Drag to rotate between images, pinch to zoom, double-tap to reset. The existing authorized plate-bearing application atlas is reused; this is not live3D, CAN bus, telemetry or a camera feed. One bounded decoded atlas is held while shown, released on pause/hide. No video decoder is started.

## SmartTube native candidate

Replaces the previous TizenTube selection. Install iDash and the separate **SmartTube-iDash.apk**, then Panel → Mode → SmartTube → Start. Package `com.shazwan.idash.smarttube` and signature permission `com.shazwan.idash.permission.CONTROL` are isolated from official SmartTube and NexaDash apps. Ordinary official/differently signed apps cannot be hosted through this path. Existing installations/data are not replaced. The update migrates the old iDash TizenTube panel selection to SmartTube and redirects only the old iDash TizenTube dock assignment; other user-selected dock apps remain unchanged. It does not uninstall any app. Updates of this derivative must use the iDash signer, not the upstream updater.

**Candidate boundary:** upstream reports a compromised build environment affecting some releases. Input is the official32.68 stable artifact, pinned by hash, but no independent malware clearance has been established. Read [SmartTube provenance](SMARTTUBE_REVIEW.md). This companion is optional and separate from the launcher. Neither an official signature nor byte preservation proves safety or correct playback.

The original three DEX files and eight ARM32/ARM64 libraries are preserved; one separate lifecycle/input bridge DEX is added. Providers/permissions/package/task affinity are isolated, all Activity screens permit secondary-display hosting, and it is re-signed. Source/rebuild instructions: tools/smarttube-idash. Native setup, multi-Activity navigation, playback/audio, remote/touch behavior and real head-unit performance remain unverified. SmartTube is a TV application; its small-panel touch experience may be limited. Use Open outside panel for a separate full-screen launch when needed.

## Lifecycle and input

DashNativeFrame, DashNativeProxy, DashNativeGeometry and DashWebPlayer are adapted from NexaDash with MIT attribution retained. A private virtual display and cooperative signed input service provide the native path; no arbitrary app-task embedding or privileged task/input grants are introduced. API27 may pause the host when another Activity resumes on the secondary display; the native session is kept while its dashboard remains visible and released in onStop/hide/destruction. Background discovery and wallpaper decoding update without replacing a running panel. Explicit appearance/mode changes rebuild and stop the session; press Start again afterwards.

The test fixture under tools/native-fixture uses a separate package/UID and the same signature permission. It is only a test app and must not be described as SmartTube playback. It belongs on disposable emulators; the build does not install anything on a physical unit. Maps companion is not included. Nothing is routed through DiAuto.

The three dock icons stay in the dock, with42dp imagery and48dp-or-larger touch tiles. The dashboard Favorites section remains removed. Modes are YouTube web, Radio FM, Kereta and SmartTube. Existing0.1.4 native-mode preference migrates to SmartTube; it does not automatically open FM.

## Reproducing the native fixture profile

Use a disposable API27 AVD with its writable system overlay. If it lacks the secondary-display feature advertisement, the fixture XML at tools/native-fixture/secondary-display.xml can be placed under /system/etc/permissions/ in that **emulator only** (adb -s emulator-5554 root/remount, push the XML, reboot). Check pm list features after boot. This changes the feature advertisement for a test profile; it does not create framework capabilities, certify stock Android builds or instruct firmware changes on a head unit. The real TEST9278B already reported this feature. The normal app gate remains unchanged.
