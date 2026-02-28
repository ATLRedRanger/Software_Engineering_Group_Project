USE playfair_db;

INSERT INTO cipher_grids (cipher_key, omitted_letter, grid_string)
VALUES
('APPLE', 'J', 'APLEBCDFGHIKMONQRSTUVWXYZ'),
('KEYWORD', 'J', 'KEYWORDABCFGHILMNPQSTUVXZ');


INSERT INTO challenges (title, ciphertext, hint, omitted_letter, answer_key)
VALUES
(
  'Classic Playfair Cipher',
  'BM OD ZB XD NA BE KU DM UI XM MO UV IF',
  'Key is a phrase. Common Playfair example.',
  'J',
  'PLAYFAIR EXAMPLE'
),
(
  'Fruit Cipher',
  'KX JE YU RE BE ZW EH E',
  '5-letter fruit.',
  'J',
  'APPLE'
);

UPDATE challenges
SET answer_key = 'PLAYFAIR EXAMPLE'
WHERE id = 1;
