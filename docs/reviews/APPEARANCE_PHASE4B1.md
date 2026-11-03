# Fasa 4B-1 — Glass & Appearance (lokal)

## Sasaran sebenar dan checkpoint

Rekod head unit pemilik TEST9278B/alps menunjukkan **1024×600 @160dpi, API27, ARM32**. Rujukan: laporan foto pemilik `owner-feedback-next19-20261031.json` dan semakan video `head-unit-native-owner-20261030.json` yang disimpan dalam workspace projek terdahulu. 800×480 ialah fallback ujian, bukan resolusi fizikal yang disahkan.

Sebelum sebarang kod diubah, status Git diperiksa dan **197 fail** working tree (tracked dan semua non-ignored untracked) disalin ke:
`/workspace/checkpoints/idash-before-phase4b1-20261102T110140Z/working-tree.tar.gz`

Fail arkib disahkan semula dengan hash setiap fail, termasuk fail baharu Fasa 2–4A. Folder checkpoint mengandungi manifest SHA256, status, HEAD, staged/unstaged patch. `.git`, cache/build terabai, env/credential/keystore/private key dikecualikan; fail teks turut disemak untuk corak rahsia. Tiada fail baseline hilang selepas perubahan ini. Ini checkpoint working tree lokal, bukan backup cloud luar workspace.

## Fungsi

- Settings → Appearance: dua lajur landscape, Live Preview di kiri; tab **Material** (Adaptive/Light/Dark, transparency, tint) dan **Effects** (LOW/BALANCED/HIGH, reflection Low/Normal/High, animation On/Off) di kanan. Sasaran sentuhan minimum 48dp, tanpa scroll pada dua konfigurasi ujian.
- Draft immutable dan cache preview berasingan. **Apply** menyimpan SharedPreferences secara atomik lalu menerbitkan cache yang sudah disediakan dan membina semula views dalam Activity yang sama. **Cancel/Back** membuang draft tanpa menulis preferences. **Reset** menyediakan default dalam draft sahaja; pengguna masih perlu Apply.
- Default BALANCED, Adaptive tint, Normal reflection dan motion On. Nilai lama dikekalkan sehingga perubahan diterapkan; profil lama tidak dimigrasi secara senyap. SharedPreferences baharu diperiksa jenis dan julatnya.
- Ketelusan diminta dalam julat 10–30%; floor opacity mengikut Light/Dark dan wallpaper melindungi kontras. Oleh itu sesetengah nilai di hujung julat boleh dihadkan pada latar cerah. Tint Adaptive/Neutral/Soft Blue/Soft Rose/Soft Mint dan foreground teks/ikon menggunakan enjin Canvas sedia ada. Blur shader berat/Compose/database tidak ditambah.
- Preview membina semula cache **hanya** apabila kualiti/wallpaper berubah, dengan debounce 250ms di worker sedia ada. Ketelusan dipaparkan dengan throttle 32ms; tiada bitmap baharu semasa slider bergerak atau dalam draw(). Apply berkongsi bitmap preview yang siap dengan backdrop global.
- LOW kekal pada cache 128px dan tidak mengaktifkan specular/reflection HIGH; BALANCED 224px, HIGH 320px. Motion menghormati Android animator scale; tiada animasi berterusan.
- Kategori lain dan pautan wallpaper/display/legacy icons kekal. More meminta pengguna menyelesaikan draft dahulu sebelum membuka pilihan lama yang menyimpan terus.

## Perlindungan skop

Hash `QuickControls`, `ControlMedia`, `ControlUtilities`, `MediaAccessService`, `ControlGlyph` dan `JakimCalendar` sama dengan checkpoint. Implementasi buildHome/buildDock/buildDrawer dan fungsi solat tidak berubah. Lifecycle Activity hanya membersihkan/menyambung editor; penyelesaian app discovery tidak lagi membina semula halaman Appearance yang sedang diedit. Susun atur dashboard/dock/drawer tidak diubah; material glass berubah hanya melalui Apply pilihan pemilik.

Tiada Camera/Flashlight, permission baharu, native library, dependency besar, root atau hidden API baharu.

## Fail diubah

- Baharu: `AppearanceEditor.java`, `GlassAppearance.java`.
- `DashSettings.java`: laluan Appearance moden; pilihan lama masih boleh dibuka melalui More.
- `GlassSurface.java`: parameter visual pilihan; konstruktor/material lama kekal ketika customization belum diterapkan.
- `GlassBackdrop.java`: metadata kualiti frame dan clamp tier; algoritma frost yang sama.
- `IosStyle.java`: bacaan preference selamat, sambungan material/kontras dan motion, notifikasi perubahan wallpaper ke preview.
- `CapsuleSlider.java`: material/foreground mengikut appearance; handler drag tidak diubah.
- `LauncherActivity.java`: lifecycle dan perlindungan draft semasa discovery aplikasi selesai.
- Ujian baharu: `AppearanceChecks.java`, `AppearanceRenderChecks.java`, `AppearancePersistenceChecks.java`, `AppearanceGradientChecks.java`; entri manifest androidTest.

## Pengesahan

Build debug, lint dan build instrumentation akhir berjaya. Lint: **0 ralat / 63 amaran**, termasuk amaran sedia ada dan string UI yang belum diekstrak untuk terjemahan. Tiada error NewApi/MissingPermission.

- Rendering parameter: **113 semakan** lulus, termasuk kontras, perubahan tint/reflection, clamp dan LOW/HIGH.
- Editor sebenar: **62 semakan pada setiap resolusi**, termasuk Apply/Cancel/Reset, cache tidak dibina semasa 50 perubahan slider, debounce burst quality kepada satu cache, tiga tier, cleanup, Light Glass pada teks/ikon Control Center dan reduced motion.
- Persistence: **3 write + 6 read**, dengan force-stop dan PID proses berbeza; semua enam tetapan dan LOW cache kekal selepas pelancaran semula.
- Suite penuh: **603 semakan lulus**, merangkumi 357 regresi terdahulu serta ujian baharu. Selepas itu 5 semakan gradient bawaan ditambah dan lulus (**608 semakan dalam set pengesahan**, termasuk konfigurasi berulang pada dua resolusi). Selepas pembetulan gradient, ujian rendering/editor/cache yang terkesan diulang pada build akhir; ulangan ini tidak ditambah sekali lagi dalam jumlah 608.

Regresi terdahulu: Smoke 58, Panel 21, Wallpaper 13, GlassRender 36, ControlGlass 36, SystemControls 17, Settings 27, MediaUtilities 29, ControlLayout 51×2, ControlVisual 9×2.

Gradient Blue/Rose bawaan menggunakan tint fallback yang mengikut palet apabila customization diaktifkan; preview menggunakan View wallpaper prosedural sedia ada tanpa bitmap tambahan. Profil default yang belum diubah kekal seperti sebelum fasa ini.

Ujian awal mengesan clipping 1px pada footer; margin dibetulkan dan kedua-dua saiz disahkan semula. Screenshot akhir ialah Activity sebenar pada emulator API27 x86_64 @160dpi, bukan UI simulasi Canvas. Wallpaper gelap/cerah ialah gradient ujian asli; tiada aset MRiOS baharu disalin. Ujian Canvas dilaporkan berasingan.

Artefak: `/workspace/output/phase4b1/` — Material dan Effects, Dark/Light, 1024×600 dan 800×480.

## Batasan

Belum diuji pada head unit fizikal ARM32/RAM 1GB. Tiada FPS, latensi sentuhan, GPU overdraw atau long-duration heap/leak profiling yang disahkan; cleanup handler/cache/callback preview diuji. Font scale/DPI lain belum disahkan. Data resolusi fizikal berasal daripada rekod pemilik terdahulu, bukan bacaan langsung peranti pada sesi ini.

Render glass menggunakan frost wallpaper bercache dan ilusi gradient/rim, bukan blur masa nyata semua views atau pembiasan sebenar. Keterbacaan diuji pada wallpaper ujian, bukan jaminan setiap imej. Sesetengah komponen lama mengekalkan profil geometri/shadow sendiri walaupun menerima parameter appearance.

Tiada commit, push atau release APK dihasilkan. Semua perubahan terdahulu dikekalkan.
