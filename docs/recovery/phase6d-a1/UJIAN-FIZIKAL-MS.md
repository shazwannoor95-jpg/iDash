# Ujian fizikal cooperative probe — belum BitChord

1. Kekalkan backup dan iDash stabil. Salin `iDash-A1-Cooperative-Probe-debug.apk` dan `iDash-A1-Lab-Host-debug.apk` melalui USB. Pasang target dahulu, kemudian host. Identiti pakej berbeza; jangan uninstall iDash/BitChord rasmi. Jika pakej makmal lama mempunyai signer lain, berhenti dan simpan datanya dahulu.
2. Buka **iDash A1 Lab Host** daripada launcher asal. Ia tidak menawarkan HOME role dan tidak perlu dijadikan launcher lalai. Panel kiri ialah salinan Fasa6C; tiada perubahan pada data iDash stabil. Semak jam, semua waktu solat dan kalendar.
3. Tekan Start di kanan. Catat mesej status. Jika firmware tidak mengiklankan secondary-display, jangan ubah system image. Butang Own-display diagnostic hanya melancarkan proxy UID host; ia tidak membuktikan embedding target.
4. Jika berjaya, UI target sendiri mesti kelihatan pada SurfaceView; catat UID dan **display >0** yang ditunjukkan. Padankan dengan requested/actual display melalui menu Panel startup diagnostics atau log/dumpsys. Paparan target pada display0 atau pilihan Open outside panel tidak dikira A1.
5. Uji counter dengan sentuhan sebenar, Library/Search/Now Playing/Settings probe, field teks, Popup dan Back. Menu ⋯ menyediakan Back dan input text bridge terhad; keyboard sebenar tidak dianggap telah disokong. Uji interaksi popup sendiri dan rekod jika keyboard/dialog sistem muncul di skrin yang salah.
6. Main/pause beep sintetik, ubah volume secara selamat dan semak audio focus apabila aplikasi lain dimainkan. Bunyi ini bukan playback BitChord. Jangan jalankan ujian ketika memandu.
7. Ubah viewport melalui buka/tutup tools/keyboard; semak nisbah dan koordinat touch. Jika menggunakan ADB pada peranti fizikal, jangan tukar resolusi/density sistem tanpa keperluan; dua resolusi makmal bukan spesifikasi baru head unit.
8. Stop/Start sebanyak 20 kali, Close panel, Back, Home, buka semula host dan putar/resume jika disokong. Selepas Stop tiada Activity target pada display lama, tiada input/audio diteruskan dan tiada pertumbuhan RAM berterusan. Jangan anggap proses cache yang masih hidup sebagai bukti leak tanpa ukuran.
9. Jika gagal, keluar host dan gunakan iDash stabil/launcher asal. Jangan cuba root, hidden API, feature injection atau force default launcher.

Arahan ADB pilihan (gunakan serial sebenar; fail log peribadi jangan diterbitkan tanpa semakan):

```sh
adb devices
adb shell getprop ro.build.version.sdk
adb shell getprop ro.product.cpu.abilist
adb shell pm list features
adb shell wm size
adb shell wm density
adb install -r iDash-A1-Cooperative-Probe-debug.apk
adb install -r iDash-A1-Lab-Host-debug.apk
adb shell am start -n com.shazwan.idash.a1lab.host/com.shazwan.idash.a1lab.ProbeHostActivity
adb shell dumpsys display > display.txt
adb shell dumpsys activity activities > activities.txt
adb shell dumpsys meminfo com.shazwan.idash.a1lab.host > host-memory.txt
adb shell dumpsys meminfo com.shazwan.idash.a1lab.target > target-memory.txt
adb shell dumpsys gfxinfo com.shazwan.idash.a1lab.host > frames.txt
adb logcat -d -v threadtime > logcat.txt
adb exec-out screencap -p > panel.png
```

Jangan hantar token, akaun atau data peribadi dalam log. Selepas bukti cross-UID/display/interaksi yang sah barulah eksperimen BitChord v1.9 diteruskan. Tiada Stash atau Smart Music fallback dalam pakej ini.
