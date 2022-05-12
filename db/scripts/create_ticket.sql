CREATE TABLE IF NOT EXISTS ticket (
    id SERIAL PRIMARY KEY,
    session_id INT NOT NULL REFERENCES sessions(id) on delete cascade,
    rowr INT NOT NULL,
    cell INT NOT NULL,
    user_id INT NOT NULL REFERENCES users(id) on delete cascade,
    constraint unique_row_cell unique (session_id, rowr, cell)
);