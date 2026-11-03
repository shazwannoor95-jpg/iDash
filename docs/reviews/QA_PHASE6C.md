# iDash Fasa 6C — QA & prestasi

APK debug akhir: **0.1.8 (code 9)**, `com.shazwan.idash`, 6,553,940bytes. `assembleDebug`, `assembleDebugAndroidTest` dan `lintDebug` berjaya; lint **0 ralat/58 amaran** (baseline 66). Sijil debug 6B dikekalkan, v2 signature dan zipalign disahkan. SHA256: `85b613f2c2e9243ed7b56123618e1d0ee24d523a7408406d07de7e4a3efa1a9e`.

## Audit & pembetulan

- `LauncherActivity.java`: cache 3 formatter/Date milik Activity, kemas kini label apabila nilainya berubah, satu timestamp konsisten untuk jam/JAKIM. Tarikh kad Calendar kini dikemas kini selepas tengah malam tanpa perlu render semula halaman.
- `LauncherActivity.java` dan `HomeWidgets.java`: pembetulan clipping kalendar yang ditemui pada 800×480/200dpi/font 115%. Fallback menegak membuang padding/gap tambahan sahaja; skala teks minimum, turutan jam→solat→kalendar, semua maklumat, dock, bahagian kanan dan Control Center dikekalkan. Konfigurasi biasa mengekalkan jarak asal.
- Ujian `MediaUtilitiesChecks.java`: baca DND selepas penantian terhad di luar UI thread. Log sistem membuktikan Priority diterapkan 245ms selepas permintaan asal; assertion segera terdahulu gagal. Kod DND/media aplikasi tidak diubah.
- Tambahan instrumentation: `QualityChecks.java`, `PerformanceAuditChecks.java`, `StabilityChecks.java` dan pendaftaran test manifest. Tiada dependency baharu atau servis aplikasi baharu.

GlassSurface, GlassBackdrop, IosStyle, GlassAppearance dan AppearanceEditor tidak berubah berbanding checkpoint 6B. Transparency 10–65/default 45, Light/Dark/Adaptive, BALANCED, rim/bevel/refleksi dan cached wallpaper frost dikekalkan; bukan live backdrop blur. ApplicationId, signing, manifest aplikasi, Wi-Fi/Bluetooth/brightness/volume, MediaSession, Notification Listener, DND dan utiliti tidak diubah.

## Keputusan akhir

| Kumpulan | Keputusan | Run | Semakan |
|---|---|---:|---:|
| Regresi sedia ada | PASS | 25 | 1374 |
| Matriks responsif | PASS | 11 | 1177 |
| Glass 3 palet×3 material×2 resolusi | PASS | 2 | 56 |
| Jam/lifecycle,profil prestasi, stress 8 kitaran | PASS | 3 | 164 |
| Jumlah APK akhir | PASS | 41 | 2771 |

**FAIL fungsi/geometri berbaki: 0.** Dua kegagalan awal direkod berasingan: timing assertion DND dan clipping density/font, kedua-duanya diuji semula selepas pembetulan. Sejarah kandidat terdahulu tidak dicampurkan dalam jumlah APK akhir. Lihat `tests/summary.json` dan `tests/investigation/`.

Yang diperiksa: cache rasmi 4 zon JAKIM/offline/cache rosak, pilihan zon, timestamp MYT tengah malam, 12/24 jam/locale/zon masa, tarikh 2027 tanpa jadual, lifecycle/recreation, Settings semua kategori, Apply/Cancel/Reset/persistence, Wi-Fi emulasi dan Bluetooth-unavailable fallback, permission brightness/volume drag, MediaSession separateUID fixture dan mask tindakan, 20 kitaran CC, 8 kitaran wallpaper/Appearance serta LOW/BALANCED/HIGH. GPS auto-zon **tidak diimplementasi**; pilihan zon manual dikekalkan. Rollover menggunakan timestamp terkawal, bukan menukar jam OS atau ujian semalaman sebenar.

Matriks UI: 800×480 dan 1024×600, 160/200dpi, font 100/115%, status bar sebenar dan reserved 48px kanan/bawah melalui overscan. AVD menggunakan hardware navigation keys; navigation bar OEM sebenar **BELUM DIUJI**. Kad kiri penuh tanpa scroll, kawalan yang dikekalkan serta target 48dp diperiksa. Airplane/Connections/Focus/Settings/Timer/Calculator/Camera/Flashlight tidak muncul semula dalam CC. Sembilan kombinasi palet/material per resolusi memberi sampel kontras minimum teks **4.80:1**, glyph **3.03:1**; bukan pensijilan semua wallpaper/keadaan disabled.

## Prestasi & isu terbuka

Median CPU UI 60 panggilan jam/minit sama: **1198.26→5.66ms**. Ini loop sintetik pada emulator TCG, bukan CPU keseluruhan atau peningkatan FPS ARM32. Jadual PSS/heap/GC, idle redraw, latensi CC/Appearance dan log percubaan startup `am start -W` tersedia dalam `PRESTASI.md` serta JSON mentah. Tiada dakwaan penjimatan RAM umum atau semua UI lebih pantas.

Gfxinfo agregat pertama: **41/41 frame janky**, p50 200ms, p90=700ms, p99=2050ms pada TCG/SwiftShader. Frame-budget emulator tidak memenuhi 16.7ms; ini amaran diagnostik dan **bukan PASS prestasi fizikal**. Stress wallpaper menaikkan PSS; pemerhatian rujukan lemah/GC yang pendek belum menolak kebocoran native atau jangka panjang. Tiada blur dinamik/bitmap baharu dalam draw yang ditambah; cache identity diuji.

**BELUM DIUJI:** TEST9278B ARM32/RAM 1GB sebenar, OEM Bluetooth/FM/MCU backlight/audio/standby, native Panel secondary-display, Spotify/YouTube sebenar, refresh HTTP JAKIM langsung, TalkBack manual dan rollover semalaman fizikal. API27 x86_64 diuji; minSdk 26/target35 dan DEX-only tanpa native `.so` menyokong pembungkusan ARM32, bukan pengesahan runtime ARM32. Adapter FM PrivateApi sedia ada kekal risiko; lihat `LINT.md`.

Ukuran startup biasa belum disahkan: tiga percubaan am start -W dengan kategori LAUNCHER pada emulator tamat masa sekitar10–11s; WaitTime bukan masa dashboard. Activity kelihatan kemudian. Ini kekal isu pengukuran/performance yang memerlukan pengesahan head unit.

## Bukti & keselamatan

Laporan penuh, checksum APK, galeri offline dan keputusan per-run tersedia dalam output Fasa6C. Checkpoint source akhir mempunyai manifest SHA256 yang diperiksa semula, bersama status Git dan patch tanpa key/credential. Rujuk checkpoints.json dalam pakej QA untuk lokasi arkib. Semak LINT.md dan CHECKLIST-FASA7A.md sebelum menilai kelayakan head unit fizikal. Source ini tidak di-commit atau push.
