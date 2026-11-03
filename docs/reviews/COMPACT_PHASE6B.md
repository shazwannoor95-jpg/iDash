# Fasa 6B — Compact Dashboard & Control Center

Java Native Views, API27, landscape. Semua kerja sebelum Fasa6B dilindungi
di `/workspace/checkpoints/idash-before-phase6b-20261103T021106Z`.

## Skop perubahan

- `LauncherActivity`: padding home lebih kecil; jam, tarikh, greeting dan kad
  kiri dikecilkan. Urutan dan lajur kiri kekal clock → prayers → calendar.
  ScrollView widget diganti dengan kolum berbatas. Insets Home menolak ruang
  yang telah dikhaskan Decor supaya padding bawah/tepi tidak dikira dua kali. Kandungan kanan, dock,
  drawer, logik JAKIM dan kawalan sistem tidak dirombak.
- `HomeWidgets`: tiga profil kepadatan semasa pengukuran, berdasarkan ruang
  sebenar selepas insets. Pengukuran natural dibuat sekali bagi setiap calon
  profil, kemudian tinggi container diselesaikan tanpa pengukuran kedua yang
  boleh mengubah autosize TextView. Teks sebaris dipadankan menggunakan satu TextPaint yang diguna semula;
  saiz akhir sahaja ditetapkan untuk mengelakkan kitaran relayout.
  Saiz minimum teks dan padding dikawal;
  tiada pemprosesan bitmap atau kerja baharu dalam draw().
- `QuickControls`: Airplane, Connections, Focus, Settings, Timer dan Calculator
  dibuang daripada UI ini sahaja. Wi-Fi, Bluetooth, media, kedua-dua kapsul
  slider dan Done kekal. Panel, Wallpapers, Brightness access / Display serta
  mesej izin brightness dinaikkan terus ke bawah baris utama. Semua tindakan
  tersebut kekal disambungkan.
- Kod `ControlUtilities` dikekalkan; listener utiliti tidak dimulakan oleh
  Control Center apabila UI utiliti tiada. Aplikasi utiliti tidak dinyahpasang.

Semua fail enjin kaca/Appearance Fasa6A kekal byte-identical: `GlassSurface`,
`GlassBackdrop`, `IosStyle`, `GlassAppearance`, `AppearanceEditor`. Lalai45%,
julat10–65%, rim/bevel/reflection, cache dan Apply/Cancel/Reset tidak diubah.

## Validasi

`CompactChecks` menggunakan Activity sebenar dan data rasmi JAKIM SGR02 yang
sedia ada untuk tarikh peranti. Ia memeriksa enam waktu, nota, semua kad kiri,
ketiadaan scroll parent, clipping/ellipsis, sasaran48dp, kawalan yang dibuang,
Done, Panel, Wallpapers dan laluan izin brightness. Baseline diambil daripada
APK Fasa6A sebelum perubahan, bukan renderer tiruan.

Resolusi800×480/1024×600, font115%, density200dpi dan status bar diuji melalui
emulator. AVD mempunyai `hw.mainKeys=true`; permintaan menunjukkan navigation
bar tidak menjadikannya bar perisian sebenar. Ruang navigasi48dp diuji berasingan
melalui overscan WindowManager dan dilabel sebagai simulasi inset, bukan bukti
navigation bar fizikal. Status bar ialah rendering sistem sebenar.

Ujian regresi sedia ada dikemas kini untuk ketiadaan enam kawalan; ujian media,
DND dan utiliti backend dikekalkan. Native Panel memerlukan feature secondary
display yang tiada pada AVD ini, maka runner tersebut tidak dilabel lulus.
Keputusan akhir/angka semakan dan profiling ada di
`/workspace/output/phase6b/LAPORAN.md` dan `tests/summary.json`.

Emulator API27 x86_64 TCG/SwiftShader bukan bukti kelancaran ARM32/RAM1GB.
Dialog ANR SystemUI semasa boot ditolak daripada screenshot akhir dan direkodkan
berasingan. Tiada ujian head unit fizikal didakwa selesai.

APK debug ialah output ujian; applicationId, versi dan signing configuration
dikekalkan. Jangan uninstall pemasangan pengguna jika tandatangan berbeza
tanpa melindungi datanya. Tiada commit, push, reset Git atau upload GitHub.

Sesi ini tidak menyediakan alat penerbitan artifact/lampiran Codex Web. Fail
output lokal tidak boleh dianggap pautan muat turun awam atau lampiran tersedia.
