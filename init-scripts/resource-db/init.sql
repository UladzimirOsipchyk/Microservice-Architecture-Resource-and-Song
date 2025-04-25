
CREATE TABLE resources (
    id SERIAL PRIMARY KEY,
    fileName VARCHAR(50),
    file_url TEXT,
    storage_type VARCHAR(50)
--     data BYTEA NOT NULL
);
