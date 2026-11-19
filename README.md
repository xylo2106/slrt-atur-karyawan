# Atur Karyawan – PT SLRT

Aplikasi web (satu file `index.html`) untuk mengatur karyawan dan membuat pesan WhatsApp harian:

Tampilan bergaya Lark: tab bawah di HP, menu samping di layar lebar, daftar dan lembar pilihan (bottom sheet).

- **Hari ini** – ringkasan hadir/OFF/cuti/belum diatur, shift hari ini, kirim 3 laporan WA, hal yang perlu perhatian (belum diatur, cuti selesai, ulang tahun), pengumuman.
- **Jadwal** – tabel shift sebulan penuh; ketuk sel untuk memilih shift. *Isi dengan pola* (urutan berulang, mis. 4-4-4HM-4HM-OFF-OFF) untuk rentang tanggal dan regu tertentu, *Salin minggu lalu*, saring per regu.
- **Kehadiran** – Hadir / OFF / Cuti / Belum diatur per hari, atur shift langsung, karyawan reguler (otomatis hadir di hari kerja, bisa ditandai OFF), nasi siang.
- **Karyawan** – cari, saring Shift/Reguler/regu, profil per karyawan (data, nama Mandarin, regu, urutan, rekap bulan ini), tambah satu atau banyak sekaligus (`Nama, NIK` per baris).
- **Pesan WA** – jadwal shift, pembagian area kerja (piket, driver), daftar hadir + nasi siang; gaya *Lengkap* atau *Polos*; bahasa Indonesia, 中文, atau keduanya.
- **Cuti & izin** – Berlangsung / Akan datang / Selesai.
- **Statistik** – rekap bulanan per karyawan dan per kode shift, unduh CSV (Excel) atau salin tabel.
- **Hari libur & masuk hari libur** – daftar hari libur (libur nasional & cuti bersama SKB 3 Menteri 2026/2027 bisa ditambahkan sekali ketuk, atau libur perusahaan sendiri). Pada hari Minggu dan hari libur karyawan reguler otomatis libur; admin memilih siapa yang masuk (ketuk tanggal di Jadwal, atau kartu di Hari ini/Kehadiran). Karyawan reguler yang masuk diberi kode **HL**; karyawan shift yang masuk tetap memakai kode 4 / 4HM.
- **Kode harian reguler** – Senin–Kamis dan Sabtu `1` jam lembur, Jumat `3` jam lembur, Minggu/libur `HL`; bisa diubah di Pengaturan atau per orang per hari. Kode tampil di pesan Jadwal shift (bagian Reguler).
- **Tanda SKS / TK** – tandai karyawan sakit (SKS) atau tanpa keterangan (TK) per hari; tampil di jadwal, kehadiran, pesan WA, dan statistik.
- **Pengaturan** – kode shift + keterangan jam, pola shift, regu, hari libur, hari kerja & kode reguler, bahasa tampilan.
- **Dwibahasa** – tampilan Indonesia + 中文 berdampingan (bisa diganti ID saja atau 中文 saja); nama Mandarin dibuat dari nama panggilan.
- **Penyimpanan** – server GitHub, status sinkron, hubungkan HP lain lewat QR, dan cadangan data.

## Penyimpanan data

App memilih penyimpanan otomatis:

| App dibuka dari | Data disimpan di |
|---|---|
| Link claude.ai (artifact) | Server claude.ai (hanya pemilik & editor) |
| GitHub Pages / file di perangkat, **server GitHub terhubung** | Repo GitHub **private** milik Anda (folder `data/`) |
| GitHub Pages / file di perangkat, belum terhubung | Browser perangkat itu saja (status **Lokal**) |

Kedua server (claude.ai dan GitHub) terpisah. Untuk memindahkan data, pakai **Penyimpanan → Cadangan → Salin cadangan**, lalu **Pulihkan** di tempat tujuan.

### Server GitHub (repo data private)

Data **tidak** disimpan di repo app ini, tapi di repo terpisah yang private:

```
slrt-atur-karyawan-data/
└── data/
    ├── main.json            # karyawan, kode shift, judul pesan, cuti/izin, pengumuman
    └── days/
        ├── 2026-11.json     # shift, area, piket, driver, nasi siang per tanggal (satu file per bulan)
        └── 2026-12.json
```

Setiap simpan menjadi satu commit, jadi riwayat perubahan bisa dilihat dan dikembalikan di GitHub.

**Cara menghubungkan (sekali per HP/laptop):**

1. Buat repo **private** baru, misalnya `slrt-atur-karyawan-data`, centang *Add a README file*. → https://github.com/new
2. Buat **fine-grained token** → https://github.com/settings/personal-access-tokens/new
   - *Repository access*: **Only select repositories** → pilih repo data saja
   - *Permissions → Repository permissions → Contents*: **Read and write**
   - Atur masa berlaku (misalnya 1 tahun). Saat habis, buat token baru dan hubungkan ulang.
3. Buka app → **Penyimpanan** → isi username, nama repo data, tempel token → **Hubungkan**.

**Menghubungkan HP tanpa mengetik token:** di perangkat yang sudah terhubung, buka **Penyimpanan → 📱 Hubungkan HP lain (scan QR)**. Scan QR dengan kamera HP lalu ketuk link yang muncul, dan HP langsung terhubung. QR tertutup otomatis setelah 2 menit. Token dihapus dari alamat browser begitu link dibuka.

Di iPhone, kalau app dipasang ke Layar Utama (*Add to Home Screen*), hubungkan dari dalam ikon itu. iOS memisahkan penyimpanan Safari dan ikon Layar Utama.

**Keamanan token**

- Token hanya tersimpan di browser perangkat yang dipakai. Token tidak pernah masuk ke kode atau repo.
- Token hanya bisa membaca/menulis repo data, tidak bisa menyentuh repo lain.
- Jangan kirim token, QR, atau link penghubung lewat chat atau WhatsApp. Isinya sama dengan token.
- Kalau HP hilang: GitHub → Settings → Developer settings → Personal access tokens → hapus token itu.

**Sinkron antar perangkat:** perubahan dikirim ±3 detik setelah berhenti mengetik. Perangkat lain mengambil perubahan saat app dibuka kembali, dan setiap 1 menit selama app terbuka. Jika dua perangkat mengubah bulan yang sama, perubahan pada tanggal berbeda digabung otomatis. Jika tanggal yang sama diubah di dua perangkat, simpanan terakhir yang dipakai.

## Menjalankan dari GitHub Pages

Settings → Pages → *Deploy from a branch* → `main` / `(root)` → Save. App akan tersedia di
`https://<username>.github.io/slrt-atur-karyawan/`.

Repo app ini **public** supaya GitHub Pages bisa dipakai gratis. Isinya hanya kode dan logo, tanpa data karyawan maupun token. Data tetap di repo data yang **private**. Jangan pernah membuat repo data menjadi public.

## APK Android

Unduh di HP: **https://github.com/xylo2106/slrt-atur-karyawan/releases/latest/download/AturKaryawan-SLRT.apk**

1. Buka link di atas di HP Android, unduh, lalu ketuk file APK-nya.
2. Jika diminta, izinkan *Install unknown apps* untuk browser/File Manager.
3. Buka **Atur Karyawan** → **Penyimpanan** → **📷 Scan QR dari perangkat yang sudah terhubung** → arahkan ke QR di laptop (*Penyimpanan → Hubungkan HP lain*).

APK ini membuka app GitHub Pages di dalam WebView, jadi setiap update `index.html` langsung terpakai tanpa memasang APK baru.
Link WhatsApp dan GitHub dibuka di aplikasinya masing-masing. APK baru dibangun otomatis (GitHub Actions → *Build APK*) hanya bila folder `android/` berubah.

Selama secrets kunci belum diatur, APK ditandatangani dengan kunci debug. Memasang APK versi baru di atas versi lama mungkin perlu menghapus versi lama dulu. Data tetap aman di repo GitHub, tinggal hubungkan ulang lewat scan QR.

## Link claude.ai

https://claude.ai/artifact/UF47P2MuEA26BF2Aw3HgGX

## Cara update app

1. Ubah `index.html` (atau minta Claude mengubahnya di chat).
2. Commit & push ke repo ini. GitHub Pages ikut terbarui dalam 1–2 menit.
3. Publikasikan ulang ke artifact claude.ai yang sama.

Update app tidak menghapus data. Data ada di repo data dan server claude.ai, bukan di repo app.
