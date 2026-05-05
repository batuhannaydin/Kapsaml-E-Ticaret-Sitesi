DROP DATABASE IF EXISTS eticaret;
CREATE DATABASE eticaret CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE eticaret;

-- Kullanicilar tablosu
CREATE TABLE users (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    full_name   VARCHAR(100) NOT NULL,
    email       VARCHAR(150) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    phone       VARCHAR(20),
    address     VARCHAR(500),
    role        VARCHAR(20)  NOT NULL DEFAULT 'CUSTOMER',
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Kategoriler tablosu
CREATE TABLE categories (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE
);

-- Urunler tablosu
CREATE TABLE products (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    category_id  INT            NOT NULL,
    name         VARCHAR(200)   NOT NULL,
    description  TEXT,
    price        DECIMAL(10,2)  NOT NULL,
    stock        INT            NOT NULL DEFAULT 0,
    image_url    VARCHAR(500),
    is_active    BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories(id)
);

-- Siparisler tablosu
CREATE TABLE orders (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    user_id       INT            NOT NULL,
    order_date    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_amount  DECIMAL(10,2)  NOT NULL,
    status        VARCHAR(50)    NOT NULL DEFAULT 'BEKLEMEDE',
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Siparis kalemleri tablosu
CREATE TABLE order_items (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    order_id    INT            NOT NULL,
    product_id  INT            NOT NULL,
    quantity    INT            NOT NULL,
    unit_price  DECIMAL(10,2)  NOT NULL,
    subtotal    DECIMAL(10,2)  NOT NULL,
    CONSTRAINT fk_items_order   FOREIGN KEY (order_id)   REFERENCES orders(id),
    CONSTRAINT fk_items_product FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Varsayilan admin (sifre SHA-256 hashli: admin123)
INSERT INTO users (full_name, email, password, phone, address, role) VALUES
('Sistem Yoneticisi', 'admin@eticaret.com',
 '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9',
 '05550000000', 'Merkez', 'ADMIN');

-- Ornek musteri (sifre: 123456)
INSERT INTO users (full_name, email, password, phone, address, role) VALUES
('Ahmet Yilmaz', 'ahmet@example.com',
 '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92',
 '05551112233', 'Istanbul, Kadikoy', 'CUSTOMER');

-- Kategoriler
INSERT INTO categories (name, description, is_active) VALUES
('Telefon',    'Akilli telefonlar ve aksesuarlari',     TRUE),
('Bilgisayar', 'Dizustu ve masaustu bilgisayarlar',     TRUE),
('Aksesuar',   'Cesitli elektronik aksesuarlar',        TRUE),
('Kitap',      'Roman, ders kitabi ve daha fazlasi',    TRUE),
('Giyim',      'Erkek ve kadin giyim urunleri',         TRUE);

-- Urunler
INSERT INTO products (category_id, name, description, price, stock, image_url, is_active) VALUES
(1, 'iPhone 15 128GB',       'Apple iPhone 15, 128GB, Mavi',                   45000.00, 10, 'https://picsum.photos/seed/iphone15/400/400',  TRUE),
(1, 'Samsung Galaxy S24',    'Samsung Galaxy S24, 256GB, Siyah',                38000.00, 15, 'https://picsum.photos/seed/galaxy/400/400',    TRUE),
(1, 'Xiaomi Redmi Note 13',  'Xiaomi Redmi Note 13, 128GB',                      9500.00, 25, 'https://picsum.photos/seed/redmi/400/400',     TRUE),
(2, 'MacBook Air M2',        'Apple MacBook Air M2, 8GB/256GB',                 42000.00,  5, 'https://picsum.photos/seed/macbook/400/400',   TRUE),
(2, 'Lenovo ThinkPad E14',   'Lenovo ThinkPad E14, i5, 16GB RAM',               28000.00,  8, 'https://picsum.photos/seed/thinkpad/400/400',  TRUE),
(2, 'Asus TUF Gaming F15',   'Asus TUF F15, RTX 4060, 16GB',                    35000.00,  6, 'https://picsum.photos/seed/asus/400/400',      TRUE),
(3, 'Bluetooth Kulaklik',    'Kablosuz Bluetooth kulaklik, gurultu engelleme',   1500.00, 30, 'https://picsum.photos/seed/headphone/400/400', TRUE),
(3, 'Telefon Kilifi',        'Seffaf silikon telefon kilifi',                     150.00,  0, 'https://picsum.photos/seed/case/400/400',      TRUE),
(3, 'USB-C Kablo',           '2 metre orgu USB-C sarj kablosu',                    99.90, 100, 'https://picsum.photos/seed/cable/400/400',     TRUE),
(4, 'Sefiller',              'Victor Hugo - Sefiller (Iki Cilt)',                 280.00, 20, 'https://picsum.photos/seed/book1/400/400',     TRUE),
(4, 'Suc ve Ceza',           'Dostoyevski - Suc ve Ceza',                         180.00, 18, 'https://picsum.photos/seed/book2/400/400',     TRUE),
(5, 'Pamuklu Tisort',        'Erkek pamuklu basic tisort',                        350.00, 50, 'https://picsum.photos/seed/tshirt/400/400',    TRUE),
(5, 'Kot Pantolon',          'Slim fit erkek kot pantolon',                       650.00, 25, 'https://picsum.photos/seed/jeans/400/400',     TRUE);
