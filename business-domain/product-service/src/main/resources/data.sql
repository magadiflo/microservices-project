TRUNCATE TABLE products;
ALTER TABLE products AUTO_INCREMENT = 1;

INSERT INTO products(id, name, price, create_at)
VALUES(1, 'Panasonic', 800, NOW()),
(2, 'Sony', 700, NOW()),
(3, 'Apple', 1000, NOW()),
(4, 'Sony Notebook', 1000, NOW()),
(5, 'Hewlett Packard', 500, NOW()),
(6, 'Bianchi', 600, NOW()),
(7, 'Nike', 100, NOW()),
(8, 'Adidas', 200, NOW()),
(9, 'Reebok', 300, NOW());
