# iDash

A lightweight, iOS-inspired Android launcher for car head units.

Initial target: Android API27, ARM32, landscape1024×600 and1GB RAM. This is a new project, independent of NexaDash. Current status: project brief and repository initialized; no application or installable APK yet.

## First milestone

- Home screen with a floating dock, readable clock and rounded widgets.
- App drawer listing installed launchable Android apps.
- Configurable shortcuts for navigation, music, antenna radio and settings.
- A Control Center-inspired quick-access sheet using supported Android settings links.
- Large touch targets, bounded animation and a low-effects mode for older hardware.

Native Android apps initially open through their normal launch activities. Two fully interactive native app panels are a later research milestone, not a promised capability of the first release. Changing the visual style does not remove Android hosting restrictions.

## Development direction

Use native Android views and Java for a small initial runtime footprint. Start with minSdk26, targetSdk35 and a separate application ID, `com.shazwan.idash`. Validate API27 behavior and1024×600 layout before claiming head-unit compatibility.

See [design brief](docs/DESIGN.md) for scope and acceptance criteria. No Apple icons, fonts or proprietary assets are bundled; the visual design will be original.
