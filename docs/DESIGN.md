# iDash design brief

## Product

An Android car launcher with iOS-inspired visual hierarchy: rounded icon tiles, a floating dock, spacious widgets and a compact quick-access sheet. Favor readability and fast interaction on a landscape head unit over reproducing a phone home screen.

## First layout

Use a compact status strip, a main widget area and a detached bottom dock. Primary widgets are clock/date, Gregorian calendar and the JAKIM calendar/prayer widget replacing Power. The owner removed dashboard Favorites; Bluetooth, Files and YouTube remain in the dock only. Settings and Apps follow them in the dock. Shortcuts open installed apps; they are not embedded activities.

The app drawer shows actual installed launchable activities with labels and icons. Dock assignments persist locally. System controls must either use available Android APIs or open the matching settings page; do not show unsupported controls as functional.

## Performance

The initial device has1GB RAM and an API27 runtime. Prefer gradients/translucent fills over live blur, small icon caches and short optional animations. Load the app list off the UI thread. Release timers and background work when hidden. Measure actual memory and interaction behavior; no performance numbers have been established yet.

## First milestone acceptance

1. Installs alongside NexaDash with a distinct package and data directory.
2. Registers a HOME Activity but leaves default-home selection to the owner.
3. Fits1024×600 without clipped clock, dock or app labels, with touch targets at least48dp.
4. Launches selected installed apps and handles missing activities gracefully.
5. Saves shortcut assignments across restart.
6. Handles Back, Home, return from external apps and configuration changes without broken navigation.
7. Builds reproducibly and passes API27 emulator smoke checks before physical testing.

## Deferred research

Two simultaneous native app panels, embedded Google Maps, OEM antenna-radio integration, CAN bus/vehicle controls and privileged system toggles require separate evidence and device-specific work. Do not route anything through DiAuto. Do not transplant experimental NexaDash host behavior and describe it as working in iDash.

## Project boundary

NexaDash is paused. Do not modify its repository, signing keys, releases or installed package while developing iDash. Keep raw APK archives, owner reports/photos, account details and signing material out of this repository. Any future reused source must retain its applicable license and attribution.

The0.1.2 visual theme bundles36 portable MRiOS PNG icons and24 wallpapers with separate third-party provenance (NOTICE). The launcher logic remains iDash-native. Do not describe the asset import as the full MRiOS executable engine or all of its widgets/gestures.

0.1.4: Panel, Customize and Control Center are adjacent header actions. The optional panel occupies the right-side blank area and starts hidden. Dock shortcuts are42dp icons within at-least48dp touch tiles. Glass/Transparent/Solid styles and short scoped transitions are iDash rendering. Four source SF Pro files are included; system emoji is retained because the input has no emoji-font payload. Initial panel modes are YouTube web and the isolated iDash TizenTube companion; see OPTIONAL_PANEL.md.
