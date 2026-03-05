-- Foreign key enable + performance-related pragmas
PRAGMA foreign_keys = ON;
PRAGMA journal_mode = WAL;
PRAGMA synchronous  = NORMAL;

-- Init SpatiaLite for geo
SELECT InitSpatialMetaData(1);

-- ENUMS (enforced via CHECK)
-- ROLE_ENUM  : 'user' | 'pro-user' | 'configuration manager'
-- STATUS_ENUM: 'planned' | 'ongoing' | 'visited' | 'canceled'
-- COLOR_ENUM : 'red' | 'orange' | 'yellow' | 'green' | 'blue' | 'purple' | 'pink' | 'gray'

CREATE TABLE images (
    id          TEXT NOT NULL PRIMARY KEY,
    url         TEXT NOT NULL UNIQUE,
    storage_key TEXT NOT NULL UNIQUE,
    description TEXT,
    uploaded_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now'))
        CHECK (datetime(uploaded_at) IS NOT NULL)
);

CREATE TABLE users (
    id              TEXT NOT NULL PRIMARY KEY,
    username        TEXT NOT NULL UNIQUE COLLATE NOCASE,
    email           TEXT NOT NULL UNIQUE COLLATE NOCASE,
    password_hash   TEXT NOT NULL,
    role            TEXT NOT NULL DEFAULT 'user'
       CHECK (role IN ('user', 'pro-user', 'configuration manager')),
    avatar_image_id TEXT
       REFERENCES images(id)
           ON DELETE SET NULL ON UPDATE CASCADE,
    created_at      TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now'))
       CHECK (datetime(created_at) IS NOT NULL),
    updated_at      TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now'))
       CHECK (datetime(updated_at) IS NOT NULL)
);

CREATE TABLE emotions (
    id         TEXT NOT NULL PRIMARY KEY,
    created_by TEXT NOT NULL
      REFERENCES users(id)
          ON DELETE RESTRICT ON UPDATE CASCADE,
    name       TEXT NOT NULL UNIQUE COLLATE NOCASE,
    name_sk    TEXT NOT NULL UNIQUE COLLATE NOCASE,
    emoji TEXT NOT NULL
);

CREATE TABLE categories (
    id             TEXT NOT NULL PRIMARY KEY,
    created_by     TEXT NOT NULL
        REFERENCES users(id)
            ON DELETE RESTRICT ON UPDATE CASCADE,
    name           TEXT NOT NULL UNIQUE COLLATE NOCASE,
    name_sk        TEXT NOT NULL UNIQUE COLLATE NOCASE,
    description    TEXT,
    description_sk TEXT,
    emoji TEXT
    color TEXT NOT NULL DEFAULT 'gray'
      CHECK (color IN ('red', 'orange', 'yellow', 'green', 'blue', 'purple', 'pink', 'gray')),
);

CREATE TABLE countries (
    id         TEXT    NOT NULL PRIMARY KEY,
    created_by TEXT    NOT NULL
       REFERENCES users(id)
           ON DELETE RESTRICT ON UPDATE CASCADE,
    name       TEXT    NOT NULL UNIQUE COLLATE NOCASE,
    name_sk    TEXT    NOT NULL UNIQUE COLLATE NOCASE,
    emoji TEXT NOT NULL,
    is_banned  INTEGER NOT NULL DEFAULT 0
       CHECK (is_banned IN (0, 1))
);

CREATE TABLE tags (
    id      TEXT NOT NULL PRIMARY KEY,
    user_id TEXT NOT NULL
      REFERENCES users(id)
          ON DELETE CASCADE ON UPDATE CASCADE,
    name    TEXT NOT NULL COLLATE NOCASE,
    color   TEXT NOT NULL DEFAULT 'gray'
      CHECK (color IN ('red', 'orange', 'yellow', 'green', 'blue', 'purple', 'pink', 'gray')),

    UNIQUE (user_id, name)
);
CREATE INDEX idx_tags_user_id ON tags(user_id);

CREATE TABLE places (
    id             TEXT NOT NULL PRIMARY KEY,
    user_id        TEXT NOT NULL
        REFERENCES users(id)
            ON DELETE RESTRICT ON UPDATE CASCADE,
    country_id     TEXT NOT NULL
        REFERENCES countries(id)
            ON DELETE RESTRICT ON UPDATE CASCADE,
    cover_image_id TEXT
        REFERENCES images(id)
            ON DELETE SET NULL ON UPDATE CASCADE,
    title          TEXT NOT NULL COLLATE NOCASE,
    description    TEXT,
    latitude       REAL NOT NULL CHECK (latitude  BETWEEN -90  AND  90),
    longitude      REAL NOT NULL CHECK (longitude BETWEEN -180 AND 180),
    created_at     TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now'))
        CHECK (datetime(created_at) IS NOT NULL),
    updated_at     TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now'))
        CHECK (datetime(updated_at) IS NOT NULL)
);
CREATE INDEX idx_places_user_id       ON places(user_id);
CREATE INDEX idx_places_country_id    ON places(country_id);

-- Adding column to the Places table as SpatiaLite specific data
SELECT AddGeometryColumn('places', 'geom', 4326, 'POINT', 'XY');
SELECT CreateSpatialIndex('places', 'geom');

CREATE TABLE trips (
    id          TEXT NOT NULL PRIMARY KEY,
    user_id     TEXT NOT NULL
       REFERENCES users(id)
           ON DELETE RESTRICT ON UPDATE CASCADE,
    category_id TEXT
       REFERENCES categories(id)
           ON DELETE SET NULL ON UPDATE CASCADE,
    country_id  TEXT
       REFERENCES countries(id)
           ON DELETE SET NULL ON UPDATE CASCADE,
    title       TEXT NOT NULL COLLATE NOCASE,
    description TEXT,
    status      TEXT NOT NULL DEFAULT 'planned'
       CHECK (status IN ('planned', 'ongoing', 'visited', 'canceled')),
    started_at  TEXT CHECK (started_at IS NULL OR datetime(started_at) IS NOT NULL),
    ended_at    TEXT CHECK (ended_at   IS NULL OR datetime(ended_at)   IS NOT NULL),
    created_at  TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now'))
       CHECK (datetime(created_at) IS NOT NULL),
    updated_at  TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now'))
       CHECK (datetime(updated_at) IS NOT NULL),

    CHECK (ended_at IS NULL OR started_at IS NULL OR ended_at >= started_at)
);
CREATE INDEX idx_trips_user_id     ON trips(user_id);
CREATE INDEX idx_trips_category_id ON trips(category_id);
CREATE INDEX idx_trips_country_id  ON trips(country_id);

CREATE TABLE routes (
    id             TEXT NOT NULL PRIMARY KEY,
    user_id        TEXT NOT NULL
        REFERENCES users(id)
            ON DELETE RESTRICT ON UPDATE CASCADE,
    cover_image_id TEXT
        REFERENCES images(id)
            ON DELETE SET NULL ON UPDATE CASCADE,
    title          TEXT NOT NULL COLLATE NOCASE,
    description    TEXT,
    total_length   REAL CHECK (total_length >= 0),
    created_at     TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now'))
        CHECK (datetime(created_at) IS NOT NULL),
    updated_at     TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now'))
        CHECK (datetime(updated_at) IS NOT NULL)
);
CREATE INDEX idx_routes_user_id        ON routes(user_id);
CREATE INDEX idx_routes_cover_image_id ON routes(cover_image_id);

CREATE TABLE trip_routes (
    id         TEXT    NOT NULL PRIMARY KEY,
    trip_id    TEXT    NOT NULL
     REFERENCES trips(id)
         ON DELETE CASCADE ON UPDATE CASCADE,
    route_id   TEXT    NOT NULL
     REFERENCES routes(id)
         ON DELETE RESTRICT ON UPDATE CASCADE,
    "order"    INTEGER NOT NULL DEFAULT 0 CHECK ("order" >= 0),
    status     TEXT    NOT NULL DEFAULT 'planned'
     CHECK (status IN ('planned', 'ongoing', 'visited', 'canceled')),
    started_at TEXT    CHECK (started_at IS NULL OR datetime(started_at) IS NOT NULL),
    ended_at   TEXT    CHECK (ended_at   IS NULL OR datetime(ended_at)   IS NOT NULL),
    created_at TEXT    NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now'))
     CHECK (datetime(created_at) IS NOT NULL),
    updated_at TEXT    NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now'))
     CHECK (datetime(updated_at) IS NOT NULL),

    UNIQUE (trip_id, route_id),
    CHECK (ended_at IS NULL OR started_at IS NULL OR ended_at >= started_at)
);
CREATE INDEX idx_trip_routes_trip_id  ON trip_routes(trip_id);
CREATE INDEX idx_trip_routes_route_id ON trip_routes(route_id);

CREATE TABLE trip_places (
    id         TEXT NOT NULL PRIMARY KEY,
    trip_id    TEXT NOT NULL
     REFERENCES trips(id)
         ON DELETE CASCADE ON UPDATE CASCADE,
    place_id   TEXT NOT NULL
     REFERENCES places(id)
         ON DELETE RESTRICT ON UPDATE CASCADE,
    visit_date TEXT CHECK (visit_date IS NULL OR datetime(visit_date) IS NOT NULL),  -- added per ERD
    created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now'))
     CHECK (datetime(created_at) IS NOT NULL),
    updated_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now'))
     CHECK (datetime(updated_at) IS NOT NULL),

    UNIQUE (trip_id, place_id)
);
CREATE INDEX idx_trip_places_trip_id  ON trip_places(trip_id);
CREATE INDEX idx_trip_places_place_id ON trip_places(place_id);

CREATE TABLE route_places (
    id         TEXT    NOT NULL PRIMARY KEY,
    route_id   TEXT    NOT NULL
      REFERENCES routes(id)
          ON DELETE CASCADE ON UPDATE CASCADE,
    place_id   TEXT    NOT NULL
      REFERENCES places(id)
          ON DELETE RESTRICT ON UPDATE CASCADE,
    "order"    INTEGER NOT NULL DEFAULT 0 CHECK ("order" >= 0),
    created_at TEXT    NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now'))
      CHECK (datetime(created_at) IS NOT NULL),
    updated_at TEXT    NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now'))
      CHECK (datetime(updated_at) IS NOT NULL),

    UNIQUE (route_id, place_id)
);
CREATE INDEX idx_route_places_route_id ON route_places(route_id);
CREATE INDEX idx_route_places_place_id ON route_places(place_id);

CREATE TABLE stories (
    id            TEXT NOT NULL PRIMARY KEY,
    user_id       TEXT NOT NULL
     REFERENCES users(id)
         ON DELETE RESTRICT ON UPDATE CASCADE,
--     trip_id       TEXT
--      REFERENCES trips(id)
--          ON DELETE SET NULL ON UPDATE CASCADE,
    trip_place_id TEXT
     REFERENCES trip_places(id)
         ON DELETE SET NULL ON UPDATE CASCADE,
    emotion_id    TEXT
     REFERENCES emotions(id)
         ON DELETE SET NULL ON UPDATE CASCADE,
    title         TEXT NOT NULL COLLATE NOCASE,
    description   TEXT,
    story_time    TEXT NOT NULL
     CHECK (datetime(story_time) IS NOT NULL),
    created_at    TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now'))
     CHECK (datetime(created_at) IS NOT NULL)
);
CREATE INDEX idx_stories_user_id       ON stories(user_id);
CREATE INDEX idx_stories_trip_id       ON stories(trip_id);
CREATE INDEX idx_stories_trip_place_id ON stories(trip_place_id);
CREATE INDEX idx_stories_emotion_id    ON stories(emotion_id);

CREATE TABLE badges_groups (
    id             TEXT NOT NULL PRIMARY KEY,
    created_by     TEXT NOT NULL
       REFERENCES users(id)
           ON DELETE RESTRICT ON UPDATE CASCADE,
    name           TEXT NOT NULL UNIQUE COLLATE NOCASE,
    name_sk        TEXT NOT NULL UNIQUE COLLATE NOCASE,
    description    TEXT,
    description_sk TEXT
);

CREATE TABLE badges (
    id             TEXT    NOT NULL PRIMARY KEY,
    created_by     TEXT    NOT NULL
        REFERENCES users(id)
            ON DELETE RESTRICT ON UPDATE CASCADE,
    group_id       TEXT    NOT NULL
        REFERENCES badges_groups(id)
            ON DELETE RESTRICT ON UPDATE CASCADE,
    image_id       TEXT
        REFERENCES images(id)
            ON DELETE SET NULL ON UPDATE CASCADE,
    name           TEXT    NOT NULL COLLATE NOCASE,
    name_sk        TEXT    NOT NULL COLLATE NOCASE,
    description    TEXT,
    description_sk TEXT,
    level          INTEGER NOT NULL DEFAULT 1 CHECK (level >= 1),
    required_value INTEGER NOT NULL DEFAULT 0 CHECK (required_value >= 0),

    UNIQUE (group_id, name, level)
);
CREATE INDEX idx_badges_group_id   ON badges(group_id);

CREATE TABLE user_badges (
    user_id        TEXT    NOT NULL
     REFERENCES users(id)
         ON DELETE CASCADE ON UPDATE CASCADE,
    badge_id       TEXT    NOT NULL
     REFERENCES badges(id)
         ON DELETE CASCADE ON UPDATE CASCADE,
    progress_value INTEGER NOT NULL DEFAULT 0 CHECK (progress_value >= 0),
    is_unlocked    INTEGER NOT NULL DEFAULT 0 CHECK (is_unlocked IN (0, 1)),

    PRIMARY KEY (user_id, badge_id)
);
CREATE INDEX idx_user_badges_badge_id ON user_badges(badge_id);

CREATE TABLE trip_tags (
    trip_id TEXT NOT NULL
       REFERENCES trips(id)
           ON DELETE CASCADE ON UPDATE CASCADE,
    tag_id  TEXT NOT NULL
       REFERENCES tags(id)
           ON DELETE CASCADE ON UPDATE CASCADE,

    PRIMARY KEY (trip_id, tag_id)
);
CREATE INDEX idx_trip_tags_tag_id ON trip_tags(tag_id);

CREATE TABLE story_tags (
    story_id TEXT NOT NULL
        REFERENCES stories(id)
            ON DELETE CASCADE ON UPDATE CASCADE,
    tag_id   TEXT NOT NULL
        REFERENCES tags(id)
            ON DELETE CASCADE ON UPDATE CASCADE,

    PRIMARY KEY (story_id, tag_id)
);
CREATE INDEX idx_story_tags_tag_id ON story_tags(tag_id);

CREATE TABLE trip_images (
    trip_id  TEXT NOT NULL
     REFERENCES trips(id)
         ON DELETE CASCADE ON UPDATE CASCADE,
    image_id TEXT NOT NULL
     REFERENCES images(id)
         ON DELETE CASCADE ON UPDATE CASCADE,

    PRIMARY KEY (trip_id, image_id)
);
CREATE INDEX idx_trip_images_trip_id ON trip_images(trip_id);

CREATE TABLE trip_places_images (
    trip_place_id TEXT NOT NULL
        REFERENCES trip_places(id)
            ON DELETE CASCADE ON UPDATE CASCADE,
    image_id      TEXT NOT NULL
        REFERENCES images(id)
            ON DELETE CASCADE ON UPDATE CASCADE,

    PRIMARY KEY (trip_place_id, image_id)
);
CREATE INDEX idx_trip_places_images_trip_place_id ON trip_places_images(trip_place_id);

CREATE TABLE trip_route_images (
    trip_route_id TEXT NOT NULL
       REFERENCES trip_routes(id)
           ON DELETE CASCADE ON UPDATE CASCADE,
    image_id      TEXT NOT NULL
       REFERENCES images(id)
           ON DELETE CASCADE ON UPDATE CASCADE,

    PRIMARY KEY (trip_route_id, image_id)
);
CREATE INDEX idx_trip_route_images_trip_route_id ON trip_route_images(trip_route_id);

CREATE TABLE story_images (
    story_id TEXT NOT NULL
      REFERENCES stories(id)
          ON DELETE CASCADE ON UPDATE CASCADE,
    image_id TEXT NOT NULL
      REFERENCES images(id)
          ON DELETE CASCADE ON UPDATE CASCADE,

    PRIMARY KEY (story_id, image_id)
);
CREATE INDEX idx_story_images_story_id ON story_images(story_id);

-- Updated_at triggers
CREATE TRIGGER trg_users_updated_at
    AFTER UPDATE ON users FOR EACH ROW
    WHEN OLD.updated_at = NEW.updated_at
BEGIN
    UPDATE users SET updated_at = strftime('%Y-%m-%dT%H:%M:%fZ', 'now')
    WHERE id = OLD.id;
END;

CREATE TRIGGER trg_places_updated_at
    AFTER UPDATE ON places FOR EACH ROW
    WHEN OLD.updated_at = NEW.updated_at
BEGIN
    UPDATE places SET updated_at = strftime('%Y-%m-%dT%H:%M:%fZ', 'now')
    WHERE id = OLD.id;
END;

CREATE TRIGGER trg_trips_updated_at
    AFTER UPDATE ON trips FOR EACH ROW
    WHEN OLD.updated_at = NEW.updated_at
BEGIN
    UPDATE trips SET updated_at = strftime('%Y-%m-%dT%H:%M:%fZ', 'now')
    WHERE id = OLD.id;
END;

CREATE TRIGGER trg_routes_updated_at
    AFTER UPDATE ON routes FOR EACH ROW
    WHEN OLD.updated_at = NEW.updated_at
BEGIN
    UPDATE routes SET updated_at = strftime('%Y-%m-%dT%H:%M:%fZ', 'now')
    WHERE id = OLD.id;
END;

CREATE TRIGGER trg_trip_routes_updated_at
    AFTER UPDATE ON trip_routes FOR EACH ROW
    WHEN OLD.updated_at = NEW.updated_at
BEGIN
    UPDATE trip_routes SET updated_at = strftime('%Y-%m-%dT%H:%M:%fZ', 'now')
    WHERE id = OLD.id;
END;

CREATE TRIGGER trg_trip_places_updated_at
    AFTER UPDATE ON trip_places FOR EACH ROW
    WHEN OLD.updated_at = NEW.updated_at
BEGIN
    UPDATE trip_places SET updated_at = strftime('%Y-%m-%dT%H:%M:%fZ', 'now')
    WHERE id = OLD.id;
END;

CREATE TRIGGER trg_route_places_updated_at
    AFTER UPDATE ON route_places FOR EACH ROW
    WHEN OLD.updated_at = NEW.updated_at
BEGIN
    UPDATE route_places SET updated_at = strftime('%Y-%m-%dT%H:%M:%fZ', 'now')
    WHERE id = OLD.id;
END;

-- SpatiaLite triggers for updating internal column for Places table
CREATE TRIGGER trg_places_geom_insert
    AFTER INSERT ON places FOR EACH ROW
BEGIN
    UPDATE places
    SET geom = MakePoint(NEW.longitude, NEW.latitude, 4326)
    WHERE id = NEW.id;
END;

CREATE TRIGGER trg_places_geom_update
    AFTER UPDATE OF latitude, longitude ON places FOR EACH ROW
BEGIN
    UPDATE places
    SET geom = MakePoint(NEW.longitude, NEW.latitude, 4326)
    WHERE id = OLD.id;
END;