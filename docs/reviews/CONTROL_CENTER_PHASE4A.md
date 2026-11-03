# Fasa 4A — Premium Control Center (lokal)

## Perubahan

- Layout landscape: Connectivity dan Media lebih lapang; Play/Pause 56dp; dua slider 80/88dp lebar kawasan sentuh. Focus berasingan dan Settings/Timer/Calculator mengisi baris utiliti tanpa ruang kosong Camera/Flashlight. Butang bawah kekal 48dp dengan padding seragam.
- Tajuk utama/media rata kiri. Label ikon/utiliti serta teks butang di tengah; nilai slider berkongsi saiz, tinggi dan baseline. Sistem font 22sp tajuk, 20sp media utama, 14sp maklumat, 12sp label, 13sp nilai/butang; teks panjang menggunakan ellipsis.
- Profil glass khusus Control Center: frost wallpaper cache dikekalkan, tint adaptif, rim bercahaya lembut, refleksi pendek mengikut bucu dan bevel yang lebih terkawal. Pantulan diagonal panjang tidak digunakan pada profil ini. Frost menutup bayang teks dashboard di bawah panel. Material biasa di dashboard/dock/drawer tidak ditukar.
- Ikon Canvas asli: skala dan stroke seragam, sambungan aktif biru dengan label status sebenar, keadaan disabled lebih jelas (alpha 0.70). Slider menggunakan fill gradient, garisan aras dan lengkung anti-alias tanpa mengubah handler drag.
- Tiada animasi berterusan ditambah. Transisi masuk sedia ada menghormati pilihan motion dan Android animator duration scale.
- Camera/Flashlight hanya mempunyai kebergantungan di Control Center. Butang, pencarian kamera/torch callback, CAMERA permission, ciri kamera pilihan, intent visibility kamera dan callback permission khusus dibuang. Tiada kod kamera komponen lain ditemui. Timer/Calculator/Settings dan DND dikekalkan.

## Fail Fasa 4A

Produksi: `QuickControls.java`, `GlassSurface.java`, `IosStyle.java`, `CapsuleSlider.java`, `ControlGlyph.java`, `ControlUtilities.java`, `ControlMedia.java` (alpha visual sahaja), `LauncherActivity.java` (pembersihan kamera sahaja), `AndroidManifest.xml`.

Ujian: `ControlLayoutChecks.java`, `MediaUtilitiesChecks.java`; baharu `ControlVisualChecks.java`, `ControlGlassChecks.java`, `Phase3GlassSurface.java` (snapshot material iDash sebelum polish, test sahaja), manifest instrumentation untuk menjalankan seluruh suite daripada satu APK ujian.

Audit sumber: kaedah kawalan sistem QuickControls dan handler sentuhan CapsuleSlider sama tepat dengan salinan sebelum fasa ini. ControlMedia sama kecuali alpha disabled. `GlassBackdrop`, `MediaAccessService`, Settings dan rendering dashboard tidak diubah.

## Pengesahan

Build akhir `assembleDebug`, `lintDebug`, `assembleDebugAndroidTest`: berjaya; lint 0 ralat, 59 amaran. Amaran lama (termasuk refleksi FM di luar skop ini) kekal; integrasi visual ini tidak menambah hidden API/dependency/native library.

Suite akhir: **357 semakan lulus dalam 12 run**, termasuk pengulangan layout/visual pada dua resolusi. Tiada kegagalan pada suite akhir.

| Ujian | Semakan lulus |
|---|---:|
| Smoke UI penuh | 58 |
| Panel (fixture berasingan) | 21 |
| Wallpaper | 13 |
| Glass cache/render sedia ada | 36 |
| Profil glass/kontras/cache baharu | 36 |
| Kawalan sistem | 17 |
| Settings/brightness/volume | 27 |
| Media/DND/utiliti | 29 |
| Layout 800×480 / 1024×600 | 51 / 51 |
| Visual sebenar 800×480 / 1024×600 | 9 / 9 |

Sampel kontras teks media akhir: **4.80:1–8.16:1** pada dua wallpaper dan dua resolusi (alpha teks diambil kira). Semua sampel melepasi 4.5:1. Imej disemak secara visual: tiada overlap/clipping yang kelihatan pada konfigurasi ujian, label utiliti berpusat, nilai slider berkongsi baseline. Saiz font besar/accessibility lain belum disahkan.

Ujian material membandingkan output piksel material biasa dengan snapshot sebelum fasa ini pada tiga kualiti dan empat warna, menyemak kontras pada latar putih, serta identiti cache selepas draw berulang. Penggunaan heap/peak PSS tidak diukur secara terkawal; proses telah tamat semasa pengambilan meminfo selepas suite, jadi tiada angka prestasi memori dilaporkan.

Artefak `/workspace/output/phase4a/`: sebelum/selepas pada 800×480 dan 1024×600, setiap satunya dengan wallpaper ujian gelap dan berwarna yang sama. Ini screenshot Activity sebenar pada emulator API27 x86_64 pada 160dpi/font scale 1; wallpaper ujian ialah gradient asli, bukan aset MRiOS baharu dan bukan simulasi Canvas bagi UI. Imej `after-media-800x480.png` menggunakan MediaSession fixture berasingan tanpa audio.

## Batasan

Tiada ujian fizikal ARM32/head unit Proton atau RAM 1GB; tiada dakwaan FPS, latency atau prestasi GPU sebenar. Frost BALANCED dikongsi dan dibatasi 224px; shader/path dicache mengikut bounds/revisi, tiada bitmap baharu dalam draw(). Kaca masih menggunakan beberapa lapisan draw/fill/rim; overdraw GPU belum diprofil pada peranti sasaran.

Pembiasan ialah ilusi gradient/rim, bukan refraction sebenar. Frost melukis wallpaper bercache, bukan blur masa nyata semua kandungan aplikasi di belakang. Control Center menggunakan Dark Glass berwarna untuk kontras; Light/Dark material sedia ada di luar panel dipelihara. Pengukuran kontras ialah sampel latar teks media pada wallpaper ujian dan bukan jaminan untuk setiap wallpaper, font scale atau ROM. Tingkah laku Spotify/YouTube, audio dan perkakasan head unit masih memerlukan ujian fizikal.

Tiada commit, push atau release APK dibuat. Semua perubahan lokal terdahulu dikekalkan; salinan sebelum fasa ini disimpan di `/tmp/idash-phase4a-before/` sepanjang sesi.
