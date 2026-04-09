PRAGMA foreign_keys=off;

CREATE TABLE categories_new (
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

INSERT INTO categories_new (id, created_by, name, name_sk, description, description_sk, emoji_unicode, color)
SELECT id, created_by, name, name_sk, description, description_sk, emoji_unicode, color
FROM categories;

DROP TABLE categories;
ALTER TABLE categories_new RENAME TO categories;

CREATE TABLE tags_new (
    id TEXT NOT NULL PRIMARY KEY,
    user_id TEXT NOT NULL
      REFERENCES users(id)
          ON DELETE CASCADE ON UPDATE CASCADE,
    name TEXT NOT NULL COLLATE NOCASE,
    color TEXT NOT NULL DEFAULT 'gray'
      CHECK (color IN ('gray', 'red_dark', 'red', 'rose', 'orange', 'amber', 'yellow', 'golden_brown', 'lime', 'green', 'indigo', 'violet', 'steel_blue', 'teal', 'blue', 'cyan', 'sage', 'brown', 'purple', 'pink')),

    UNIQUE (user_id, name)
);

INSERT INTO tags_new (id, user_id, name, color)
SELECT id, user_id, name, color
FROM tags;

DROP TABLE tags;
ALTER TABLE tags_new RENAME TO tags;

CREATE INDEX idx_tags_user_id ON tags(user_id);

PRAGMA foreign_keys=on;
PRAGMA foreign_key_check;

