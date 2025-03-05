
CREATE TABLE resources (
    id SERIAL PRIMARY KEY,
    file_url VARCHAR(255) NOT NULL,
    data BYTEA NOT NULL
);
