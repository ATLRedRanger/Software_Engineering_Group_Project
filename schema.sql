DROP DATABASE IF EXISTS playfair_db;
CREATE DATABASE playfair_db;
USE playfair_db;

-- Cipher grids 
CREATE TABLE cipher_grids (
    id INT AUTO_INCREMENT PRIMARY KEY,
    grid_name VARCHAR(100) NOT NULL,
    grid_key VARCHAR(25) NOT NULL,
    omitted_letter CHAR(1) NOT NULL DEFAULT 'J',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Challenges table
CREATE TABLE challenges (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    ciphertext TEXT NOT NULL,
    hint VARCHAR(255) NOT NULL,
    hint2 VARCHAR(255),
    difficulty ENUM('easy', 'medium', 'hard') NOT NULL DEFAULT 'easy',
    omitted_letter CHAR(1) NOT NULL DEFAULT 'J',
    answer_key VARCHAR(100) NOT NULL,
    grid_id INT,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (grid_id) REFERENCES cipher_grids(id)
);

-- Users table
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    streak INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Public view
CREATE OR REPLACE VIEW challenges_public AS
SELECT
    id,
    title,
    ciphertext,
    hint,
    hint2,
    difficulty,
    omitted_letter,
    grid_id,
    created_at
FROM challenges
WHERE is_active = 1;