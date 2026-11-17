# Atur Karyawan – PT SLRT

Aplikasi web (satu file `index.html`) untuk mengatur karyawan dan membuat pesan WhatsApp harian:

- **Pesan WA** – shift per karyawan, pembagian area kerja, piket, driver, nasi siang → 2 pesan siap kirim (gaya *Menarik* atau *Polos*).
- **Jadwal** – tabel shift mingguan.
- **Cuti & Izin** – pengajuan dengan status Berlangsung / Akan datang / Selesai.
- **Karyawan** – input daftar sekaligus (`Nama, NIK` per baris), urutan, tanggal lahir, kode shift.
- **Ringkasan** – sebaran shift (donat) dan rekap bulanan.
- **Beranda** – menu, timeline pengumuman, cuti, dan ulang tahun.
- **Penyimpanan** – server GitHub, status sinkron, dan cadangan data.

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

**Keamanan token**

- Token hanya tersimpan di browser perangkat yang dipakai. Token tidak pernah masuk ke kode atau repo.
- Token hanya bisa membaca/menulis repo data, tidak bisa menyentuh repo lain.
- Jangan kirim token lewat chat atau WhatsApp.
- Kalau HP hilang: GitHub → Settings → Developer settings → Personal access tokens → hapus token itu.

**Sinkron antar perangkat:** perubahan dikirim ±3 detik setelah berhenti mengetik. Perangkat lain mengambil perubahan saat app dibuka kembali, dan setiap 1 menit selama app terbuka. Jika dua perangkat mengubah bulan yang sama, perubahan pada tanggal berbeda digabung otomatis. Jika tanggal yang sama diubah di dua perangkat, simpanan terakhir yang dipakai.

## Menjalankan dari GitHub Pages

Settings → Pages → *Deploy from a branch* → `main` / `(root)` → Save. App akan tersedia di
`https://<username>.github.io/slrt-atur-karyawan/`.

Repo app ini **public** supaya GitHub Pages bisa dipakai gratis. Isinya hanya kode dan logo, tanpa data karyawan maupun token. Data tetap di repo data yang **private**. Jangan pernah membuat repo data menjadi public.

## Link claude.ai

https://claude.ai/artifact/UF47P2MuEA26BF2Aw3HgGX

## Cara update app

1. Ubah `index.html` (atau minta Claude mengubahnya di chat).
2. Commit & push ke repo ini. GitHub Pages ikut terbarui dalam 1–2 menit.
3. Publikasikan ulang ke artifact claude.ai yang sama.

Update app tidak menghapus data. Data ada di repo data dan server claude.ai, bukan di repo app.
