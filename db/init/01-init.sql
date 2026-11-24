-- Une base MySQL = un "schéma". Un utilisateur par service.
CREATE DATABASE IF NOT EXISTS reservations_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS suivi_db        CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS inventaire_db   CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS usagers_db      CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'reservations_user'@'%' IDENTIFIED BY 'reservations_pass';
CREATE USER IF NOT EXISTS 'inventaire_user'@'%'   IDENTIFIED BY 'inventaire_pass';
CREATE USER IF NOT EXISTS 'usagers_user'@'%'      IDENTIFIED BY 'usagers_pass';

-- Réservations accède à ses deux bases (écriture + projection de suivi)
GRANT ALL PRIVILEGES ON reservations_db.* TO 'reservations_user'@'%';
GRANT ALL PRIVILEGES ON suivi_db.*        TO 'reservations_user'@'%';
GRANT ALL PRIVILEGES ON inventaire_db.*   TO 'inventaire_user'@'%';
GRANT ALL PRIVILEGES ON usagers_db.*      TO 'usagers_user'@'%';
FLUSH PRIVILEGES;
