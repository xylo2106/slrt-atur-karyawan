# Atur Karyawan – PT SLRT

Aplikasi web (satu file `index.html`) untuk mengatur karyawan dan membuat pesan WhatsApp harian:

- **Pesan WA** – shift per karyawan, pembagian area kerja, piket, driver, nasi siang → 2 pesan siap kirim (gaya *Menarik* atau *Polos*).
- **Jadwal** – tabel shift mingguan.
- **Cuti & Izin** – pengajuan dengan status Berlangsung / Akan datang / Selesai.
- **Karyawan** – input daftar sekaligus (`Nama, NIK` per baris), urutan, tanggal lahir, kode shift.
- **Ringkasan** – sebaran shift (donat) dan rekap bulanan.
- **Beranda** – menu, timeline pengumuman, cuti, dan ulang tahun.

## Di mana app dijalankan

App utama di-host sebagai artifact claude.ai:
https://claude.ai/artifact/UF47P2MuEA26BF2Aw3HgGX

Di sana data tersimpan di **server** (kapabilitas `db` claude.ai), hanya bisa dibaca/ditulis pemilik & editor.
Repo ini adalah **sumber kode** dan riwayat versinya.

Kalau `index.html` dibuka di luar claude.ai (langsung dari file atau GitHub Pages), app tetap jalan
tapi data hanya tersimpan di browser perangkat itu (status **Lokal**). Gunakan menu *Cadangan* untuk memindahkan data.

## Struktur data (server)

| Dokumen | Isi |
|---|---|
| `app/main` | nama sapaan, daftar karyawan, kode shift, judul pesan, cuti/izin, pengumuman |
| `days/YYYY-MM-DD` | shift, area kerja, piket, driver, nasi siang untuk tanggal tsb |

## Cara update

1. Ubah `index.html` (atau minta Claude mengubahnya di chat).
2. Commit & push ke repo ini.
3. Publikasikan ulang ke artifact claude.ai yang sama, agar link di atas ikut terbarui. Data di server tidak terhapus saat app diperbarui.

Tidak ada kunci/token yang disimpan di repo ini. Akses update memakai koneksi akun GitHub ke Claude.
