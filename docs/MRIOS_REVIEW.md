# MRiOS V2: static review and asset integration

Input: owner-uploaded `MRiOS V2.apk`,20,543,638 bytes. SHA256: `f11442cb96b7ab0ef2ece5e7241fdb4a38ed148ba841735fdf45530b0db071cb`.

Declared package: `com.mrios.launcher`, version2.0/code999999, minimum SDK32, target SDK37. These are manifest declarations, not evidence that its appearance implements a particular Apple OS release.

The archive provides only ARM64 native libraries: `libandroidx.graphics.path.so`, `libdatastore_shared_counter.so`, `libstub.so`. The owner's API27/ARM32 head unit cannot install/run this input unchanged. Editing minimum SDK alone does not supply missing ARM32 libraries or backport newer Android APIs.

## Components inspected

- One13,431,044-byte DEX with11,319 classes. Main Activity: `com.grice.mobile.LauncherActivity`; Application: `com.pairip.application.Application`.
- Resources include50 layout XMLs, mostly AppCompat/Material and advertising-related layouts. This review did not identify a self-contained home/dashboard XML that can simply replace the iDash screen.
- Named image resources include Files, Settings, Calendar, Maps, Weather and other iOS-style icons. The wallpaper directory contains24 images,47–70.
- Notification badge service is declared. Runtime badge/notification behavior was not tested.
- Vungle, Google advertising/Firebase, billing and Pairip/license-check components are present. Their presence does not establish which behavior is active at runtime, and no security certification is claimed.

## Integrated into iDash0.1.1

| Input resource | iDash use |
| --- | --- |
| `res/drawable/ic_ios_files.png` | Files favorite and dock icon |
| `res/drawable/ic_ios_settings.png` | Settings dock icon |

These are directly reused PNG images, not newly generated approximations. Native Android ImageViews render them without requiring the source app's SDK32/ARM64 runtime. Bluetooth and YouTube marks remain original vector drawing with stronger blue/red gradients; all three favorite labels now stay Bluetooth, Files, YouTube even when assigned to an alternate app.

The images have separate provenance and are excluded from the MIT license in NOTICE. Their original rights remain with their respective owners; a redistribution license was not established from the APK. Do not represent the extracted artwork as original iDash artwork or MIT-licensed material.

## Integration boundary

No MRiOS executable DEX/native library, advertising, billing, notification listener or licensing code was merged into iDash. No source protection or license checks were modified. Full MRiOS widgets, gestures, animation and Control Center were not ported or runtime-tested. Those features need separate source/API work, rather than copying a resource filename or lowering a manifest version.

The original APK remains local and is not included in the repository. This report records static evidence and the two actual asset integrations; it does not claim a completed MRiOS-to-API27 port or an exact iOS27 clone.
