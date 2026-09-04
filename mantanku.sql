-- =========================================================
-- Database: mantanku
-- Table   : mantan_terindah (id, nama, no_hp, alamat)
-- =========================================================

CREATE DATABASE IF NOT EXISTS `mantanku`
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE `mantanku`;

DROP TABLE IF EXISTS `mantan_terindah`;

CREATE TABLE `mantan_terindah` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `nama` VARCHAR(255) NOT NULL,
  `no_hp` VARCHAR(20) NOT NULL,
  `alamat` TEXT NULL,
  `created_at` TIMESTAMP NULL DEFAULT NULL,
  `updated_at` TIMESTAMP NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Sample data
INSERT INTO `mantan_terindah` (`nama`, `no_hp`, `alamat`, `created_at`, `updated_at`) VALUES
('Siti Aminah', '081234567890', 'Jl. Merdeka No. 1, Jember', NOW(), NOW()),
('Rina Wulandari', '081298765432', 'Jl. Sudirman No. 12, Jember', NOW(), NOW()),
('Dewi Lestari', '082112345678', 'Jl. Gajah Mada No. 5, Jember', NOW(), NOW());
