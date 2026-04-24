CREATE TABLE customers (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(64) NOT NULL,
    address VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE menu_options (
    option_id VARCHAR(64) PRIMARY KEY,
    menu_group VARCHAR(32) NOT NULL,
    label VARCHAR(255) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE orders (
    id VARCHAR(36) PRIMARY KEY,
    customer_id VARCHAR(36) NOT NULL,
    status VARCHAR(32) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES customers (id)
);

CREATE TABLE order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id VARCHAR(36) NOT NULL,
    line_number INT NOT NULL,
    crust_option_id VARCHAR(64) NOT NULL,
    sauce_option_id VARCHAR(64) NOT NULL,
    cheese_option_id VARCHAR(64) NOT NULL,
    line_total DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT fk_order_items_crust FOREIGN KEY (crust_option_id) REFERENCES menu_options (option_id),
    CONSTRAINT fk_order_items_sauce FOREIGN KEY (sauce_option_id) REFERENCES menu_options (option_id),
    CONSTRAINT fk_order_items_cheese FOREIGN KEY (cheese_option_id) REFERENCES menu_options (option_id)
);

CREATE TABLE order_item_toppings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_item_id BIGINT NOT NULL,
    topping_option_id VARCHAR(64) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_order_item_toppings_item FOREIGN KEY (order_item_id) REFERENCES order_items (id),
    CONSTRAINT fk_order_item_toppings_option FOREIGN KEY (topping_option_id) REFERENCES menu_options (option_id)
);

CREATE TABLE payments (
    id VARCHAR(36) PRIMARY KEY,
    order_id VARCHAR(36) NOT NULL UNIQUE,
    payment_method VARCHAR(64) NOT NULL,
    billing_name VARCHAR(255) NOT NULL,
    card_last4 VARCHAR(4) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(32) NOT NULL,
    paid_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_payments_order FOREIGN KEY (order_id) REFERENCES orders (id)
);

CREATE TABLE delivery_assignments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id VARCHAR(36) NOT NULL UNIQUE,
    driver_name VARCHAR(255) NOT NULL,
    driver_phone VARCHAR(64) NOT NULL,
    driver_vehicle VARCHAR(255) NOT NULL,
    eta_minutes INT NOT NULL,
    assigned_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_delivery_assignments_order FOREIGN KEY (order_id) REFERENCES orders (id)
);
