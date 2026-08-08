CREATE TABLE IF NOT EXISTS price_range (
    id          INT PRIMARY KEY,

    min         DECIMAL(10, 2) NOT NULL
        CONSTRAINT chk_price_min CHECK (min >= 0),

    max         DECIMAL(10, 2) NOT NULL
        CONSTRAINT chk_price_max CHECK (max >= min),

    description VARCHAR(50) CHECK (description <> '')
);

CREATE TABLE IF NOT EXISTS cuisine_type (
    id          INT PRIMARY KEY,
    description VARCHAR(100) NOT NULL CHECK (description <> '')
);

CREATE TABLE IF NOT EXISTS awards (
    id          INT PRIMARY KEY,
    description VARCHAR(100) NOT NULL CHECK (description <> '')
);

CREATE TABLE IF NOT EXISTS services_and_facilities (
  id    INT PRIMARY KEY,
  description   TEXT NOT NULL CHECK (description <> '')
);

CREATE TABLE IF NOT EXISTS location (
    latitude    DOUBLE PRECISION NOT NULL CHECK (latitude BETWEEN -90.0 AND 90.0),
    longitude   DOUBLE PRECISION NOT NULL CHECK (longitude BETWEEN -180.0 AND 180.0),
    city        VARCHAR(100)     CHECK (city <> ''),
    country     CHAR(2)          CHECK (country ~ '^[A-Z]{2}$'),
    address     VARCHAR(255)     CHECK (address <> ''),

    PRIMARY KEY (latitude, longitude)
);



CREATE TABLE IF NOT EXISTS "user" (
    id                  UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    username            VARCHAR(50)  NOT NULL UNIQUE CHECK (username <> ''),
    hash                VARCHAR(255) NOT NULL CHECK (hash <> ''),
    salt                VARCHAR(255) NOT NULL CHECK (salt <> ''),
    first_name          VARCHAR(100) CHECK (first_name <> ''),
    last_name           VARCHAR(100) CHECK (last_name <> ''),

    -- A user cannot be born in the future
    birth_date          DATE CHECK (birth_date <= CURRENT_DATE),
    latitude            DOUBLE PRECISION,
    longitude           DOUBLE PRECISION,
    is_owner            BOOLEAN      NOT NULL,
    registered_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (latitude, longitude) REFERENCES location (latitude, longitude)
);

CREATE TABLE IF NOT EXISTS restaurant (
    id           UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    owner_id     UUID                  NOT NULL,

    name         VARCHAR(100) NOT NULL CHECK (name <> ''),
    description  TEXT CHECK (description <> ''),
    web_url      TEXT CHECK (web_url <> ''),
    phone_number TEXT CHECK (phone_number <> ''),
    price_range  INT,

    award        INT NOT NULL DEFAULT 0,
    green_star   BOOLEAN NOT NULL DEFAULT false,

    has_delivery BOOLEAN NOT NULL DEFAULT false,
    has_booking  BOOLEAN NOT NULL DEFAULT false,

    latitude     DOUBLE PRECISION,
    longitude    DOUBLE PRECISION,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP CHECK (created_at <= CURRENT_TIMESTAMP),


    FOREIGN KEY (award) REFERENCES awards(id),
    FOREIGN KEY (owner_id) REFERENCES "user"(id),
    FOREIGN KEY (price_range) REFERENCES price_range (id),
    FOREIGN KEY (latitude, longitude) REFERENCES location (latitude, longitude)
);


CREATE TABLE IF NOT EXISTS user_favorites (
    user_id       UUID NOT NULL,
    restaurant_id UUID NOT NULL,

    PRIMARY KEY (user_id, restaurant_id),
    FOREIGN KEY (user_id) REFERENCES "user"(id),
    FOREIGN KEY (restaurant_id) REFERENCES restaurant (id)
);

CREATE TABLE IF NOT EXISTS restaurant_cuisine (
    restaurant_id UUID NOT NULL,
    type  INT  NOT NULL,

    PRIMARY KEY (restaurant_id, type),
    FOREIGN KEY (restaurant_id) REFERENCES restaurant (id),
    FOREIGN KEY (type) REFERENCES cuisine_type (id)
);


CREATE TABLE IF NOT EXISTS restaurant_services (
    restaurant_id UUID NOT NULL,
    service     INT NOT NULL,

    PRIMARY KEY (restaurant_id,service),

    FOREIGN KEY (restaurant_id) REFERENCES restaurant (id),
    FOREIGN KEY (service) REFERENCES services_and_facilities(id)
);


CREATE TABLE IF NOT EXISTS review (
    id            UUID PRIMARY KEY   DEFAULT gen_random_uuid(),
    customer_id   UUID      NOT NULL,
    restaurant_id UUID      NOT NULL,
    rating        INT       NOT NULL CHECK (rating BETWEEN 1 AND 5),
    text          TEXT      NOT NULL CHECK (text <> ''),
    response      TEXT CHECK (response <> ''),
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    responded_at  TIMESTAMP,


    UNIQUE (customer_id, restaurant_id),
    FOREIGN KEY (customer_id) REFERENCES "user" (id),
    FOREIGN KEY (restaurant_id) REFERENCES restaurant (id),

    CONSTRAINT chk_response_after_review CHECK (responded_at IS NULL OR responded_at >= created_at),

    -- Ensures a review text always has a timestamp, and a timestamp always has text
    CONSTRAINT chk_review_completeness CHECK (
        (text IS NULL AND created_at IS NULL) OR
        (text IS NOT NULL AND created_at IS NOT NULL)
    ),

    -- Ensures a response text always has a timestamp, and a timestamp always has text
    CONSTRAINT chk_response_completeness CHECK (
        (response IS NULL AND responded_at IS NULL) OR
        (response IS NOT NULL AND responded_at IS NOT NULL)
    )
);