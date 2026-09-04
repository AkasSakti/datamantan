# Mantanku — CRUD Web + Mobile

Aplikasi CRUD data "mantan terindah" (nama, no_hp, alamat), terdiri dari:
- **`web/`** — Laravel 11, CRUD via halaman web + REST API JSON.
- **`mobile/`** — Android native (Kotlin, minSdk 29 – targetSdk/compileSdk 36), CRUD via REST API ke Laravel.
- **`mantanku.sql`** — skrip pembuatan database `mantanku` dan tabel `mantan_terindah` (dengan data contoh).

Web dan mobile sama-sama membaca/menulis ke satu database MySQL yang sama (`mantanku`), web langsung lewat Eloquent, mobile lewat API Laravel (`/api/mantan`).

## 1. Database

1. Jalankan MySQL (mis. lewat Laragon).
2. Import `mantanku.sql`:
   ```
   mysql -u root < mantanku.sql
   ```
   Ini membuat database `mantanku`, tabel `mantan_terindah` (id, nama, no_hp, alamat, timestamps), dan 3 baris data contoh.

## 2. Web (Laravel 11)

```
cd web
composer install      # jika belum
php artisan migrate    # opsional, tabel sudah dibuat oleh mantanku.sql
php artisan serve
```
Buka `http://127.0.0.1:8000` → CRUD lewat browser di `/mantan`.

`.env` sudah diset ke `DB_DATABASE=mantanku`, `DB_USERNAME=root`, `DB_PASSWORD=` (default Laragon). Sesuaikan bila kredensial MySQL berbeda.

**REST API** (dipakai juga oleh app mobile):
| Method | Endpoint | Keterangan |
|---|---|---|
| GET | `/api/mantan` | daftar semua data |
| GET | `/api/mantan/{id}` | detail satu data |
| POST | `/api/mantan` | tambah (`nama`, `no_hp`, `alamat`) |
| PUT | `/api/mantan/{id}` | update |
| DELETE | `/api/mantan/{id}` | hapus |

Semua response berbentuk JSON `{ success, message, data }`.

## 3. Mobile (Android)

Buka folder `mobile/` di Android Studio (Sync Gradle otomatis mengunduh dependency). Aplikasi sudah dicoba build (`./gradlew assembleDebug`) dan **berhasil**.

- Base URL API ada di `app/src/main/java/com/datamantan/mantanku/data/RetrofitClient.kt`:
  - Default `http://10.0.2.2:8000/` → alamat ini otomatis mengarah ke `localhost:8000` milik komputer host **saat dijalankan di emulator Android**.
  - Untuk **HP fisik**, ganti ke IP LAN komputer, mis. `http://192.168.1.10:8000/`, lalu pastikan HP & komputer satu jaringan Wi-Fi dan `php artisan serve` dijalankan dengan `--host=0.0.0.0`.
- Pastikan `php artisan serve` (web Laravel) sedang berjalan sebelum membuka app mobile, karena mobile murni konsumsi API, tidak ada mode offline.
- Fitur: daftar mantan (RecyclerView + pull-to-refresh), tambah, edit, hapus (dengan konfirmasi), dan halaman detail.

## Catatan

- Route API dan route web sama-sama bernama `mantan.*` secara default oleh Laravel; route API sengaja diberi prefix nama `api.mantan.*` (`routes/api.php`) supaya tidak bentrok dengan `route('mantan.index')` dsb. yang dipakai di Blade view.
- PHP 8.5 di lingkungan ini mendeprecate `PDO::MYSQL_ATTR_SSL_CA` — sudah ditangani di `config/database.php` dan `public/index.php` agar tidak "membocorkan" warning ke response JSON API.
