# Fasa 5A — kesesuaian peranti dan APK ujian

## Keselamatan dan skop

HEAD kekal `6f88d5a`; perubahan semua fasa terdahulu kekal lokal, tanpa commit/push/reset. Checkpoint lengkap Fasa 4B2: `/workspace/checkpoints/idash-phase4b2-complete-20261102T150918Z/`. Arkib dan hash212fail disahkan sebelum kerja. Selepas penambahan harness, satu-satunya fail checkpoint yang berubah ialah manifest **androidTest**. Tiada fail produksi, UI, glass, dock, wallpaper atau logik sistem diubah.

Tambahan fasa ini: `DeviceReadinessChecks.java`, entri instrumentasinya dan laporan ini. Harness mengukur memori, memeriksa cache frost yang sama serta pelepasan callback dalam20 kitaran Control Center. Ia bukan fungsi aplikasi dan tidak terkandung dalam APK host.

## Audit APK

- Package sebenar `com.shazwan.idash`; versionName0.1.8/versionCode9; minSdk26, targetSdk35, compileSdk35. Spesifikasi minimum tidak diubah. API27 termasuk dalam julat sokongan.
- Java Native Views/Canvas. `debugRuntimeClasspath`: **No dependencies**; `mergeDebugNativeLibs`: **NO-SOURCE**. Tiada entri `lib/`, `.so`, APK bersarang atau penapis ABI dalam konfigurasi. DEX boleh dipasang pada ARM32; pemasangan/runtime pada ARM32 fizikal belum disahkan. Emulator yang tersedia ialah x86_64.
- `apksigner verify`: sah, RSA 2048 Android Debug, APK Signature Schemev2 (disokong API27); `zipalign -c -v4`: lulus; integritiZIP: lulus.
- MAIN/HOME/DEFAULT dan MAIN/LAUNCHER pada LauncherActivity exported/singleTask/sensorLandscape; DashNativeProxy exportedfalse. Package dan konfigurasi asal tidak ditukar.
- Tiada uses-permission CAMERA, FLASHLIGHT atau MEDIA_CONTENT_CONTROL. NotificationListenerService dilindungi BIND_NOTIFICATION_LISTENER_SERVICE sebagai izin pengikat servis, bukan permintaan kuasa sistem kepada aplikasi. WRITE_SETTINGS/DND memerlukan akses khas pilihan pengguna; listener perlu disahkan diberikan.
- Laluan API31 Bluetooth/API33 receiver mempunyai guard versi; API27 notification-listener access disemak sebelum digunakan. Lint tiada NewApi/MissingPermission error. Ini bukan bukti semua ROM OEM menyokong semua laluan.
- Amaran PrivateApi **sedia ada** di DashFmController: refleksi `android.os.SystemProperties` bagi diagnostik FM firmware. Tidak diubah dan tiada hidden API baharu diperkenalkan. FM OEM/native companion kekal eksperimen, memerlukan pengujian fizikal.

## Pakej ujian dan keselamatan data

`/workspace/output/phase5a/iDash-0.1.8-v9-api27-test-debug.apk`: **6,548,329 bytes (6.25 MiB)**, standard **debug signing**, bukan release. SHA256APK:
`6eb3c52e264db7e27484145e9e237a0a5172681a679e53c17136a811554ea5c2`.

SHA256sijil debug:
`8d52f749c398e75bfced5fc872008bedb64434bd58d2042afdb91d61377bd2b3`.

Sijil ini berbeza daripada preview terdahulu. Jangan uninstall untuk mengatasi UPDATE_INCOMPATIBLE: data private akan hilang, dan allowBackupfalse. Gunakan peranti ujian kosong atau semak sijil pemasangan sedia ada sebelum update. Tiada keystore pengeluaran digunakan untuk membina/menandatangani APK fasa ini; tiada release APK dihasilkan. APK companion lama yang dilindungi signature mungkin tidak berpasangan dengan host debug ini. Fixture milik projek ditandatangani debug yang sama hanya untuk ujian emulator.

## Pengesahan

Keputusan akhir dan ukuran disertakan dalam laporan output `/workspace/output/phase5a/LAPORAN.md` bersama log audit/regresi dan screenshot aplikasi sebenar. Panduan USB, perlindungan data, pemilihan/pemulihan launcher dan ADB terdapat dalam `PANDUAN-UJIAN.md` di folder sama.

Larian awal emulator fresh-data API27/1024 MiB gagal selepas106 semakan Settings semasa menunggu status JAKIM. Screenshot menunjukkan dialog **System UI isn't responding**; tidak dikira sebagai lulus atau didakwa pembetulan aplikasi. Larian fungsi seterusnya menggunakan emulator1536 MiB; angka RAM/FPS emulator tidak mengesahkan head unit 1GB.

Had: tiada head unit fizikal/ARM32, radio/FM OEM, SmartTube playback sebenar, media pihak ketiga sebenar, cold-boot/sleep-wake ROM kereta atau ujian hidup endpoint JAKIM dalam fasa ini. Media fixture menguji framework MediaSession sebenar, tanpa output audio atau kandungan servis pihak ketiga. Ujian cache/callback bukan bukti ketiadaan semua kebocoran memori.

## Keputusan akhir fungsi dan profil emulator

Build `assembleDebug`, `assembleDebugAndroidTest`, `lintDebug` berjaya; lint **0 ralat/66 amaran**. `testDebugUnitTest` ialah **NO-SOURCE**; jumlah semakan ialah assertion instrumentasi Android. Laporan dependencies menyeluruh mempunyai konfigurasi UTP/managed-device dalaman yang belum dicache offline; runtimeclasspath tetap kosong. Larian menggunakan ADB, bukan Gradle managed devices.

Regresi meliputi Settings/Appearance pada dua resolusi, persistence proses baharu, akses ditolak/diberi, reset/data dikekalkan, dashboard/dock/drawer/Back, wallpaper, glass/LOW/cache, Wi-Fi, Bluetooth unavailable/fallback, brightness manual/auto, AudioManager, MediaSession berasingan UID, Focus/DND dan utiliti. Native panel lulus21 semakan selepas konfigurasi feature fixture dibetulkan. APK/manifest produksi tidak diubah untuk membetulkan persekitaran.

AVD baharu tidak mengiklankan secondary display pada mulanya. XML fixture `tools/native-fixture/secondary-display.xml` dipasang ke image writable **emulator sahaja** menggunakan adbd root/remount, kemudian reboot. Ujian dijalankan selepas adbd kembali ke shellUID2000; iDash/fixture kekal UIDaplikasi biasa. Ini bukan prosedur untuk head unit, bukan akses root dalam APK dan bukan pengesahan firmware TEST9278B. Data/pemasangan pada AVD lama dipelihara.

Harness profiling awal belum muncul sebagai instrumentation: AGP menggantikan nama entri pertama dengan runner lalai. Entri runner baharu dipindah selepas SmokeChecks, didaftarkan semula dan larian akhir **68 semakan lulus**. Semua larian awal yang gagal disimpan berasingan dan tidak dikira sebagai lulus.

Profil API27 x86_64, TCG/SwiftShader, RAMkonfigurasi1536 MiB (jumlah OS1,567,580,160 bytes), BALANCED:

| Ukuran | Bacaan |
|---|---:|
| Dashboard biasa tanpa instrumentasi: PSS | 91.7 MiB |
| Dashboard dengan instrumentasi: PSS / Java digunakan | 100.0 MiB / 2.66 MiB |
| Control Center terbuka: PSS | 114.4 MiB |
| Selepas20 kitaran, tertutup: PSS / Java digunakan | 125.1 MiB / 6.17 MiB |
| Selepas permintaanGC: PSS / Java digunakan | 122.5 MiB / 3.60 MiB |
| Bitmap yang diketahui (wallpaper, frost, ikon aplikasi) | 18 / 2.77 MiB |
| Cache frost tunggal yang sama sepanjang20 kitaran | 88.4 KiB |

Jumlah bitmap di atas bukan inventori semua bitmap framework/driver. Ia bergantung pada wallpaper dan aplikasi dalam profil ujian. SelepasGC, surface hidup turun379→27; senarai weakref masih379 slot sehingga penciptaan surface berikutnya membersihkan entri kosong (cleanup sedia ada apabila >128). PSS tidak kembali ke baseline: tempoh ini tidak cukup untuk menyimpulkan kebocoran atau ketiadaan kebocoran. Semua68assertion/cache/callback yang diperiksa lulus; ujian sesi panjang dan tekanan RAM fizikal masih diperlukan.

`gfxinfo` larian20 kitaran: **83/83 frame jank (100%)**, median500 ms, p90 1100 ms. `am start -W` dashboard biasa timeout selepas11,658 ms; selepas menunggu, Activity direkod resumed dan PSS dapat diukur. Ini keputusan prestasi emulator yang buruk, bukan keputusan lulus prestasi atau bukti FPS/RAM head unit. Overhead TCG/SwiftShader dan instrumentasi turut terlibat; tiada atribusi tunggal kepada peranti atau aplikasi dibuat. Overdraw GPU belum diukur kuantitatif; glass berlapis masih memerlukan pemeriksaan GPU fizikal. Cache/penyediaan frost dan discovery aplikasi kekal di worker; tiada penciptaan bitmap dalam draw.

Dialog ANR **SystemUIService** semasa boot direkod dalam events/screenshot; crashbuffer akhir iDash kosong pada larian diperiksa. Screenshot yang dilindungi dialog ditandakan sebagai diagnostik dan diganti dengan tangkapan semula. Geometry Control Center lulus51+51; kontras sampel3label media pada dua wallpaper minimum4.80:1(1024) dan4.83:1(800), bukan ukuran semua teks.

Panduan pemasangan/pemulihan: `docs/reviews/DEVICE_PHASE5A_GUIDE.md`. Screenshot akhir ialah Activity sebenar pada emulator, bukan simulasi UI Canvas; wallpaper gradient ujian dibina secara asli. Warna/opacity/design produksi kekal sama dengan checkpoint Fasa 4B2.

Jumlah akhir: **1,247 semakan /25 larian lulus** (1,179regresi +68readiness). Larian1024×600 diulang273 semakan selepas dialog emulator ditutup dan keenam-enam screenshot diambil semula. Larian awal/diagnostik tidak ditambah kepada jumlah akhir.
