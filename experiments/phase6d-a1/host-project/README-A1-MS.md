# Makmal cooperative A1 API27

Salinan eksperimen berasingan iDash Fasa6C, bukan update iDash stabil atau BitChord. Lihat LAPORAN-MS.md dalam pakej output bagi keputusan sebenar: stock emulator tidak menyokong pelancaran Activity pada secondary display, jadi embedding belum disahkan.

Modul app ialah host; probe-target ialah sasaran UI sendiri. DashNativeFrame/Proxy/Geometry berasal daripada NexaDash di bawah MIT; lihat NOTICE dan docs/licenses/NexaDash-MIT.txt. Tiada fork BitChord/GPL code baru dibundel. UI, font dan artwork warisan mempunyai hak berasingan yang dihuraikan dalam NOTICE. Jangan menganggap semuanya MIT.

Bina dengan JDK17, Gradle8.9, Android SDK35. Buat local.properties dengan sdk.dir setempat. Tiada database/dependency baru. TargetSdk27 ialah konfigurasi makmal untuk ujian Android8.1; bukan cadangan penerbitan Play Store.

Signing input environment: A1LAB_KEYSTORE (path ke keystore eksperimen sendiri), A1LAB_STORE_PASSWORD, alias a1lab. Gunakan sijil eksperimen yang sama untuk kedua-dua modul; jangan gunakan atau menggantikan kunci stabil. Kunci/password asal makmal tidak diedarkan. Membina dengan sijil lain menghasilkan APK yang tidak boleh mengemas kini pemasangan makmal sedia ada tanpa tindakan migrasi pengguna.

```sh
gradle :app:assembleDebug :probe-target:assembleDebug :app:lintDebug :probe-target:lintDebug
gradle :app:assembleDebugAndroidTest :probe-target:assembleDebugAndroidTest
```

Instrumentasi host `com.shazwan.idash.a1lab.host.test/com.shazwan.idash.a1lab.A1Checks`; target `com.shazwan.idash.a1lab.target.test/com.shazwan.idash.a1lab.target.TargetChecks`. Pasang APK instrumentasi pada emulator/peranti ujian sahaja. Pada firmware yang menyokong secondary display, cabang ujian host hanya memeriksa permulaan rendering; keseluruhan touch/IME/dialog/performance masih memerlukan ujian fizikal berasingan. Jangan ubah imej sistem untuk memaksa PASS.
