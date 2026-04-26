CREATE TABLE staff_members (
    demo_id VARCHAR(64) PRIMARY KEY,
    role VARCHAR(32) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(64) NOT NULL,
    active BOOLEAN NOT NULL,
    vehicle_description VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_staff_members_role ON staff_members (role);
