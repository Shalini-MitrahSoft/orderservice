CREATE TYPE order_status AS ENUM ('PENDING', 'CONFIRMED', 'REJECTED', 'CANCELLED');

CREATE TABLE orders (
    id          BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    status      order_status NOT NULL,
    total_amount NUMERIC(19, 2),
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP
);

CREATE TABLE order_items (
    id          BIGSERIAL PRIMARY KEY,
    order_id    BIGINT NOT NULL,
    product_id  BIGINT NOT NULL,
    quantity    INTEGER NOT NULL,
    unit_price  NUMERIC(19, 2),
    total_price NUMERIC(19, 2),
    CONSTRAINT fk_order FOREIGN KEY (order_id) REFERENCES orders (id)
);
