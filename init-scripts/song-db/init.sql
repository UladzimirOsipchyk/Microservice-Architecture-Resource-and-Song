
CREATE TABLE song (
    id SERIAL PRIMARY KEY,
    resource_id BIGINT,
    name VARCHAR(255) NOT NULL,
    artist VARCHAR(255),
    album VARCHAR(255),
    length VARCHAR(50),
    year VARCHAR(4)
);