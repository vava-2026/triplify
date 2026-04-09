PRAGMA foreign_keys=off;

DROP TABLE countries;
CREATE TABLE countries (
    id TEXT NOT NULL PRIMARY KEY,
    created_by TEXT
        REFERENCES users(id)
            ON DELETE RESTRICT ON UPDATE CASCADE,
    name TEXT NOT NULL UNIQUE COLLATE NOCASE,
    name_sk TEXT NOT NULL UNIQUE COLLATE NOCASE,
    emoji_unicode TEXT NOT NULL,
    is_available INTEGER NOT NULL DEFAULT 1
        CHECK (is_available IN (0, 1))
);

PRAGMA foreign_keys=on;
PRAGMA foreign_key_check;