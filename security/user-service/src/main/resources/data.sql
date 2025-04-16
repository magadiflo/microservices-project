DELETE FROM users_roles;
DELETE FROM users;
DELETE FROM roles;

-- Password: 123456
INSERT INTO users(id, username, password, enabled, email)
VALUES(1, 'admin', '$2a$10$Yg5fqSv6vCGGyf4a6wlOXe9HZVV1EBtcBBKp1KgoF/Ec9j3j2oaPG', true, 'admin@gmail.com'),
(2, 'martin', '$2a$10$DkMNPHecq0S/T.dkC8Wrh.whOH.hvKzbtmMGs9r4NQcM.VvbIqdJy', true, 'martin@gmail.com');

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
