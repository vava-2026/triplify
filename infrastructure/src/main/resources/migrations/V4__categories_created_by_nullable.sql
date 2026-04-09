PRAGMA foreign_keys=off;

DROP TABLE categories;
CREATE TABLE categories (
    id TEXT NOT NULL PRIMARY KEY,
    created_by TEXT
        REFERENCES users(id)
            ON DELETE RESTRICT ON UPDATE CASCADE,
    name TEXT NOT NULL UNIQUE COLLATE NOCASE,
    name_sk TEXT NOT NULL UNIQUE COLLATE NOCASE,
    description TEXT,
    description_sk TEXT,
    emoji_unicode TEXT,
    color TEXT NOT NULL DEFAULT 'gray'
        CHECK (color IN ('gray', 'red_dark', 'red', 'rose', 'orange', 'amber', 'yellow', 'golden_brown', 'lime', 'green', 'indigo', 'violet', 'steel_blue', 'teal', 'blue', 'cyan', 'sage', 'brown', 'purple', 'pink'))
);

PRAGMA foreign_keys=on;
PRAGMA foreign_key_check;