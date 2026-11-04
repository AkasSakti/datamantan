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

## 4. Deploy ke Hosting (Web Online + Mobile Pakai API Online)

Bagian ini untuk skenario **web Laravel sudah dionlinekan** (mis. di `https://mantanku.example.com`) supaya app mobile bisa dipakai dari HP mana saja tanpa harus satu Wi-Fi dengan laptop.

> Ganti `mantanku.example.com` di seluruh contoh di bawah dengan domain/subdomain milikmu.

### 4.1 Persiapan

- Hosting dengan **PHP ≥ 8.2**, ekstensi `pdo_mysql`, `mbstring`, `openssl`, `tokenizer`, `xml`, `ctype`, `json`, `bcmath`, `fileinfo`.
- Database **MySQL/MariaDB**.
- Domain/subdomain yang sudah diarahkan ke hosting, idealnya dengan **SSL (HTTPS)** aktif (cPanel biasanya menyediakan AutoSSL / Let's Encrypt gratis).
- Di laptop, siapkan folder `vendor/` agar bisa diupload bila hosting tidak punya Composer:
  ```
  cd web
  composer install --no-dev --optimize-autoloader
  ```

### 4.2 Opsi A — Shared Hosting (cPanel)

**1) Buat database**

1. cPanel → **MySQL® Databases** → buat database, mis. `user_mantanku`.
2. Buat user database, mis. `user_mantan`, beri password kuat.
3. **Add User To Database** → centang **ALL PRIVILEGES**.

**2) Import tabel**

Di shared hosting biasanya **tidak boleh** `CREATE DATABASE`, jadi sebelum import, hapus/komentari dua perintah ini di `mantanku.sql`:
```sql
CREATE DATABASE IF NOT EXISTS `mantanku` ... ;
USE `mantanku`;
```
Lalu cPanel → **phpMyAdmin** → pilih database `user_mantanku` → tab **Import** → pilih `mantanku.sql` → **Go**.

**3) Upload file Laravel**

Struktur yang disarankan (folder inti Laravel **di luar** `public_html` agar `.env` tidak bisa diakses publik):
```
/home/USERNAME/
├── mantanku/          ← isi folder web/ (app, bootstrap, config, vendor, storage, .env, dst.)
└── public_html/       ← isi folder web/public/ (index.php, .htaccess, favicon.ico, robots.txt)
```
1. Zip folder `web/` (sudah termasuk `vendor/`) → upload lewat **File Manager** ke `/home/USERNAME/` → Extract → rename jadi `mantanku`.
2. Pindahkan **isi** `mantanku/public/` ke `public_html/` (termasuk file tersembunyi `.htaccess`; aktifkan *Show Hidden Files* di File Manager).
3. Edit `public_html/index.php`, sesuaikan path ke folder inti:
   ```php
   if (file_exists($maintenance = __DIR__.'/../mantanku/storage/framework/maintenance.php')) {
       require $maintenance;
   }
   require __DIR__.'/../mantanku/vendor/autoload.php';
   $app = require_once __DIR__.'/../mantanku/bootstrap/app.php';
   ```

   > Kalau memakai **subdomain**, lebih rapi: arahkan *Document Root* subdomain langsung ke `/home/USERNAME/mantanku/public` (cPanel → **Domains** → Manage). Dengan cara ini `index.php` **tidak perlu diedit**.

**4) Atur `.env`** (di `/home/USERNAME/mantanku/.env`)

Salin dari `.env.example` bila belum ada, lalu ubah:
```env
APP_NAME=Mantanku
APP_ENV=production
APP_DEBUG=false
APP_URL=https://mantanku.example.com

DB_CONNECTION=mysql
DB_HOST=localhost
DB_PORT=3306
DB_DATABASE=user_mantanku
DB_USERNAME=user_mantan
DB_PASSWORD=passwordKuat123

# mantanku.sql hanya membuat tabel mantan_terindah,
# jadi session & cache disimpan di file saja:
SESSION_DRIVER=file
CACHE_STORE=file
QUEUE_CONNECTION=sync
```
`APP_KEY` wajib terisi. Jika tidak ada akses terminal, generate di laptop (`php artisan key:generate --show`) lalu tempel hasilnya ke `APP_KEY=`.

**5) Permission & cache**

- Folder `storage/` dan `bootstrap/cache/` harus bisa ditulis: permission `775` (File Manager → *Change Permissions*).
- Jika cPanel punya **Terminal**/SSH, jalankan:
  ```
  cd ~/mantanku
  php artisan key:generate      # jika APP_KEY masih kosong
  php artisan config:cache
  php artisan route:cache
  php artisan view:cache
  ```
  Setiap kali `.env` diubah, jalankan lagi `php artisan config:clear` (atau `config:cache`).

**6) Tes**

- `https://mantanku.example.com/mantan` → halaman CRUD web tampil.
- `https://mantanku.example.com/api/mantan` → muncul JSON `{"success":true,...}`.

### 4.3 Opsi B — VPS (Ubuntu + Nginx)

```bash
# 1. Paket
sudo apt update
sudo apt install -y nginx mysql-server git unzip \
  php8.3-fpm php8.3-mysql php8.3-mbstring php8.3-xml php8.3-curl php8.3-zip php8.3-bcmath
curl -sS https://getcomposer.org/installer | php && sudo mv composer.phar /usr/local/bin/composer

# 2. Database (mantanku.sql sekaligus membuat database `mantanku`)
sudo git clone https://github.com/AkasSakti/datamantan.git /var/www/datamantan
sudo mysql < /var/www/datamantan/mantanku.sql
sudo mysql -e "CREATE USER 'mantan'@'localhost' IDENTIFIED BY 'passwordKuat123';"
sudo mysql -e "GRANT ALL ON mantanku.* TO 'mantan'@'localhost'; FLUSH PRIVILEGES;"

# 3. Laravel
cd /var/www/datamantan/web
sudo composer install --no-dev --optimize-autoloader
sudo cp .env.example .env      # lalu edit seperti contoh .env di Opsi A (DB_USERNAME=mantan, dst.)
sudo php artisan key:generate
sudo php artisan config:cache && sudo php artisan route:cache && sudo php artisan view:cache
sudo chown -R www-data:www-data storage bootstrap/cache
```
> Mau session/cache disimpan di database? Jalankan `php artisan migrate --force` pada database **kosong** (tanpa import `mantanku.sql`, karena migration juga membuat tabel `mantan_terindah`), lalu isi data lewat web.

Konfigurasi Nginx `/etc/nginx/sites-available/mantanku`:
```nginx
server {
    listen 80;
    server_name mantanku.example.com;
    root /var/www/datamantan/web/public;
    index index.php;

    location / {
        try_files $uri $uri/ /index.php?$query_string;
    }

    location ~ \.php$ {
        include snippets/fastcgi-php.conf;
        fastcgi_pass unix:/run/php/php8.3-fpm.sock;
    }

    location ~ /\.(?!well-known) {
        deny all;
    }
}
```
```bash
sudo ln -s /etc/nginx/sites-available/mantanku /etc/nginx/sites-enabled/
sudo nginx -t && sudo systemctl reload nginx

# HTTPS gratis (Let's Encrypt)
sudo apt install -y certbot python3-certbot-nginx
sudo certbot --nginx -d mantanku.example.com
```

### 4.4 Arahkan App Mobile ke Server Online

**1) Ganti Base URL** di [`RetrofitClient.kt`](mobile/app/src/main/java/com/datamantan/mantanku/data/RetrofitClient.kt):
```kotlin
const val BASE_URL = "https://mantanku.example.com/"
```
- **Wajib diakhiri `/`** (Retrofit menolak base URL tanpa garis miring di akhir).
- Jangan tambahkan `api/` di sini — endpoint di `MantanApiService.kt` sudah memakai `api/mantan`.
- Jika web diletakkan di subfolder, mis. `https://domain.com/mantanku/`, isi base URL dengan path tersebut (tetap diakhiri `/`).

**2) Network security config** — [`network_security_config.xml`](mobile/app/src/main/res/xml/network_security_config.xml):
- Server sudah **HTTPS** → tidak perlu mengubah apa pun (HTTPS selalu diizinkan).
- Server masih **HTTP** (tanpa SSL) → Android 9+ memblokir HTTP ke domain yang tidak terdaftar, jadi tambahkan domainmu:
  ```xml
  <domain-config cleartextTrafficPermitted="true">
      <domain includeSubdomains="false">10.0.2.2</domain>
      <domain includeSubdomains="false">localhost</domain>
      <domain includeSubdomains="false">127.0.0.1</domain>
      <domain includeSubdomains="true">mantanku.example.com</domain>
  </domain-config>
  ```
  Sangat disarankan pakai HTTPS untuk versi yang dibagikan.

**3) (Opsional) Logging** — `HttpLoggingInterceptor` di `RetrofitClient.kt` di-set `Level.BODY` (mencatat seluruh request/response ke Logcat). Untuk build rilis, ubah ke `Level.NONE`.

**4) Build APK**
- Debug (cepat, untuk dicoba/dibagikan):
  ```
  cd mobile
  ./gradlew assembleDebug
  ```
  Hasil: `mobile/app/build/outputs/apk/debug/app-debug.apk`.
- Release (bertanda tangan): Android Studio → **Build → Generate Signed Bundle / APK…** → APK → buat/pilih keystore → *release*. Hasil: `mobile/app/build/outputs/apk/release/app-release.apk`.

Install APK di HP, buka app → data dari server online langsung tampil. Data yang ditambah lewat HP muncul juga di halaman web, dan sebaliknya.

### 4.5 Troubleshooting

| Gejala | Penyebab umum | Solusi |
|---|---|---|
| **500 Server Error** | `APP_KEY` kosong, permission `storage/` | Isi `APP_KEY`, chmod `775` `storage/` & `bootstrap/cache/`, cek `storage/logs/laravel.log` |
| **404** di `/mantan` atau `/api/mantan` | `.htaccess` tidak ikut terupload / document root salah | Pastikan `.htaccess` ada di `public_html`, atau document root mengarah ke folder `public` |
| **SQLSTATE[HY000] [1045] Access denied** | Kredensial DB salah | Cek `DB_DATABASE`, `DB_USERNAME`, `DB_PASSWORD` (di cPanel biasanya berawalan `username_`), lalu `php artisan config:clear` |
| **Table 'sessions' doesn't exist** | `SESSION_DRIVER=database` tapi tabelnya tidak ada | Set `SESSION_DRIVER=file` dan `CACHE_STORE=file` |
| Perubahan `.env` tidak berpengaruh | Config masih ter-cache | `php artisan config:clear` lalu `config:cache` |
| App: **"CLEARTEXT communication not permitted"** | Server HTTP tapi domain belum didaftarkan | Pakai HTTPS, atau tambahkan domain di `network_security_config.xml` (4.4 langkah 2) |
| App: **timeout / Unable to resolve host** | HP tanpa internet / domain salah | Buka `https://mantanku.example.com/api/mantan` di browser HP untuk memastikan API bisa diakses |
| App: error parsing JSON | Response berupa halaman HTML error, bukan JSON | Cek `storage/logs/laravel.log`; tes endpoint lewat browser/Postman |
| **"baseUrl must end in /"** | `BASE_URL` tanpa `/` di akhir | Tambahkan `/` di akhir `BASE_URL` |

**Keamanan:** API ini **tanpa autentikasi** — siapa pun yang tahu URL-nya bisa menambah/mengubah/menghapus data. Cukup untuk tugas/demo; untuk penggunaan nyata tambahkan autentikasi (mis. Laravel Sanctum, paketnya sudah terpasang). Jangan pernah meng-commit file `.env` ke GitHub.

## Catatan

- Route API dan route web sama-sama bernama `mantan.*` secara default oleh Laravel; route API sengaja diberi prefix nama `api.mantan.*` (`routes/api.php`) supaya tidak bentrok dengan `route('mantan.index')` dsb. yang dipakai di Blade view.
- PHP 8.5 di lingkungan ini mendeprecate `PDO::MYSQL_ATTR_SSL_CA` — sudah ditangani di `config/database.php` dan `public/index.php` agar tidak "membocorkan" warning ke response JSON API.
