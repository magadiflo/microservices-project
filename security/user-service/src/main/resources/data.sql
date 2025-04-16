DELETE FROM users_roles;
DELETE FROM users;
DELETE FROM roles;

INSERT INTO users(id, username, password, enabled, email)
VALUES(1, 'admin', '123456', true, 'admin@gmail.com'),
(2, 'martin', '123456', true, 'martin@gmail.com');

INSERT INTO roles(id, name)
VALUES(1, 'ROLE_ADMIN'),
(2, 'ROLE_USER');

INSERT INTO users_roles(user_id, role_id)
VALUES(1, 1),
(1, 2),
(2, 2);

-- Actualiza la secuencia para que el siguiente valor sea 3
SELECT setval('users_id_seq', 2, true);
SELECT setval('roles_id_seq', 2, true);
