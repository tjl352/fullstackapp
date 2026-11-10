-- Ebook store tables for the original-copy shop.
-- Assumes jhi_user already exists (admin = 1, user = 2).
--
-- The running app applies the same tables and sample rows through Liquibase
-- on startup (sample rows load in the dev/faker context only).
-- Run sections 1 and 2 on a database that does not already have these tables.
-- If the app has already started against this database, skip to section 3.

-- ---------------------------------------------------------------------------
-- 1. Create
-- ---------------------------------------------------------------------------

CREATE TABLE ebook (
    id BIGINT NOT NULL,
    format VARCHAR(10) NOT NULL,
    quantity INTEGER NOT NULL,
    title VARCHAR(255),
    price DECIMAL(21, 2),
    file_object_key VARCHAR(255),
    CONSTRAINT pk_ebook PRIMARY KEY (id),
    CONSTRAINT ck_ebook_format CHECK (format IN ('PDF', 'WORD')),
    CONSTRAINT ck_ebook_quantity CHECK (quantity = 1)
);

CREATE TABLE ebook_order (
    id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    status VARCHAR(20),
    placed_at TIMESTAMP,
    CONSTRAINT pk_ebook_order PRIMARY KEY (id),
    CONSTRAINT fk_ebook_order_user_id FOREIGN KEY (user_id) REFERENCES jhi_user (id)
);

CREATE TABLE ebook_order_item (
    id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    ebook_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(21, 2),
    CONSTRAINT pk_ebook_order_item PRIMARY KEY (id),
    CONSTRAINT ux_ebook_order_item_ebook_id UNIQUE (ebook_id),
    CONSTRAINT ck_ebook_order_item_quantity CHECK (quantity = 1),
    CONSTRAINT fk_ebook_order_item_order_id FOREIGN KEY (order_id) REFERENCES ebook_order (id),
    CONSTRAINT fk_ebook_order_item_ebook_id FOREIGN KEY (ebook_id) REFERENCES ebook (id)
);

-- ---------------------------------------------------------------------------
-- 2. Sample rows
--    101 and 102 are still for sale.
--    103 and 104 were bought by user (id 2) on paid order 201.
--    105 is on admin's order 202, which has no status, date, or price.
-- ---------------------------------------------------------------------------

INSERT INTO ebook (id, format, quantity, title, price, file_object_key) VALUES
    (101, 'PDF', 1, 'North Sea Field Notes', 12.00, 'originals/north-sea.pdf'),
    (102, 'WORD', 1, 'Kitchen Ledger', NULL, NULL),
    (103, 'PDF', 1, 'Single Pressing', 40.00, 'originals/single-pressing.pdf'),
    (104, 'WORD', 1, NULL, 8.50, 'originals/margin-notes.docx'),
    (105, 'WORD', 1, 'Untitled Draft', NULL, NULL);

INSERT INTO ebook_order (id, user_id, status, placed_at) VALUES
    (201, 2, 'PAID', TIMESTAMP '2026-06-01 15:00:00'),
    (202, 1, NULL, NULL);

INSERT INTO ebook_order_item (id, order_id, ebook_id, quantity, unit_price) VALUES
    (301, 201, 103, 1, 40.00),
    (302, 201, 104, 1, 8.50),
    (303, 202, 105, 1, NULL);

-- ---------------------------------------------------------------------------
-- 3. Queries to try
-- ---------------------------------------------------------------------------

-- Every original, and the order that sold it when there is one.
SELECT
    e.id,
    e.format,
    e.title,
    e.price,
    oi.order_id,
    oi.unit_price
FROM ebook e
LEFT JOIN ebook_order_item oi ON oi.ebook_id = e.id
ORDER BY e.id;

-- Copies still for sale (no order item).
SELECT e.id, e.format, e.title, e.price, e.file_object_key
FROM ebook e
LEFT JOIN ebook_order_item oi ON oi.ebook_id = e.id
WHERE oi.id IS NULL
ORDER BY e.id;

-- What each customer bought.
SELECT
    u.login,
    o.id AS order_id,
    o.status,
    o.placed_at,
    e.id AS ebook_id,
    e.format,
    e.title,
    oi.quantity,
    oi.unit_price
FROM ebook_order o
JOIN jhi_user u ON u.id = o.user_id
JOIN ebook_order_item oi ON oi.order_id = o.id
JOIN ebook e ON e.id = oi.ebook_id
ORDER BY u.login, o.id, oi.id;

-- How many originals exist of each type.
SELECT format, COUNT(*) AS copies
FROM ebook
GROUP BY format
ORDER BY format;

-- Paid order total from the line prices. Null prices are skipped by SUM.
SELECT o.id AS order_id, o.status, SUM(oi.unit_price) AS charged_total
FROM ebook_order o
JOIN ebook_order_item oi ON oi.order_id = o.id
GROUP BY o.id, o.status
ORDER BY o.id;

-- ---------------------------------------------------------------------------
-- 4. These three statements are expected to fail
-- ---------------------------------------------------------------------------

-- Second sale of an original that is already on an order.
-- INSERT INTO ebook_order_item (id, order_id, ebook_id, quantity, unit_price)
-- VALUES (399, 202, 103, 1, 1.00);

-- Quantity other than 1.
-- INSERT INTO ebook (id, format, quantity, title)
-- VALUES (199, 'PDF', 2, 'Should fail');

-- A type other than PDF or WORD.
-- INSERT INTO ebook (id, format, quantity, title)
-- VALUES (198, 'EPUB', 1, 'Should fail');
