# iDash Fasa 5A — pemasangan dan ujian head unit

Sasaran rekod projek: TEST9278B/alps, Android 8.1/API27, ARM32, RAM sekitar 1GB, 1024×600 @160dpi. Sahkan di peranti; ujian emulator bukan pengesahan spesifikasi fizikal.

## Sebelum memasang: lindungi data

APK ini **debug**, package `com.shazwan.idash`, versi 0.1.8 (9), sijil SHA256:
`8d52f749c398e75bfced5fc872008bedb64434bd58d2042afdb91d61377bd2b3`.

Sijil ini **berbeza** daripada preview lama yang diterbitkan (`49f6adc61ce465f494ab7d86bf8adcc196b03189ab81f9e287d91328cd63de73`). Jika preview itu sudah dipasang, Android akan menolak update ini. **Berhenti jika tandatangan tidak sepadan; jangan terus uninstall.** Uninstall memadam preferences, zon, favorites dan wallpaper peribadi dalam storan aplikasi. `allowBackup=false`; jangan menganggap Android/ADB backup tersedia. Simpan APK asal, catat tetapan dan pilihan aplikasi, serta simpan fail wallpaper asal dahulu. Laluan paling selamat untuk debug ini ialah peranti ujian tanpa pemasangan lama. Update tanpa kehilangan data pada pemasangan lama memerlukan APK dengan sijil yang sepadan; pakej tersebut tidak dihasilkan dalam fasa ini.

Jika ADB tersedia, semak APK terpasang dahulu:

```sh
adb shell pm path com.shazwan.idash
adb pull <laluan-base.apk-yang-dipaparkan> idash-installed.apk
apksigner verify --print-certs idash-installed.apk
adb shell getprop ro.build.version.sdk
adb shell getprop ro.product.cpu.abilist
adb shell wm size
adb shell wm density
```

Jangan tukar resolusi head unit berdasarkan resolusi emulator. Jangan padam data aplikasi atau launcher OEM.

## Pasang melalui USB

1. Parkir kereta. Salin `iDash-0.1.8-v9-api27-test-debug.apk` ke USB dan buka dalam pengurus fail head unit.
2. Jika belum pernah memasang iDash, benarkan pemasangan daripada pengurus fail tersebut melalui halaman rasmi Android jika diminta, kemudian pasang. Jika iDash sudah ada, teruskan sebagai update **hanya selepas sijil disahkan sama**. Hentikan jika terdapat ralat pakej/tandatangan.
3. Buka iDash daripada senarai aplikasi dahulu. Jangan jadikan launcher lalai sehingga ujian asas selesai.
4. Jika mahu, pilih iDash melalui Settings Android → Apps → Default apps/Home app. Kekalkan launcher asal terpasang dan ingat namanya.

ADB pilihan, hanya selepas semakan sijil: `adb install -r iDash-0.1.8-v9-api27-test-debug.apk`. `-r` tidak mengatasi ketidakpadanan sijil. Jangan gunakan uninstall/clear-data sebagai pembetulan automatik. Jika Android menolak versi lebih rendah, jangan paksa downgrade.

## Senarai semak fizikal

- Dashboard/dock/App Drawer: navigasi, ikon aplikasi, favorites dan scroll; tiada clipping pada 1024×600. Back dan Home kembali dengan betul.
- Control Center: buka/tutup berulang, drag slider perlahan/pantas; uji sentuhan dan butang volume fizikal.
- Wi-Fi/Bluetooth: bandingkan status dengan Android Settings; uji kawalan/fallback dan kembali ke iDash. Firmware mungkin menggunakan Bluetooth/vendor MCU tersendiri.
- Brightness: tanpa izin, buka Modify system settings; beri izin hanya jika mahu. Uji manual/auto, slider dan penyegerakan selepas kembali. Sesetengah head unit menggunakan kawalan backlight OEM berasingan.
- Media: tanpa Notification Listener Access, mesej akses diperlukan mesti jelas. Jika diberi akses, uji pemain yang menyediakan MediaSession (tajuk, artis, play/pause/next/previous). Akses ini luas; beri hanya jika selesa. Tiada jaminan semua aplikasi YouTube/Spotify menyediakan sesi yang boleh dikawal.
- Focus: tanpa Notification Policy Access tidak boleh mengubah DND. Jika diberi, uji perubahan secara sengaja dan pulihkan mod asal.
- Settings/Appearance: navigasi setiap kategori; uji Apply/Cancel/Reset Appearance dan pemulihan selepas restart. Reset Settings menyeluruh hanya jika sengaja mahu memadam tetapan yang dinyatakan.
- Waktu Solat: pilih zon JAKIM betul, semak tarikh/masa Malaysia dan status data. Uji refresh rasmi apabila bersambung; tiada masa rekaan jika data tiada.
- Timer/Calculator: buka aplikasi yang tersedia; keadaan tiada aplikasi mesti dikendalikan. Camera/Flashlight kekal tiada dalam Control Center.
- Uji sleep/wake, cold start dan perjalanan penggunaan panjang ketika kenderaan berhenti. Catat crash, freeze, panas atau aplikasi ditutup oleh sistem.

Companion SmartTube lama mungkin menggunakan permission signature yang memerlukan sijil host sepadan. Fixture emulator yang ditandatangani debug tidak mengesahkan companion sebenar atau radio FM pada firmware Proton.

## Kembali ke launcher asal

Buka Android Settings → Apps → Default apps/Home app, pilih launcher asal dan tekan Home. Jika iDash bermasalah, gunakan ikon Settings/notification panel Android untuk membuka tetapan. Jangan uninstall iDash untuk menukar launcher; itu berisiko memadam datanya.

Jika ADB tersedia dan UI bermasalah, buka halaman rasmi pemilihan Home dengan `adb shell am start -a android.settings.HOME_SETTINGS`; jika ROM tidak menyediakannya, cuba `adb shell am start -a android.settings.SETTINGS`. Pilih launcher asal sendiri.

## Log diagnostik pilihan

Dengan USB debugging yang dibenarkan oleh pemilik, jalankan pada komputer:

```sh
adb logcat -v threadtime -b main -b system -b crash > idash-headunit-logcat.txt
```

Ulang masalah kemudian Ctrl+C. Jangan kosongkan log sebelum menyimpan bukti masalah.

```sh
adb shell dumpsys meminfo com.shazwan.idash > idash-meminfo.txt
adb shell dumpsys gfxinfo com.shazwan.idash framestats > idash-gfxinfo.txt
```

Catat tindakan, masa, permission dan aplikasi media yang digunakan. Semak log untuk maklumat peribadi sebelum berkongsi; jangan muat naik log mentah atau rahsia ke GitHub.
