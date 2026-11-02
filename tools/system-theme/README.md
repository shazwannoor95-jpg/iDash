# iDash system palette · experimental no-root route

This is a pair of **dynamic, resource-only RRO APKs**, targeting the owner's reviewed API27 MediaTek Settings and SystemUI packages. It does not modify the firmware's existing static overlay. The original static RRO is evidence that the firmware uses resource overlays, not permission to replace it or proof that downloaded overlays will be accepted.

Scope: iOS-inspired blue accents, Settings light/dark surfaces, dark quick-settings/shade surfaces and pale notification surfaces. No layouts, fonts, gestures, native libraries, Java/DEX code, Settings functionality or SystemUI logic are replaced. It is not a full iOS conversion. Other apps remain app-owned. Some colors may be unused by particular screens, inherited framework colors may remain, and actual readability must be checked on the target unit.

## Observed result

On the disposable API27 emulator, ADB UID2000(shell) installed both packages, but neither appeared in Overlay Manager. Both enable attempts exited1. No palette was activated, so no applied-theme screenshot or visual-success claim is provided. Both candidates were removed afterwards. The owner firmware has not been tested; vendor policy can differ. Treat this as a research candidate, not a proven no-root theme installer.

## Before installing

Requires a computer with working Android Platform Tools/ADB and USB debugging authorized by the owner. No `su`, `adb root`, Magisk, Shizuku, firmware flash or system-partition writes are used. **No-root still requires ADB; installing these APKs alone is not activation.** Do not remove OEM Theme/SystemUI/Settings or disable existing overlays.

Keep the ADB connection available for rollback. The screen/shade may refresh when a SystemUI overlay changes. Target hashes in artifact.json identify the exact APK exports used to build this candidate. A different build needs a separate review.

Run the included read-only preflight with Python3:

```sh
python preflight.py
```

For multiple connected devices add `--serial YOUR_DEVICE`. Do not post device serials publicly. Preflight checks API27, exact target APK hashes and access to `cmd overlay list`; it does not install or change anything. If `sha256sum` or overlay commands are unavailable, permission is denied, or hashes differ, stop and retain the output locally for review. Do not try to bypass firmware policy. A preflight pass does not establish activation permission.

## Install and activate separately

From the extracted ZIP folder, with exactly the intended unit connected:

```sh
adb install -r iDash-settings-overlay.apk
adb install -r iDash-systemui-overlay.apk
adb shell cmd overlay list
adb shell cmd overlay enable --user 0 com.shazwan.idash.overlay.settings
adb shell cmd overlay list
```

Continue only if Settings overlay is shown `[x]` and Settings opens with readable text and controls. Otherwise use rollback below. Then, separately:

```sh
adb shell cmd overlay enable --user 0 com.shazwan.idash.overlay.systemui
adb shell cmd overlay list
```

Verify `[x]`, then inspect notification text, quick-settings icons and the expanded shade. Command success alone is not visual verification. A missing entry, `[---]`, disabled entry or permission/idmap error is not success. No reboot should be used to try to force a failed activation. The package has no launcher icon; that is expected for resource-only overlays.

## Roll back

These commands target only the two iDash overlays:

```sh
adb shell cmd overlay disable --user 0 com.shazwan.idash.overlay.systemui
adb shell cmd overlay disable --user 0 com.shazwan.idash.overlay.settings
adb uninstall com.shazwan.idash.overlay.systemui
adb uninstall com.shazwan.idash.overlay.settings
```

If one command reports an absent package, continue the remaining rollback commands. Do not disable/remove `android.auto_generated_rro__` or any stock overlay. This restores the baseline that existed before these candidates; it does not clear app data. Activation persistence after reboot and firmware updates is not certified.

## Build and evidence

Full source: tools/system-theme in https://github.com/shazwannoor95-jpg/iDash. Use JDK17, SDK35/build-tools36 and your own key (alias idash): set ANDROID_HOME, IDASH_KEYSTORE, IDASH_STORE_PASSWORD privately, then `bash tools/system-theme/build.sh`. The verification script additionally consumes the locally reviewed target APKs; these owner APKs are not redistributed. Rebuild with a different key cannot update the published candidates in place.

The build verifies signatures/alignment and exact color-name existence against target exports. See artifact.json for source target hashes and the recorded emulator activation/rollback result. API27 emulator results do not establish acceptance on the owner's vendor firmware. No physical-head-unit activation, full visual audit, notification privacy/content or vehicle hardware test is certified here.
