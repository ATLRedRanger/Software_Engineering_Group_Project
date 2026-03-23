USE playfair_db;

-- Sample cipher grids
INSERT INTO cipher_grids (cipher_key, omitted_letter, grid_string)
VALUES
('APPLE', 'J', 'APLEBCDFGHIKMONQRSTUVWXYZ'),
('KEYWORD', 'J', 'KEYWORDABCFGHILMNPQSTUVXZ');

-- Sample challenges
INSERT INTO challenges
(title, ciphertext, hint, hint2, omitted_letter, answer_key, difficulty, is_active)
VALUES
(
  'Classic Playfair Cipher',
  'BM OD ZB XD NA BE KU DM UI XM MO UV IF',
  'Key is a phrase. Common Playfair example.',
  'Classic Playfair demo phrase',
  'J',
  'PLAYFAIR EXAMPLE',
  'easy',
  1
),
(
  'Fruit Cipher',
  'KX JE YU RE BE ZW EH E',
  '5-letter fruit.',
  'A common red fruit',
  'J',
  'APPLE',
  'easy',
  1
),
(
  'Puzzle Cipher',
  'AB CD EF GH IJ',
  'Think about row shifts',
  'Classic Playfair rule',
  'J',
  'SECRET',
  'medium',
  1
),
(
  'Double Rectangle Cipher',
  'ZX CV BN ML KJ',
  'Multiple rectangle swaps',
  'Look at column rules',
  'J',
  'PUZZLE',
  'hard',
  1
),
(
  'Intermediate Cipher',
  'LM NO PQ RS TU',
  'Look at digraph pairs',
  'Focus on row movement',
  'J',
  'TARGET',
  'medium',
  1
),
(
  'Hidden Phrase Cipher',
  'GH IJ KL MN OP',
  'Pairs matter',
  'Use rectangle swaps',
  'J',
  'MESSAGE',
  'medium',
  1
),
(
  'Advanced Cipher',
  'QR ST UV WX YZ',
  'Harder digraph pattern',
  'Think about row and rectangle rules together',
  'J',
  'ENIGMA',
  'hard',
  1
);
