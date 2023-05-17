INSERT INTO usuarios(username, password, enabled, nombre, apellido, email) VALUES('martin', '$2a$10$mDbXJSRpVTKKvZqJI4gJcOE3RkPGXvHqTmY//JTjBfEePxZlN8Yle', true, 'Martín', 'Díaz', 'martin@magadiflo.com');
INSERT INTO usuarios(username, password, enabled, nombre, apellido, email) VALUES('admin', '$2a$10$rqfZCKzNIF6OiWgHtTGnIezjypeYS/EtABbXNea7FKqNQVSQgmNai', true, 'Admin', 'Admin', 'admin@magadiflo.com');

INSERT INTO roles(nombre) VALUES('ROLE_USER');
INSERT INTO roles(nombre) VALUES('ROLE_ADMIN');

INSERT INTO usuarios_roles(usuario_id, rol_id) VALUES(1,1);
INSERT INTO usuarios_roles(usuario_id, rol_id) VALUES(2,2);
INSERT INTO usuarios_roles(usuario_id, rol_id) VALUES(2,1);