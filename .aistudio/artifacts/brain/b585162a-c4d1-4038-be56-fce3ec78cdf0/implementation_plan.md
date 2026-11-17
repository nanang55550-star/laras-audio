# Penyempurnaan Tampilan Utama — Daftar Lagu Padat & Header Ringkas

Tampilan utama saat ini sudah fungsional, namun area atas masih terpakai oleh kolom pencarian permanen dan tombol aksi besar, serta setiap lagu dibungkus kartu berbingkai yang membuat daftar terasa renggang. Pembaruan ini merapikan tampilan utama **Laras Audio** menjadi **daftar lagu padat bergaya audiophile modern** yang langsung menonjolkan koleksi musik sejak pandangan pertama.

---

## Keputusan Pengguna Terkonfirmasi

> [!IMPORTANT]
> Seluruh arah desain di bawah ini mengikuti pilihan yang Anda konfirmasi.

- **Arah Tata Letak**: **Fokus pada daftar lagu padat tanpa banyak kartu**, memaksimalkan jumlah lagu yang terlihat sekaligus di layar.
- **Pencarian**: **Ikon cari di header yang melebar saat diketuk**, menjaga bagian atas tetap bersih saat tidak sedang mencari.
- **Gaya Daftar Lagu**: **Daftar lagu rapat dengan garis pemisah tipis elegan** menggantikan kartu berbingkai terpisah.

---

## 1. Ringkasan & Nilai Perbaikan

- **Apa yang Berubah**: Area header, bilah filter/kontrol, dan tampilan baris lagu pada layar **Pustaka** disederhanakan agar jauh lebih padat, lega, dan berkelas tanpa mengurangi fitur apa pun.
- **Manfaat Utama**: Pengguna langsung melihat lebih banyak judul lagu tanpa harus banyak menggulir, navigasi pencarian tetap cepat dalam satu ketukan, dan hierarki visual terasa lebih rapi menyerupai pemutar musik hi-fi modern.

---

## 2. Pengalaman Pengguna & Desain Visual

### Alur Interaksi Utama

1. **Header Terpadu & Pencarian Ekspansif**:
   - Bagian atas menampilkan judul **Laras Audio** (*Playfair Display*) dan subjudul ringkas (`N Lagu Offline · MP3 · FLAC · WAV`).
   - Di kanan header terdapat dua tombol ikon sejajar: **Cari** (kaca pembesar) dan **Pengaturan** (roda gigi).
   - Saat ikon **Cari** diketuk, kolom pencarian elegan muncul dengan animasi *expand* halus di bawah header; mengetuk tombol tutup/hapus akan membersihkan kueri dan meringkas kembali header.
2. **Bilah Filter & Aksi Cepat Sejajar**:
   - Baris filter format (**Semua**, **FLAC**, **WAV**, **MP3**, **Lainnya**) dibuat lebih tipis dan rapat.
   - Tombol **Putar Semua** dan **Acak** disajikan sebagai baris kontrol ringkas tepat di atas daftar lagu bersama jumlah hasil pencarian, sehingga tidak memakan ruang vertikal berlebih.
3. **Daftar Lagu Rapat Berpemisah Tipis**:
   - Setiap lagu tampil sebagai baris datar rapat (*compact row*) dengan sampul album `48.dp`, judul (*IvoryText*), metadata satu baris (`Artis · Lencana Format · Durasi`), tombol Favorit, dan menu aksi cepat.
   - Di antara baris terdapat garis pemisah horizontal tipis (`0.5.dp`) berwarna abu-obsidian redup yang memberi ritme visual bersih tanpa kotak kartu berulang.
   - Lagu yang sedang diputar ditandai dengan latar sorotan emas sangat tipis (`~8%`), teks judul emas lembut (*SoftGold*), serta indikator equalizer pada sampul album.

### Identitas Visual & Konsistensi Tema

- **Palet Warna**: Tetap menggunakan tema **Obsidian Gelap (`#09090D`)** dengan aksen **Emas Hangat (`#E6BA5E`)** dan teks **Putih Gading (`#F4F1EA`)**.
- **Tipografi**: *Playfair Display* untuk tajuk utama, *Plus Jakarta Sans* untuk judul lagu/artis, dan *JetBrains Mono* untuk durasi serta lencana format (`FLAC`, `WAV`, `MP3`).
- **Aksesibilitas**: Seluruh tombol ikon di header dan setiap baris lagu tetap mempertahankan area sentuh minimal `48.dp`.

---

## 3. Keputusan Desain & Trade-Off

- **Baris Datar Berpemisah vs Kartu Terpisah**:
  - *Pendekatan Dipilih*: Baris datar rapat dengan garis pemisah tipis khusus pada daftar utama (sambil tetap mempertahankan gaya kartu terstruktur di layar Daftar Putar dan Pengaturan agar hierarki tetap jelas).
  - *Mengapa*: Mengurangi kebisingan garis batas (*visual clutter*) dan menambah kepadatan lagu hingga ~30–40% lebih banyak dalam satu layar.
- **Pencarian Ekspansif vs Kolom Pencarian Statis**:
  - *Pendekatan Dipilih*: Tombol ikon pencarian di header yang membuka kolom input secara dinamis ketika dibutuhkan (dan tetap terbuka otomatis jika ada teks pencarian aktif).
  - *Mengapa*: Memberi ruang visual lebih luas pada daftar lagu tanpa menghilangkan akses cepat ke fitur pencarian.

---

## 4. Arsitektur Komponen *(Referensi Teknis)*

### Diagram Tata Letak Layar Utama Baru

```
┌──────────────────────────────────────────────────────────┐
│  Laras Audio                        [ 🔍 Cari ] [ ⚙ ]   │
│  3 Lagu Offline · MP3 · FLAC · WAV                       │
├──────────────────────────────────────────────────────────┤
│  (Opsional saat 🔍 diketuk: Kolom Pencarian Ekspansif)   │
├──────────────────────────────────────────────────────────┤
│  [ Semua ]  [ FLAC ]  [ WAV ]  [ MP3 ]  [ Lainnya ]      │
├──────────────────────────────────────────────────────────┤
│  3 Trek                          [ ▶ Putar ]  [ 🔀 Acak ]│
├──────────────────────────────────────────────────────────┤
│  [Art] Nocturne Emas ················ [♡] [⋮]            │
│        Kuartet Senyap · FLAC · 00:32 · LIRIK             │
│  ──────────────────────────────────────────────────────  │
│  [Art] Senja di Obsidian ············ [♡] [⋮]            │
│        Raka Pratama · MP3 · 00:30 · LIRIK                │
│  ──────────────────────────────────────────────────────  │
│  [Art] Gema Harmoni ················· [♡] [⋮]            │
│        Studio Laras · WAV · 00:28 · LIRIK                │
└──────────────────────────────────────────────────────────┘
```

### Pemetaan State & Interaksi

- **State Pencarian Ekspansif**: Dikelola di level tampilan layar utama (`isSearchExpanded`); ketika pengguna mengetuk ikon cari, animasi `AnimatedVisibility` menampilkan kolom input dan meminta fokus papan ketik; menutup pencarian otomatis mengosongkan `searchQuery` di ViewModel.
- **Penanganan Tombol Kembali (`BackHandler`)**: Jika bilah pencarian sedang terbuka dan pengguna menekan tombol kembali sistem, pencarian ditutup lebih dulu sebelum keluar dari aplikasi.
- **Konsistensi Fitur**: Seluruh interaksi baris lagu (Putar, Favorit, Tambah ke Antrean, Tambah ke Daftar Putar) tetap terhubung penuh ke ViewModel yang ada tanpa perubahan pada mesin pemutar Media3 maupun database Room.
