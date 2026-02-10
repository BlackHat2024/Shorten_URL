CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,

    CONSTRAINT uk_users_email UNIQUE (email)

);

CREATE TABLE short_urls (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    short_url VARCHAR(20) NOT NULL,
    original_url VARCHAR(2048) NOT NULL,
    created_at DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expiration_time DATETIME  NOT NULL,
    click_count BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(50),

    CONSTRAINT uk_shortened_urls_short_code UNIQUE (short_url)
);
