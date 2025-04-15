TRUNCATE TABLE users RESTART IDENTITY;

INSERT INTO users(id, username, password, enabled, email)
VALUES(1, 'martin', '123456', true, 'martin@gmail.com'),
(2, 'admin', '123456', true, 'admin@gmail.com');

-- Actualiza la secuencia para que el siguiente valor sea 3
SELECT setval('users_id_seq', 2, true);
