# Pemulihan source iDash Fasa 6D + A1 ke PC

Branch `recovery/fasa-6d-a1-20261103` menyimpan **source working tree sebenar**, bukan pembinaan semula daripada APK. Baseline asal `main`: `6f88d5a3f4f17eef67ced57e3134b42240698e40`. Semua tujuh perubahan tracked dan semua source/dokumentasi untracked yang diperlukan dipelihara. Satu entri untracked APK `dist/iDash-0.1.8-preview.apk` sengaja tidak di-commit; fail asal masih berada di workspace dan backup penuh.

## Kandungan

- Aplikasi utama: source Fasa 2–6C, termasuk Liquid Glass 6A, compact dashboard/Control Center 6B dan kerja QA 6C. `applicationId`/namespace kekal `com.shazwan.idash`, versi source `0.1.8`/code9, minSdk26/targetSdk35. Tiada perubahan logik baharu untuk recovery ini.
- `experiments/phase6d-a1/host-project`: salinan eksperimen berasingan dengan modul host dan cooperative probe, ujian instrumentasi, resources serta konfigurasi asalnya. Namespace, package dan konfigurasi signing eksperimen dikekalkan. Keystore, password, `local.properties`, build output dan AVD tidak disalin.
- `docs/recovery/phase6d-a1`: keputusan PoC, panduan fizikal, panduan Windows dan metadata. Laluan workspace dalam laporan ialah rekod sejarah, bukan fail yang akan wujud pada PC anda. Nama APK dalam metadata bukan APK yang dibundel.
- Lesen/NOTICE dikekalkan. Resources binari baharu dalam salinan A1 ialah salinan aset launcher yang disemak hash, bukan APK pihak ketiga. Aset warisan tidak semuanya berlesen MIT; lihat NOTICE dan dokumen provenance.

## Buka di Windows

1. Clone repository dan pilih branch recovery di direktori PC baharu. Jangan overwrite projek PC sedia ada tanpa backup.
2. Buka root iDash dalam Android Studio untuk launcher; buka `experiments/phase6d-a1/host-project` sebagai projek berasingan untuk makmal A1.
3. Gunakan JDK17, SDK35 dan Gradle8.9. Root mempunyai Gradle wrapper; makmal menggunakan pemasangan Gradle8.9. Tetapkan SDK menggunakan `local.properties` sendiri; contoh disertakan dalam makmal.
4. Debug build tempatan: `./gradlew :app:assembleDebug` (Windows: `gradlew.bat :app:assembleDebug`). Ini ialah arahan untuk PC, **bukan build yang dijalankan semasa recovery**.
5. Keystore stabil tidak diedarkan. Debug APK PC mungkin mempunyai signer lain; jangan uninstall pemasangan lama untuk mengatasi konflik tanpa backup data. Untuk makmal, bina kedua-dua modul dengan signer eksperimen yang sama melalui environment yang dihuraikan dalam `README-A1-MS.md`; jangan gunakan/menggantikan signer stabil.

## Status ujian dan APK

Tiada rebuild atau ujian Android baharu dijalankan untuk pemindahan ini. Semakan recovery mengesahkan hash source, backup, manifest dan senarai commit; laporan build/lint/regresi terdahulu disimpan dalam `docs/reviews` dan laporan A1. Jangan menyamakan semakan recovery dengan ujian fizikal.

APK launcher sedia ada ialah **Fasa6C debug 0.1.8/v9**, bukan build Fasa6D: saiz 6553940 bait, SHA256 `85b613f2c2e9243ed7b56123618e1d0ee24d523a7408406d07de7e4a3efa1a9e`. Signing stabil SHA256 `8d52f749c398e75bfced5fc872008bedb64434bd58d2042afdb91d61377bd2b3`. APK eksperimen host/target ialah `0.1-6d-a1`/code1, pakej `.a1lab.host`/`.a1lab.target`, minSdk26/targetSdk27, signer makmal berasingan. Tiada APK baharu disertakan dalam commit ini.

**A1 belum disahkan berjaya:** emulator stok API27 mencipta display tetapi proxy muncul pada display0. Ujian safety/capability bukan kejayaan hosting. Ujian fizikal TEST9278B, fork BitChord dan eksperimen Stash belum selesai; A2/B tidak ditambah. Jangan menganggap source ini sudah menyediakan BitChord dalam panel.

Cadangan APK: Draft Release berasingan bertajuk “iDash 0.1.8/v9 — Fasa6C debug test”, dan Draft A1 khusus untuk pasangan makmal. Jangan label launcher lama sebagai Fasa6D atau publish automatik. Recovery ini tidak mencipta release APK awam.

## Backup kekal

- Penuh: 255161417 bait; SHA256 `b2c6aae90cdcf8fe9a42159e67dd4a2ff8a1d46293a8fafadd1bf57b713e7d8f`.
- Source-only: 15452751 bait; SHA256 `14052389013a7001dd0c91676492b9b1cb6a20b666f21ed99c00befbfe52947b`.

Kedua-duanya kekal di output workspace; ZIP backup tidak diupload dalam commit ini. Pemulihan source ini tidak mengandungi signing key atau keseluruhan cache/sejarah Git tempatan.
