PRAGMA foreign_keys=off;

DROP TABLE badges_groups;
DROP TABLE badges;

CREATE TABLE badges_groups (
    id TEXT NOT NULL PRIMARY KEY,
    created_by TEXT
        REFERENCES users(id)
            ON DELETE SET NULL ON UPDATE CASCADE,
    name TEXT NOT NULL UNIQUE COLLATE NOCASE,
    name_sk TEXT NOT NULL UNIQUE COLLATE NOCASE,
    description TEXT,
    description_sk TEXT
);

CREATE TABLE badges (
    id TEXT NOT NULL PRIMARY KEY,
    created_by TEXT
        REFERENCES users(id)
            ON DELETE SET NULL ON UPDATE CASCADE,
    group_id TEXT NOT NULL
        REFERENCES badges_groups(id)
            ON DELETE CASCADE ON UPDATE CASCADE,
    image_id TEXT
        REFERENCES images(id)
            ON DELETE SET NULL ON UPDATE CASCADE,
    name TEXT NOT NULL COLLATE NOCASE,
    name_sk TEXT NOT NULL COLLATE NOCASE,
    description TEXT,
    description_sk TEXT,
    level INTEGER NOT NULL DEFAULT 1 CHECK (level >= 1),
    required_value INTEGER NOT NULL DEFAULT 0 CHECK (required_value >= 0),

    UNIQUE (group_id, name, level)
);
CREATE INDEX idx_badges_group_id ON badges(group_id);

PRAGMA foreign_keys=on;
PRAGMA foreign_key_check;