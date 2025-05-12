
CREATE TABLE storage (
    id SERIAL PRIMARY KEY,
    storage_type VARCHAR(255) NOT NULL,
    bucket_name VARCHAR(255),
    path VARCHAR(255)
);