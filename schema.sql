CREATE DATABASE IF NOT EXISTS playfair_db;
USE playfair_db;

DROP TABLE IF EXISTS cipher_grids;
CREATE TABLE cipher_grids (
  id INT AUTO_INCREMENT PRIMARY KEY,
  cipher_key VARCHAR(100) NOT NULL,
  omitted_letter CHAR(1) NOT NULL,
  grid_string CHAR(25) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_key_omit (cipher_key, omitted_letter)
);

DROP TABLE IF EXISTS cipher_messages;
CREATE TABLE cipher_messages (
  id INT AUTO_INCREMENT PRIMARY KEY,
  cipher_key VARCHAR(100) NOT NULL,
  omitted_letter CHAR(1) NOT NULL,
  plaintext TEXT,
  ciphertext TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);