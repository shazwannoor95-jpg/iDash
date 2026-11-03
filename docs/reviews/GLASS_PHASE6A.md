# Fasa 6A — Liquid Glass refinement

Implementasi asal Java Native Views/Canvas, API27. Tiada perubahan layout,
applicationId, signing, integrasi media atau kawalan sistem. Tiada commit/push.

## Perubahan material

- `GlassSurface`: buang opacity floor; alpha lapisan warna ialah
  `round(255 × (1 − transparency/100))`. Tint menggunakan 26% warna wallpaper
  dan 74% neutral material. Dark menggunakan penyerapan RGB frost 166/255
  melalui `LightingColorFilter` statik; ini bukan opacity floor.
- `IosStyle`: alpha lalai 140, Dark neutral `#202832`, rim luar/dalam kekal
  0.85/0.65dp. Curved specular mengikuti tepi atas dan bucu; bevel dikekalkan.
- `GlassAppearance`: julat Transparency 10–65%, lalai baharu 45% bagi preference
  yang belum wujud dan Reset. Nilai lama 10/20/30 kekal sama; key dan makna
  peratus tidak diskala semula. Pilih 45%, atau Reset kemudian Apply, untuk
  menggunakan profil baharu pada pemasangan yang sudah menyimpan 20%.
- `AppearanceEditor`: chrome dan preview menggunakan draft material/polisi
  Adaptive yang sama, private cache serta warna teks/kawalan native yang sesuai.
  Apply menyimpan; Cancel membuang draft; Reset hanya mengubah draft.

Polisi Adaptive tunggal: luminance tint wallpaper >0.35 memilih Light,
selebihnya Dark. Light menggunakan teks gelap; Dark menggunakan teks cerah.

## Kontras dan kos

Perlindungan kontras setempat menggunakan gradient lembut di belakang teks dan
glyph. Geometri, sampel wallpaper dan shader disimpan sehingga material,
bounds atau geometri kandungan berubah. Tiada bitmap baharu dalam `draw()`;
cache frost `GlassBackdrop` sedia ada tidak diubah. LOW tidak melukis
reflection/bevel; perlindungan keterbacaan kekal pada semua kualiti.

Ini **wallpaper frost bercache**, bukan live backdrop blur atau pembiasan optik
sebenar. Pada Dark Glass atas wallpaper neutral terang, bayangan perlindungan label
masih boleh kelihatan, terutama apabila transparency dinaikkan ke 65%. Perlindungan ini menambah
draw call setempat dan traversal geometri; kos ARM32 sebenar belum diukur.

## Pengesahan

`Glass6aChecks` mengambil screenshot Activity sebenar melalui UiAutomation,
menggunakan wallpaper gradient ujian asli. Baseline ialah APK Fasa 5A yang
disimpan, bukan renderer tiruan. Matriks akhir: biru/merah jambu/neutral,
Adaptive/Light/Dark, 800×480 dan 1024×600; nilai lama 20% dan lalai baharu 45%.
65% diuji tambahan pada neutral. Kontras disampel pada kawasan teks/glyph
dengan ink disembunyikan sementara, mengekalkan background dan geometri.
Ini bukan pensijilan WCAG untuk semua wallpaper, keadaan disabled atau semua
ikon Canvas. Ujian Canvas berasingan meliputi LOW/BALANCED/HIGH, nilai slider,
cache identity dan kesinambungan default/Apply.

Keputusan akhir, screenshot asli, profiling dan jumlah semakan direkodkan dalam
`/workspace/output/phase6a/LAPORAN.md` dan `tests/summary.json`.

Emulator API27 x86_64 TCG/SwiftShader bukan head unit TEST9278B ARM32/RAM1GB.
Ujian native Panel tidak diulang kerana AVD ini tiada secondary-display feature.
Tiada dakwaan FPS, masa respons, overdraw atau ketiadaan memory leak pada
peranti fizikal. Semua 51 perubahan lokal terdahulu dilindungi oleh checkpoint
sebelum Fasa 6A di `/workspace/checkpoints/idash-before-phase6a-20261102T233154Z`.
