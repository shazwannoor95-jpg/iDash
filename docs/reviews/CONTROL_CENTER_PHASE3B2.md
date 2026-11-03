# Control Center — Phase 3B-2 (local)

Java Native Views / public Android APIs; landscape composition, glass rendering and Phase 3B-1 radio/brightness/volume implementation retained. No release APK, commit or push.

## Implementation

- `ControlMedia.java`: lifecycle-scoped MediaSessionManager active-session listener and MediaController callbacks; selects playing, then paused sessions. Displays title, artist, app and actual playback state; supported action mask gates Previous/Play/Pause/Next. ACTION_PLAY_PAUSE-only sessions use public media-button dispatch. Granted-but-empty and missing/denied access have distinct states. No media polling, artwork loading or audio playback service.
- `MediaAccessService.java`: minimal system-bound NotificationListenerService, only reports connection/disconnection to this package. Does not inspect/store notification contents. Media card opens a disclosure and Notification Listener Access settings; no assumption that access is granted and no MEDIA_CONTENT_CONTROL request. Android's broad notification access remains user controlled.
- `ControlUtilities.java`: reads NotificationManager DND filter. Policy Access disclosure/settings when denied; Off/Priority/Alarms selector writes only after explicit selection. CameraManager torch discovery runs on existing worker; callbacks track actual state/unavailability. CAMERA runtime request is made only after a torch tap. Missing flash/camera/app disables the relevant utility with a visible label. Timer uses SHOW_TIMERS then SET_TIMER; Calculator uses public category then installed launcher-label fallback, without fixed package names; camera uses STILL_IMAGE_CAMERA. Missing/stale intents, SecurityException and unavailable camera are handled. Calculator fallback waits for the existing launcher app list, shows Checking during startup, and refreshes after queued discovery; it does not permanently label a still-loading list as absent.
- `QuickControls.java`, `ControlGlyph.java`: connect existing controls and add original pause glyph; no position/size/layout or glass-engine change. Existing Settings shortcut retained. No QR button exists, so no scanner/SDK added.
- `LauncherActivity.java`: CAMERA permission result and owned-torch best-effort teardown. Manifest adds minimal service, DND/CAMERA declarations, normal SET_ALARM permission for SET_TIMER fallback, optional camera features and utility intent visibility.
- Tests: `MediaUtilitiesChecks.java`, existing `ControlLayoutChecks.java`; separate test-only `tools/media-fixture/` application creates an actual framework MediaSession under a different UID, without audio. Fixture APK stays in build/, never packaged into iDash.

## Validation

- Offline Gradle `:app:assembleDebug :app:lintDebug :app:assembleDebugAndroidTest`: successful. Lint: 0 errors, 59 warnings (including pre-existing FM reflection warning outside this phase; no hidden APIs introduced here).
- API27 emulator: **114 assertions passed** across SystemControlsChecks 17, SettingsChecks 27, ControlLayoutChecks 41 and MediaUtilitiesChecks 29. Final layout and media runs used the final debug APK. Includes actual Wi-Fi/volume/brightness regression, denied/granted Modify System Settings and observer lifecycle.
- MediaUtilitiesChecks: **29 passed**: denied/granted/revoked Notification Listener Access, granted empty session, real separate-UID MediaSession, Play/Pause/Next/Previous and metadata callbacks, toggle-only action mask, explicit DND selection, lifecycle detach/reattach, no-flash and missing intent; actual Timer and Calculator Activity launches and foreground-return recovery. The first utility-launch run stalled while waiting for the background app to become idle and was stopped, not counted as a pass; the corrected harness waits for lifecycle transitions instead.
- Actual 800×480 emulator capture: `/workspace/output/idash-control-center-media-800x480.png` (fixture metadata and real framework PLAYING state; no audible audio).
- No native code/dependency added; framework APIs used here are available on API27 with guards for newer receiver registration. ARM32 binary/hardware performance not run. No new bitmap/frame rendering path.

## Boundaries

- API27 x86_64 software emulator, not ARM32 Proton head unit. No physical RAM/GPU/thermal/touch-latency certification.
- Real OS MediaSession transport/metadata tested with a separate-UID fixture, not Spotify, YouTube or audible playback. Third-party apps must expose an accessible MediaSession and support the advertised actions. Selection currently prioritizes a playing session at active-session discovery; no user session picker.
- Notification/DND access tests use disposable-emulator shell grants/revocations; physical ROM special-access pages may differ. Camera permission granted/denied with a usable physical flash, camera Activity launch, torch busy and actual illumination remain untested.
- No-flash and missing-utility fallback are checked. Timer and Calculator launch on the emulator; SET_TIMER-only fallback, vendor camera/timer/calculator behavior and QR compatibility are not certified. Intent resolution does not guarantee every external application behaves correctly.
- Notification listener is bound by Android when enabled; iDash adds no persistent polling/foreground media service. Visible Control Center registers callbacks, pause/dismiss unregisters them and invalidates pending camera discovery. Access dialogs are dismissed on pause. Torch is not forced on by resume/permission grant.
