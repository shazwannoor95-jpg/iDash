# Fasa 6D-A1 — keputusan eksperimen

Status: APK PoC host dan sasaran cooperative berjaya dibina. **Embedding A1 belum berjaya disahkan.** Ini bukan prototaip BitChord yang sudah berfungsi.

## Keputusan sebenar

- Emulator baru: Android 8.1 API27, x86_64, RAM konfigurasi 1GB, software emulation tanpa KVM. Kedua-dua resolusi 800×480 dan 1024×600 diuji pada 160dpi.
- Imej SDK stok tidak mengiklankan `android.software.activities_on_secondary_displays`. Tiada feature disuntik, root, remount, writable-system atau ubah firmware. AVD lama yang mempunyai XML feature tambahan tidak digunakan untuk keputusan lulus.
- Private VirtualDisplay berjaya dicipta. Diagnostik melancarkan proxy milik host dengan API awam `setLaunchDisplayId`; display diminta 2/3 dalam ujian awal tetapi Activity sebenar berada pada **display 0**. Log sistem: `getValidLaunchStackOnDisplay: can't launch on displayId`. ID boleh berubah selepas ulangan.
- Host menghentikan proses dan membersihkan resource apabila display tidak sepadan. Sasaran UID lain tidak dilancarkan secara palsu pada display utama sebagai bukti A1.
- Ini membuktikan halangan konfigurasi/platform emulator ini. Ia **tidak** membuktikan semua peranti API27 mustahil menyokong A1. Ujian pada TEST9278B masih munasabah tanpa mengubah firmware.
- Fork BitChord bersyarat belum dimulakan. Tiada APK rasmi BitChord atau Stash diubah, ditandatangani semula, dipasang atau dibundel. A2/B tidak dilaksanakan. Eksperimen Stash belum bersedia.

## Implementasi berasingan

Lokasi: `/workspace/idash-experiments/phase6d-a1/host-project`.

Fail utama baharu/diubah dalam salinan eksperimen sahaja:

- `app/build.gradle`, `settings.gradle` dan manifest: identiti/signing eksperimen, launcher biasa tanpa HOME role; Activity proxy opt-in.
- `ProbeHostActivity.java`: subclass dashboard Fasa6C, hanya mengganti kandungan panel kanan dengan PoC sendiri. Tetapan awal makmal hanya dalam preferences pakej makmal.
- `DashNativeFrame.java`: sasaran sendiri sahaja; signature gate, pemeriksaan manifest `allowEmbedded` melalui public XML API, handshake protokol, pengesahan display sebenar, diagnostik UID sendiri dan cleanup.
- `DashNativeProxy.java`: proxy host awam untuk UID-presence pada private display, bukan bypass opt-in.
- `DashFmController.java`: integrasi vendor/reflection tidak digunakan dalam makmal; laluan resolver diganti dengan status unavailable. Sumber asal tidak disentuh.
- Modul `probe-target`: `ProbeActivity`, `Bridge`, manifest dan Gradle. UI cooperative kecil sendiri, bukan UI tiruan BitChord. Bridge memeriksa signature, UID dan pakej host setiap transaksi; signature permission; input terhad kepada Activity pada display yang disahkan >0. Touch queue dibatasi dan event direcycle.
- `A1Checks.java`, `TargetChecks.java`: instrumentasi keselamatan, capability, lifecycle dan sasaran standalone.

Host `com.shazwan.idash.a1lab.host`, sasaran `com.shazwan.idash.a1lab.target`; namespace Activity host `com.shazwan.idash.a1lab`. Kedua-duanya minSdk26, targetSdk27, versi `0.1-6d-a1`/code1. Target27 dipilih untuk makmal API27; hanya amaran lint polisi Play `ExpiredTargetSdkVersion` dinyahaktifkan. Bukan keluaran Play/release.

Kedua-dua APK DEX-only, tiada library native atau dependency baharu. Pembungkusan tidak mengecualikan ARM32, tetapi runtime ARM32 belum diuji. Kedua-duanya menggunakan sijil makmal yang sama, berbeza daripada sijil iDash stabil; SHA256 sijil dalam `APK-METADATA.json`. Kunci dan password tidak disertakan.

## Pengesahan

- `assembleDebug` kedua-dua modul dan APK instrumentasi: berjaya; `build-verified.log`.
- Lint: host 57 amaran, sasaran 4 amaran, **0 ralat**. Antaranya hardcoded text, accessibility touch dan constructor View. Ini bukan pengisytiharan semua accessibility/perilaku telah lengkap.
- **42 semakan safety/capability setiap resolusi** (84 untuk dua profil). Ini bukan 84 semakan kejayaan embedding. Ulangan selepas dialog System UI ditutup direkodkan berasingan, tidak ditambah untuk membesarkan jumlah.
- UID host 10060 dan sasaran 10059 pada emulator: berbeza; signature permission dan handshake Binder cross-UID disahkan. Binding sahaja tidak membuka Activity sasaran.
- **11 semakan sasaran standalone**: counter, input dalaman, Back, dialog sendiri, audio focus/tone dan lifecycle cleanup; panggilan bridge daripada UID sasaran sendiri ditolak kerana bukan UID host. Ini bukan ujian aplikasi penyerang lengkap.
- 20 percubaan start/stop **ditolak capability gate** bagi setiap profil: tiada display/binding/input-thread tertinggal pada objek selepas penutupan. Bukan 20 kitaran hosted playback berjaya. `dumpsys display` selepas ujian hanya menunjukkan skrin terbina.
- Jam, waktu solat dan kalendar diperiksa sepenuhnya kelihatan dalam viewport sebelum/selepas probe. Snapshot yang diambil tepat ketika `wm size` berubah boleh menunjukkan scaling sementara; bukti utama ialah screenshot instrumentasi selepas layout siap.
- Sumber asal: **226 fail sepadan checkpoint Fasa6C**, 63 entri Git lokal dikekalkan. Lima fail LauncherActivity/HomeWidgets/GlassSurface/GlassBackdrop/IosStyle salinan makmal sama kecuali namespace. APK stabil dan arkib checkpoint masih sama SHA256; lihat `PRESERVATION.json`. NexaDash tidak diubah.

## Prestasi dan masalah yang belum selesai

PSS host pada ujian awal sekitar 112MiB sebelum dan 103–105MiB selepas/GC; ukuran akhir host sahaja 114096KiB (~111MiB). Ulangan akhir: PSS sebelum 116271/123124KiB, selepas 108052/106613KiB, selepas GC request 106844/106069KiB; CPU kumulatif bertambah kira-kira 5.1/5.8s. Java used instrumentasi sekitar 1.6–3.6MB; `dumpsys meminfo` Java Heap PSS 6176KiB. Ini metrik berbeza, bukan RAM BitChord atau jumlah dua aplikasi aktif.

CPU kumulatif proses bertambah kira-kira 3.5–3.9 saat sepanjang prosedur percubaan ditolak/diagnostik/screenshot; bukan peratus CPU playback. Startup daripada log sistem kira-kira 12.5–13.1s host dan 4.4s target standalone dalam software emulator; bukan cold-start benchmark terkawal. Gfxinfo sampel akhir hanya 6 frame, semuanya janky (100%), median bucket 2050ms; tidak mewakili FPS head unit. Native startup, hosted resize latency dan prestasi playback tidak dapat diukur kerana panel belum ditubuhkan.

Emulator mengalami **ANR System UI semasa boot** pada 09:55:26, sebelum ujian PoC akhir. Screenshot awal turut menangkap dialog tersebut dan dikekalkan berawalan `initial-`. Selepas memilih Wait, paparan direkodkan semula. `dumpsys activity lastanr` menyatakan tiada ANR, tetapi logcat membuktikan ANR System UI; oleh itu jangan menggunakan output lastanr itu untuk mendakwa tiada ANR keseluruhan.

Satu APK perantaraan makmal pernah crash `ClassNotFoundException` akibat output incremental selepas pemindahan namespace. Folder sumber diselaraskan dan incremental compilation makmal dimatikan; compile penuh tanpa clean menghasilkan kelas sebenar, diikuti pemasangan dan ujian akhir berjaya. Rekod crash lama dikekalkan. Ini tidak melibatkan APK stabil. Tiada crash aplikasi baharu dilihat dalam larian akhir yang direkodkan; bukan jaminan tiada crash fizikal.

**Belum disahkan:** rendering target pada secondary display/SurfaceView, touch cross-process, Search/Playlist/Library/Now Playing/Settings BitChord, keyboard sistem, OAuth/permission dialogs, popup window umum, Back hosted, resize sebenar, 20 kitaran hosted, audio focus antara pemain, bunyi fizikal, RAM/CPU pemain ARM32 dan suspend/resume head unit. Audio ujian hanya beep sintetik 300ms; bukan muzik.

## Langkah seterusnya

Pasang sasaran cooperative dan host makmal pada TEST9278B tanpa mengganti launcher/data stabil; ikut `UJIAN-FIZIKAL-MS.md`. Hanya apabila display >0, UID berasingan, frame sebenar dan interaksi lulus, teruskan fork source rasmi BitChord tag v1.9 (GPLv3) dengan package eksperimen, hosted entry, opt-in, bridge input/lifecycle dan pairing signer yang sama. Sediakan corresponding source serta atribusi GPLv3 apabila fork diedarkan. Jangan ubah APK rasmi. Stash kekal ditangguhkan sehingga BitChord terbukti.

## Hasil

APK host/target, source makmal, screenshot emulator sebenar, log dan galeri offline berada di `/workspace/output/phase6d-a1`. `SHA256SUMS.txt` dan `APK-METADATA.json` mengesahkan fail. Tiada kunci, password atau token dalam ZIP source/hasil. Aset warisan mempunyai lesen berasingan yang dijelaskan dalam NOTICE; salinan makmal ini untuk semakan pemilik, bukan pengisytiharan lesen pengedaran awam semua aset.

Tiada commit, push, reset, clean, upload GitHub atau rebuild iDash stabil dilakukan. Mekanisme penerbitan artifact/lampiran Codex Web tidak tersedia melalui tool sesi ini; path lokal ini bukan pautan download yang disahkan.
