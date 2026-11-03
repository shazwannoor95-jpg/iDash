# Fasa 4B-2 — Settings iDash lengkap (lokal)

## Skop dan keselamatan

Checkpoint lengkap sebelum kod diubah: `/workspace/checkpoints/idash-before-phase4b2-20261102T122803Z/working-tree.tar.gz` — 204 fail tracked/non-ignored untracked, disahkan semula melalui SHA256. Status, HEAD dan patch lokal disimpan; tiada keystore, password, token atau rahsia dimasukkan.

**Isu warna/kepekatan Glass sengaja ditangguh ke fasa akhir.** `GlassSurface`, `GlassBackdrop`, `IosStyle`, `GlassAppearance`, `CapsuleSlider` dan `ControlGlyph` tidak diubah. Tiada pelarasan opacity/tint/palet, reka bentuk dock atau enjin rendering dalam fasa ini.

## Fungsi Settings

- **Settings Home:** lima kategori berikon vektor asli — Appearance, Home & Dock, Waktu Solat, Panel, About. Grid landscape dan sidebar kategori menggunakan material sedia ada; Back kembali ke Settings tanpa menutup launcher.
- **Appearance:** menggunakan editor Fasa 4B-1 yang sama, termasuk Live Preview, Apply/Cancel/Reset, enam kumpulan kawalan dan cache debounce. Tiada editor/kawalan pendua. Percubaan meninggalkan draft memaparkan Simpan/Buang/Kekal, termasuk Back Android dan navigasi melalui dock/header. Cancel sedia ada tetap membuang draft secara eksplisit. Refresh halaman yang sama mengekalkan editor yang sedang diedit.
- **Home & Dock:** kalendar, widget Waktu Solat dan format jam; dua profil dock yang benar-benar disokong: Kompak (ikon34/dock60dp), Comfort (ikon42/dock76dp). Saiz ikon/ruang tidak ditambah sebagai slider bebas kerana renderer sedia ada hanya mempunyai profil berpasangan. Tiga slot Bluetooth/Files/YouTube kekal sebagai aplikasi utama/dock. Picker mempunyai Clear/Refresh dan menyemak Activity masih dipasang, enabled, exported dan boleh diakses sebelum menyimpan. Aplikasi pilihan lama yang hilang dipaparkan dengan fallback pilih semula; pilihan lain tidak dipadam secara senyap.
- **Waktu Solat:** zon, paparan widget/countdown, permintaan refresh, kalendar rasmi dan status liputan data tempatan. Status membaca parser/cache JAKIM sedia ada di worker; sumber dan MYT dinyatakan. Masa cache hanya dipaparkan jika fail cache wujud, bukan timestamp muat turun yang direka. Pengiraan/JakimCalendar dan sumber e-Solat tidak diubah.
- **Panel:** empat mod sedia ada, status signer companion SmartTube, pintasan buka jika dipasang, bantuan Start, paparan Panel serta laporan FM tersimpan. Membuka Settings atau memilih mod tidak memulakan pemain/tuner.
- **About:** nama/versi/build sebenar, minimum API dan API/ABI peranti, sasaran API27/ARM32, bantuan, tetapan Android/Home dan penjelasan akses brightness/media/DND. Halaman rasmi dibuka selepas tindakan pengguna; tiada akses dianggap telah diberi atau DND diubah.
- **Reset Settings:** dialog menyenaraikan preferences/assignment yang direset dan imej peribadi, cache JAKIM, FM serta data companion yang dikekalkan. Cancel tidak memadam pilihan. Reset positif menggunakan mekanisme preferences sedia ada dengan pengesahan.

Kategori selain Appearance menyimpan selepas tindakan pengguna. Appearance terus menggunakan draft/Apply. Sesetengah kandungan boleh discroll secara native apabila dock Comfort mengecilkan ruang pada fallback; sasaran sentuhan kekal sekurang-kurangnya48dp.

## Perlindungan kod

Hash QuickControls, ControlMedia, ControlUtilities, MediaAccessService dan JakimCalendar sama dengan checkpoint. Implementasi dashboard/dock/app drawer dan pengiraan solat tidak diubah. Launcher hanya mendapat pemilik/lifecycle Settings serta guard navigasi Appearance sebelum render; tindakan dock/header biasa menggunakan implementasi asal.

Camera/Flashlight tidak ditambah semula. Tiada permission/dependency/database/root/hidden API baharu. Kualiti Glass/warna tidak ditala.

## Fail

- `DashSettings.java`: kategori Home, sidebar, halaman tetapan, status data serta cleanup dialog/callback.
- Baharu `SettingsActions.java`, `SettingsNavigation.java`, `SettingsGlyph.java`.
- `LauncherActivity.java`: lifecycle Settings dan semakan guard sebelum render.
- `AppearanceEditor.java`: Back melalui guard; satu flag memelihara keadaan Panel asal semasa membatalkan navigasi. Logik preview/Apply/Cancel/Reset serta rendering tidak ditukar.
- Ujian baharu `SettingsCompleteChecks`, `SettingsPersistenceChecks`, `SettingsAccessChecks`, `SettingsResetChecks`; entri androidTest manifest.

## Pengesahan

Build debug/lint/instrumentation berjaya; **0 ralat / 66 amaran**. Tiada error NewApi/MissingPermission. **1,179 semakan lulus dalam 24 run akhir**, termasuk konfigurasi berulang pada dua resolusi. Semua ujian regresi sedia ada dijalankan.

| Kumpulan | Lulus |
|---|---:|
| Settings lengkap / geometri / navigasi 1024×600 & 800×480 | 273 + 273 |
| Persistence Settings (proses berbeza) | 3 + 7 |
| Permission ditolak / pautan rasmi / fallback | 7 |
| Reset positif & data yang dikekalkan | 8 |
| Regresi Fasa 4B-1 dan terdahulu (UI, panel, wallpaper, glass, Appearance, sistem/media, layout/visual) | 608 |

Semakan kategori meliputi sasaran sentuhan dan clipping semua kawalan interaktif, data JAKIM offline sebenar, pilihan aplikasi hilang/tidak lagi boleh diakses, Save/Discard/Stay untuk draft, Back dan Cancel. Persistence memeriksa PID proses baharu selepas force-stop. Reset positif mengesahkan imej/cache dan preferences FM dikekalkan byte-for-byte/nilai asal. Tiada angka prestasi fizikal disimpulkan.

Harness akses Android awal menunggu `am start -W` selepas kembali ke launcher; run itu ditamatkan dan tidak dikira lulus. Harness akhir menunggu lifecycle/resume tanpa menunggu idle aplikasi latar. Tiada kod aplikasi diubah untuk pembetulan harness ini.

Screenshot aplikasi sebenar: `/workspace/output/phase4b2/` — Settings Home dan kelima-lima halaman kategori pada1024×600 serta800×480. EmulatorAPI27 x86_64 @160dpi; ini bukan simulasi Canvas atau pengesahan head unit fizikal.

Ujian navigasi awal diselaraskan supaya menunggu mesej callback/dismiss butang AlertDialog Android sebelum tindakan seterusnya; run awal yang gagal tidak dikira sebagai lulus. Screenshot akhir menunggu toast dan pembentangan UI selesai.

## Batasan

Prestasi/latensi/RAM pada head unit ARM32/1GB belum diuji. Resolusi utama1024×600@160dpi berdasarkan rekod TEST9278B/alps;800×480 ialah fallback. Font scale/DPI di luar konfigurasi ini belum disahkan. Sambungan hidup JAKIM, servis FM OEM serta playback SmartTube fizikal masih belum disahkan dalam fasa ini; Settings menggunakan mekanisme sedia ada dan tidak menganggap fungsi perkakasan tersedia.

Tiada commit, push atau release APK dibuat. Isu Glass terlalu pekat/solid kekal dalam backlog fasa akhir, seperti arahan pemilik.
