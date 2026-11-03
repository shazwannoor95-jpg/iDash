# Phase3B-1 system controls — local only

Scope changes: QuickControls.java, normal-permission declarations in AndroidManifest.xml, and Control Center lifecycle hooks in LauncherActivity.java. Tests: new SystemControlsChecks.java and accessible action-name updates in ControlLayoutChecks.java. No glass/layout/dock/widget/drawer modifications, no commit/push, no release artifact update. Earlier uncommitted phases remain in the workspace.

Wi-Fi: actual state via WifiManager. Public setWifiEnabled requests on API26–28 on the existing worker executor; API29+ conservatively opens Wi-Fi Settings. CHANGE_WIFI_STATE is declared only through API28. Missing/denied/unsupported or rejected requests route to Settings; transient states are displayed and ignore repeated toggle requests. No SSID/location collection.

Bluetooth: reads adapter state, explicitly distinguishing missing adapter/permission/transition from Off. Public enable/disable only below API33, with existing normal legacy permissions through API30 or BLUETOOTH_CONNECT on API31/32. BLUETOOTH_ADMIN added only through API30. API33+, no adapter, missing permission or rejected request uses Bluetooth Settings; the existing launcher helper catches unavailable/denied Settings activities and falls back to generic Settings. Modern platform permission branches were inspected/policy-tested, not executed on modern devices. Android adapter status is not OEM hands-free status.

Airplane Mode: read-only Settings.Global flag plus existing Android Settings shortcut; no Global writes, root or hidden APIs. Brightness: preserves explicit special-access page, canWrite checks, manual-mode-only writes and disabled slider for Automatic/unknown mode, unknown value or denied access. Unknown brightness is not invented as50%. No silent mode changes. OEM physical backlight behavior remains unverified.

Volume: reads AudioManager max/current; respects fixed-volume devices. Pending drag values are coalesced to one write per120ms, deduplicated against current stream volume; release flushes the final value. Pause/close cancels queued writes. Source rate bound excludes the explicit final flush. The burst test submits50 requests and verifies <=2 writes plus the final AudioManager value.

Lifecycle: visible Control Center registers official Wi-Fi/Bluetooth/Airplane broadcasts and a Settings.System ContentObserver. Settings callbacks are coalesced160ms. Pause/stop/dismiss unregister and remove callbacks; resume refreshes and registers idempotently. A2-second fallback reads only music volume while the dialog is active, covering hardware changes with no public SDK volume-state broadcast. The old full-status clock-ticker polling was removed. Radio requests run off the UI thread and do not navigate after their original dialog is closed. No new Android signature/system permissions; existing app-owned companion permission is unchanged.

Final validation on exact signed API27 x86_64 emulator build:
- assembleDebug, assembleDebugAndroidTest, lintDebug PASS (0 errors/49 warnings).
-17 system integration assertions PASS: actual Wi-Fi toggle and restoration, SDK policy, real labels/airplane flag, burst-volume bound, pause/callback cleanup, missing-Bluetooth Settings fallback and explicit foreground return/resubscription.
-27 existing Settings/control assertions PASS: actual volume and brightness drag, special-access denied/allowed, safe Automatic mode, existing preferences and shortcuts. Temporary emulator app-op, brightness/mode, volume and Wi-Fi are restored.
-36 Control Center geometry assertions PASS at800x480. Screenshot: /workspace/output/idash-control-center-3b1-800x480.png.

Earlier lifecycle attempts assumed one Back or an immediate REORDER_TO_FRONT launch completed the Settings transition; they failed to observe a resumed Activity and are not passing results. Final test explicitly foregrounds the launcher using the test-only public ADB am start -W command before checking actual onResume observation. This is not root and is not included in production app logic. Back/task-stack behavior across vendor Settings implementations remains a physical-test item.

Not verified: actual Bluetooth On/Off on a real adapter (emulator unavailable), real API29+/31+/33+ device fallbacks, Wi-Fi permission denial on vendor builds, OEM FM volume/backlight, all physical knob events, head-unit ARM32/1GB FPS/RAM/thermal behavior. Java/Canvas additions use APIs guarded for API27; no native ABI dependency added. Media/Focus/other utilities remain the phase3A disabled previews.
