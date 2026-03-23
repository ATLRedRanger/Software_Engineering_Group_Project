CREATE DATABASE IF NOT EXISTS playfair_db;
USE playfair_db;

CREATE TABLE IF NOT EXISTS cipher_grids (
    id INT AUTO_INCREMENT PRIMARY KEY,
    cipher_key VARCHAR(100) NOT NULL,
    omitted_letter CHAR(1) NOT NULL DEFAULT 'J',
    grid_string CHAR(25) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE IF NOT EXISTS challenges (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    ciphertext TEXT NOT NULL,
    hint VARCHAR(255) NOT NULL,
    omitted_letter CHAR(1) NOT NULL DEFAULT 'J',
    answer_key VARCHAR(100) NOT NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE OR REPLACE VIEW challenges_public AS
SELECT
    id,
    title,
    ciphertext,
    hint,
    omitted_letter,
    created_at
FROM challenges
WHERE is_active = 1;

