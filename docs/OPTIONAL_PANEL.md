# Optional media panel · iDash0.1.4

The header groups **Panel**, **Customize** and **Control Center**. Panel is hidden and unallocated on a fresh launch. Tap Panel to open the upper-right dashboard rectangle, independent of the left heading/widgets; tap Panel again or Close to dispose it. The dock shortcuts remain small. No mode automatically starts playback or an external app.

## YouTube web

The default mode adapts NexaDash's DashWebPlayer: explicit Start, mobile YouTube/search, Back, Home, Stop, bounded link handling and the existing fullscreen/custom-view return path. Creating the panel does not create a WebView or perform a network request. It releases the browser on pause/hide. Site/login/codec/provider/network restrictions may affect playback; a UI fixture is not a live YouTube verification.

## TizenTube native

Install/update iDash, then install **TizenTube-iDash-arm32.apk** from dist/companions. In Panel → Mode choose **TizenTube · native companion**, then Start. First-use setup can be completed through the native input tools → Open outside panel, then return and Start again. The input tools include Back and a text-entry bridge for a focused field.

This is a separate package, `com.shazwan.idash.tizentube`, with iDash's signing key and `com.shazwan.idash.permission.CONTROL`. It does not replace NexaDash's `com.shazwan.nexadash.tizentube.next` or its installed data. Ordinary Play Store apps and differently signed NexaDash companions cannot be hosted by this signature-protected iDash path.

The ARM32 candidate retains all four original DEX and two native libraries byte-for-byte; its manifest identity/provider/permission/task names are isolated and it is re-signed. Rebuild source is under tools/tizentube-idash; it consumes the already-published reviewed TizenTube Next APK and pinned APKEditor5d94bc08565e30e3e3bb7accbd83138e4d6a8e45. The signing key is private. Source byte preservation, signing and alignment do not certify playback, account/setup, audio or real head-unit operation. The ARM32 app is not executed by the x86 emulator tests.

## Lifecycle and input

DashNativeFrame, DashNativeProxy, DashNativeGeometry and DashWebPlayer are adapted from NexaDash with MIT attribution retained. A private virtual display and cooperative signed input service provide the native path; no arbitrary app-task embedding or privileged task/input grants are introduced. API27 may pause the host when another Activity resumes on the secondary display; the native session is kept while its dashboard remains visible and released in onStop/hide/destruction. Background discovery and wallpaper decoding update without replacing a running panel. Explicit appearance/mode changes rebuild and stop the session; press Start again afterwards.

The test fixture under tools/native-fixture uses a separate package/UID and the same signature permission. It is only a test app and must not be described as TizenTube playback. It belongs on disposable emulators; the build does not install anything on a physical unit. No FM radio, Maps companion or vehicle preview mode is added by this first port. Nothing is routed through DiAuto.

The three dock icons stay in the dock, with42dp imagery and48dp-or-larger touch tiles. The dashboard Favorites section remains removed. Initial modes are YouTube web and TizenTube native.

## Reproducing the native fixture profile

Use a disposable API27 AVD with its writable system overlay. If it lacks the secondary-display feature advertisement, the fixture XML at tools/native-fixture/secondary-display.xml can be placed under /system/etc/permissions/ in that **emulator only** (adb -s emulator-5554 root/remount, push the XML, reboot). Check pm list features after boot. This changes the feature advertisement for a test profile; it does not create framework capabilities, certify stock Android builds or instruct firmware changes on a head unit. The real TEST9278B already reported this feature. The normal app gate remains unchanged.
