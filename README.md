# iDash

An iPad-inspired Android launcher for landscape car head units. Floating glass dock, rounded widgets, an App Library and a Control Center, built with native Android views and no third-party runtime dependencies.

![iDash dashboard](docs/dashboard.png)

[MRiOS-themed App Library preview (0.1.2)](docs/app-library.png) · [Earlier Control Center preview (0.1.0)](docs/control-center.png)

## Try the preview

[Download iDash 0.1.3 preview](dist/iDash-0.1.3-preview.apk?raw=true) · Android8.0/API26 or newer · package `com.shazwan.idash`

Install alongside your existing launcher. Open **iDash** from the app list first. To make it your home app, open **Control Center → Choose default launcher** and select iDash yourself. Retain the original launcher as a fallback. Android installation may ask you to allow installs from your file manager/browser.

## Included

- Landscape dashboard without a Favorites section, with translucent rounded cards,24 MRiOS wallpapers and two original gradient palettes.
- Local clock/date, a browsable Gregorian calendar and an official JAKIM calendar/prayer-times widget replacing Power. Select your zone explicitly; no GPS permission is requested.
- Three shortcuts — Bluetooth, Files, YouTube — in the floating dock only. Hold a favorite to select its app, or hold an App Library icon to assign it. Assignments persist across restarts.
- App Library with installed application icons, live name search, Refresh and Back/Done navigation.
- Control Center with Android Wi-Fi, Bluetooth, display, sound and default-home settings links, plus a real media-volume slider. OEM hardware may handle volume differently.
- Customize: calendar/JAKIM visibility,24/12-hour time, MRiOS icon theme and Wallpapers gallery. MRiOS wallpaper47 is the default unless a rose gradient was previously selected. Supported App Library apps use the MRiOS image theme; unmatched apps retain their own icons.

External apps open normally in their own screens. No native app embedding, Maps rendering, OEM radio driver, media-session playback controls, weather service or privileged system toggles are implemented in this preview. Dock shortcuts launch an installed app. No runtime permission prompts, root or ADB grants are needed for normal launcher use; the manifest requests normal audio-settings and Internet permissions. Internet is used only when you request a JAKIM timetable refresh.

This is an original iOS/iPadOS-inspired design adapted for a head unit. It is not a verified pixel-perfect reproduction of an iOS27.2/iPadOS27 release. The36 iOS-style PNG icons and24 wallpaper images are extracted from the owner-supplied MRiOS V2 APK; see docs/MRIOS_REVIEW.md and NOTICE for their separate provenance. Preview screenshots use a disposable Android emulator, not an Apple device or a physical head-unit result.

## Build

Open in Android Studio, or use JDK17, SDK platform35 and Gradle8.9:

```bash
./gradlew :app:assembleDebug :app:lintDebug :app:assembleDebugAndroidTest
```

Set `ANDROID_HOME` or create a private `local.properties` with `sdk.dir`. Debug APK: `app/build/outputs/apk/debug/app-debug.apk`. The Gradle wrapper is pinned with a distribution SHA256. An existing Gradle8.9 installation can run the same tasks if distribution download is unavailable.

The downloadable preview uses a separate iDash signing key kept outside this repository. Locally generated debug APKs have a different signer: keep using your own key for your builds, and do not uninstall an existing preview without backing up any settings you want to retain. To build a matching update you need the original iDash signing key; it is never bundled here.

For a separately signed preview, set `IDASH_KEYSTORE` and `IDASH_STORE_PASSWORD` (key alias `idash`), install SDK build-tools36.0.0, then run `bash tools/build-preview.sh`. `IDASH_GRADLE` optionally selects an existing Gradle executable. No passwords should be placed in Git or shared shell history.

See [validation](docs/VALIDATION.md) and [design brief](docs/DESIGN.md). The first target is API27/ARM32,1024×600,1GB RAM; physical-head-unit performance remains to be measured. NexaDash is a separate, paused project.

## JAKIM calendar

Tap the JAKIM widget to choose a zone, browse a date or refresh from the official e-Solat endpoint. SGR01, SGR02, SGR03 and WLY01 include the existing official2026 caches from NexaDash; no zone is silently inferred. Other valid Malaysian zone codes require a download. Dates outside available coverage show no timetable, rather than reusing an expired day. The displayed Hijri date comes from the matching official row. Times use Malaysia timezone and the device clock/date; this preview does not verify GPS time, schedule alarms or calculate substitute prayer times.

Online refresh validates zone/status, dates and time ordering, limits responses to2MiB and retains valid caches on failure. Live endpoint/physical-device network behavior has not been certified here. Source/provenance is in assets/jakim/source.json; adapted parser attribution is in NOTICE and docs/licenses/NexaDash-MIT.txt.
