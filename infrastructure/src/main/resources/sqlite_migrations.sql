-- Pragmas(packages) for SQLite
PRAGMA foreign_keys  = ON;
-- Perfomance pragmas
PRAGMA journal_mode  = WAL;
PRAGMA synchronous   = NORMAL;

-- Initializing of SpatiaLite module
SELECT InitSpatialMetaData(1);


-- ENUMS (enforced via CHECK)
-- ROLE_ENUM     : 'guest' | 'user' | 'moderator' | 'admin'
-- STATUS_ENUM   : 'draft' | 'active' | 'completed' | 'archived'
-- PRIORITY_ENUM : 'low' | 'medium' | 'high' | 'critical'

CREATE TABLE images (
    id          TEXT     NOT NULL PRIMARY KEY,
    url         TEXT     NOT NULL UNIQUE,
    storage_key TEXT     NOT NULL UNIQUE,
    description TEXT,
    uploaded_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')) CHECK (datetime(started_at) IS NOT NULL)
);

CREATE TABLE users (
    id              TEXT     NOT NULL PRIMARY KEY,
    username        TEXT     NOT NULL UNIQUE COLLATE NOCASE,
    email           TEXT     NOT NULL UNIQUE,
    password_hash   TEXT     NOT NULL,
    role            TEXT     NOT NULL DEFAULT 'user'
       CHECK (role IN ('guest', 'user', 'moderator', 'admin')),
    avatar_image_id TEXT
       REFERENCES images(id)
           ON DELETE SET NULL
           ON UPDATE CASCADE,
    created_at      TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')) CHECK (datetime(started_at) IS NOT NULL),
    updated_at      TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')) CHECK (datetime(started_at) IS NOT NULL)
);
CREATE INDEX idx_users_email ON users(email COLLATE NOCASE);

CREATE TABLE categories (
    id          TEXT NOT NULL PRIMARY KEY,
    created_by  TEXT NOT NULL
        REFERENCES users(id)
            ON DELETE RESTRICT
            ON UPDATE CASCADE,
    name        TEXT NOT NULL UNIQUE COLLATE NOCASE,
    description TEXT,
    icon        TEXT
);

CREATE INDEX idx_categories_created_by ON categories(created_by);

CREATE TABLE countries (
   id            TEXT    NOT NULL PRIMARY KEY,
   created_by    TEXT    NOT NULL
       REFERENCES users(id)
           ON DELETE RESTRICT
           ON UPDATE CASCADE,
   name          TEXT    NOT NULL UNIQUE COLLATE NOCASE,
   flag_image_id TEXT
       REFERENCES images(id)
           ON DELETE SET NULL
           ON UPDATE CASCADE,
   is_banned     INTEGER NOT NULL DEFAULT 0
       CHECK (is_banned IN (0, 1))
);

CREATE INDEX idx_countries_created_by ON countries(created_by);
CREATE INDEX idx_countries_is_banned  ON countries(is_banned);

CREATE TABLE emotions (
    id         TEXT NOT NULL PRIMARY KEY,
    created_by TEXT NOT NULL
      REFERENCES users(id)
          ON DELETE RESTRICT
          ON UPDATE CASCADE,
    name       TEXT NOT NULL UNIQUE COLLATE NOCASE,
    icon       TEXT
);
CREATE INDEX idx_emotions_created_by ON emotions(created_by);

CREATE TABLE tags (
    id      TEXT NOT NULL PRIMARY KEY,
    user_id TEXT NOT NULL
      REFERENCES users(id)
          ON DELETE CASCADE
          ON UPDATE CASCADE,
    name    TEXT NOT NULL COLLATE NOCASE,

    UNIQUE (user_id, name)
);

CREATE INDEX idx_tags_user_id ON tags(user_id);

CREATE TABLE places (
    id          TEXT     NOT NULL PRIMARY KEY,
    user_id     TEXT     NOT NULL
        REFERENCES users(id)
            ON DELETE RESTRICT
            ON UPDATE CASCADE,
    country_id  TEXT     NOT NULL
        REFERENCES countries(id)
            ON DELETE RESTRICT
            ON UPDATE CASCADE,
    title       TEXT     NOT NULL COLLATE NOCASE,
    description TEXT,
    latitude    REAL     NOT NULL CHECK (latitude  BETWEEN -90  AND  90),
    longitude   REAL     NOT NULL CHECK (longitude BETWEEN -180 AND 180),
    created_at  TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')) CHECK (datetime(started_at) IS NOT NULL),
    updated_at  TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')) CHECK (datetime(started_at) IS NOT NULL)
);

-- SpatiaLite POINT column — WGS84 (SRID 4326), lon FIRST per spec
SELECT AddGeometryColumn('places', 'geom', 4326, 'POINT', 'XY');
SELECT CreateSpatialIndex('places', 'geom');

CREATE INDEX idx_places_user_id    ON places(user_id);
CREATE INDEX idx_places_country_id ON places(country_id);

CREATE TABLE trips (
    id          TEXT     NOT NULL PRIMARY KEY,
    user_id     TEXT     NOT NULL
       REFERENCES users(id)
           ON DELETE RESTRICT
           ON UPDATE CASCADE,
    category_id TEXT
       REFERENCES categories(id)
           ON DELETE SET NULL
           ON UPDATE CASCADE,
    country_id  TEXT
       REFERENCES countries(id)
           ON DELETE SET NULL
           ON UPDATE CASCADE,
    title       TEXT     NOT NULL COLLATE NOCASE,
    description TEXT,
    status      TEXT     NOT NULL DEFAULT 'draft'
       CHECK (status IN ('draft', 'active', 'completed', 'archived')),
    started_at  TEXT CHECK (datetime(started_at) IS NOT NULL),
    ended_at    TEXT CHECK (datetime(started_at) IS NOT NULL),
    created_at  TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')) CHECK (datetime(started_at) IS NOT NULL),
    updated_at  TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')) CHECK (datetime(started_at) IS NOT NULL),
);
CREATE INDEX idx_trips_user_id     ON trips(user_id);
CREATE INDEX idx_trips_category_id ON trips(category_id);
CREATE INDEX idx_trips_country_id  ON trips(country_id);
CREATE INDEX idx_trips_status      ON trips(status);

CREATE TABLE routes (
    id           TEXT     NOT NULL PRIMARY KEY,
    user_id      TEXT     NOT NULL
        REFERENCES users(id)
            ON DELETE RESTRICT
            ON UPDATE CASCADE,
    title        TEXT     NOT NULL COLLATE NOCASE,
    description  TEXT,
    total_length REAL     CHECK (total_length IS NULL OR total_length >= 0),
    status       TEXT     NOT NULL DEFAULT 'draft'
        CHECK (status IN ('draft', 'active', 'completed', 'archived')),
    created_at   TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')) CHECK (datetime(started_at) IS NOT NULL),
    updated_at   TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')) CHECK (datetime(started_at) IS NOT NULL)
);

CREATE INDEX idx_routes_user_id ON routes(user_id);
CREATE INDEX idx_routes_status  ON routes(status);

CREATE TABLE trip_routes (
     id         TEXT     NOT NULL PRIMARY KEY,
     trip_id    TEXT     NOT NULL
         REFERENCES trips(id)
             ON DELETE CASCADE
             ON UPDATE CASCADE,
     route_id   TEXT     NOT NULL
         REFERENCES routes(id)
             ON DELETE RESTRICT
             ON UPDATE CASCADE,
     priority   TEXT     NOT NULL DEFAULT 'medium'
         CHECK (priority IN ('low', 'medium', 'high', 'critical')),
     status     TEXT     NOT NULL DEFAULT 'draft'
         CHECK (status IN ('draft', 'active', 'completed', 'archived')),
     started_at TEXT CHECK (datetime(started_at) IS NOT NULL),
     ended_at   TEXT CHECK (datetime(started_at) IS NOT NULL),

     UNIQUE (trip_id, route_id),
);

CREATE INDEX idx_trip_routes_trip_id  ON trip_routes(trip_id);
CREATE INDEX idx_trip_routes_route_id ON trip_routes(route_id);

CREATE TABLE route_places (
    id         TEXT     NOT NULL PRIMARY KEY,
    route_id   TEXT     NOT NULL
      REFERENCES routes(id)
          ON DELETE CASCADE
          ON UPDATE CASCADE,
    place_id   TEXT     NOT NULL
      REFERENCES places(id)
          ON DELETE RESTRICT
          ON UPDATE CASCADE,
    priority   TEXT     NOT NULL DEFAULT 'medium'
      CHECK (priority IN ('low', 'medium', 'high', 'critical')),
    status     TEXT     NOT NULL DEFAULT 'draft'
      CHECK (status IN ('draft', 'active', 'completed', 'archived')),
    started_at TEXT CHECK (datetime(started_at) IS NOT NULL),
    ended_at   TEXT CHECK (datetime(started_at) IS NOT NULL),

    UNIQUE (route_id, place_id),
);
CREATE INDEX idx_route_places_route_id ON route_places(route_id);
CREATE INDEX idx_route_places_place_id ON route_places(place_id);

CREATE TABLE stories (
     id          TEXT     NOT NULL PRIMARY KEY,
     user_id     TEXT     NOT NULL
         REFERENCES users(id)
             ON DELETE RESTRICT
             ON UPDATE CASCADE,
     trip_id     TEXT
         REFERENCES trips(id)
             ON DELETE SET NULL
             ON UPDATE CASCADE,
     emotion_id  TEXT
         REFERENCES emotions(id)
             ON DELETE SET NULL
             ON UPDATE CASCADE,
     title       TEXT     NOT NULL COLLATE NOCASE,
     description TEXT,
     story_time  TEXT NOT NULL,
     created_at  TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')) CHECK (datetime(started_at) IS NOT NULL),
);
CREATE INDEX idx_stories_user_id    ON stories(user_id);
CREATE INDEX idx_stories_trip_id    ON stories(trip_id);
CREATE INDEX idx_stories_emotion_id ON stories(emotion_id);

CREATE TABLE badges_groups (
    id          TEXT NOT NULL PRIMARY KEY,
    created_by  TEXT NOT NULL
       REFERENCES users(id)
           ON DELETE RESTRICT
           ON UPDATE CASCADE,
    name        TEXT NOT NULL UNIQUE COLLATE NOCASE,
    description TEXT
);

CREATE INDEX idx_badges_groups_created_by ON badges_groups(created_by);

CREATE TABLE badges (
    id             TEXT    NOT NULL PRIMARY KEY,
    created_by     TEXT    NOT NULL
        REFERENCES users(id)
            ON DELETE RESTRICT
            ON UPDATE CASCADE,
    group_id       TEXT    NOT NULL
        REFERENCES badges_groups(id)
            ON DELETE RESTRICT
            ON UPDATE CASCADE,
    image_id       TEXT
        REFERENCES images(id)
            ON DELETE SET NULL
            ON UPDATE CASCADE,
    name           TEXT    NOT NULL COLLATE NOCASE,
    description    TEXT,
    level          INTEGER NOT NULL DEFAULT 1  CHECK (level >= 1),
    required_value INTEGER NOT NULL DEFAULT 0  CHECK (required_value >= 0),

    UNIQUE (group_id, name, level)
);
CREATE INDEX idx_badges_created_by ON badges(created_by);
CREATE INDEX idx_badges_group_id   ON badges(group_id);

CREATE TABLE user_badges (
    user_id        TEXT    NOT NULL
     REFERENCES users(id)
         ON DELETE CASCADE
         ON UPDATE CASCADE,
    badge_id       TEXT    NOT NULL
     REFERENCES badges(id)
         ON DELETE CASCADE
         ON UPDATE CASCADE,
    progress_value INTEGER NOT NULL DEFAULT 0  CHECK (progress_value >= 0),
    is_unlocked    INTEGER NOT NULL DEFAULT 0  CHECK (is_unlocked IN (0, 1)),

    PRIMARY KEY (user_id, badge_id)
);
CREATE INDEX idx_user_badges_badge_id ON user_badges(badge_id);

CREATE TABLE trip_tags (
    trip_id TEXT NOT NULL
       REFERENCES trips(id)
           ON DELETE CASCADE
           ON UPDATE CASCADE,
    tag_id  TEXT NOT NULL
       REFERENCES tags(id)
           ON DELETE CASCADE
           ON UPDATE CASCADE,

    PRIMARY KEY (trip_id, tag_id)
);
CREATE INDEX idx_trip_tags_tag_id ON trip_tags(tag_id);

CREATE TABLE story_tags (
    story_id TEXT NOT NULL
        REFERENCES stories(id)
            ON DELETE CASCADE
            ON UPDATE CASCADE,
    tag_id   TEXT NOT NULL
        REFERENCES tags(id)
            ON DELETE CASCADE
            ON UPDATE CASCADE,

    PRIMARY KEY (story_id, tag_id)
);
CREATE INDEX idx_story_tags_tag_id ON story_tags(tag_id);


-- Images junction tables with main entities: Trip, Route, Places

-- TRIP_IMAGES: image_id PK, trip_id PK
CREATE TABLE trip_images (
    image_id TEXT NOT NULL
     REFERENCES images(id)
         ON DELETE CASCADE
         ON UPDATE CASCADE,
    trip_id  TEXT NOT NULL
     REFERENCES trips(id)
         ON DELETE CASCADE
         ON UPDATE CASCADE,

    PRIMARY KEY (image_id, trip_id)
);
CREATE INDEX idx_trip_images_trip_id ON trip_images(trip_id);

-- PLACES_IMAGES: place_id PK, image_id PK
CREATE TABLE places_images (
    place_id TEXT NOT NULL
       REFERENCES places(id)
           ON DELETE CASCADE
           ON UPDATE CASCADE,
    image_id TEXT NOT NULL
       REFERENCES images(id)
           ON DELETE CASCADE
           ON UPDATE CASCADE,

    PRIMARY KEY (place_id, image_id)
);
CREATE INDEX idx_places_images_place_id ON places_images(place_id);

-- ROUTE_IMAGES: route_id PK, image_id PK
CREATE TABLE route_images (
    route_id TEXT NOT NULL
      REFERENCES routes(id)
          ON DELETE CASCADE
          ON UPDATE CASCADE,
    image_id TEXT NOT NULL
      REFERENCES images(id)
          ON DELETE CASCADE
          ON UPDATE CASCADE,

    PRIMARY KEY (route_id, image_id)
);
CREATE INDEX idx_route_images_route_id ON route_images(route_id);

-- STORY_IMAGES: story_id PK, image_id PK
CREATE TABLE story_images (
    story_id TEXT NOT NULL
      REFERENCES stories(id)
          ON DELETE CASCADE
          ON UPDATE CASCADE,
    image_id TEXT NOT NULL
      REFERENCES images(id)
          ON DELETE CASCADE
          ON UPDATE CASCADE,

    PRIMARY KEY (story_id, image_id)
);
CREATE INDEX idx_story_images_story_id ON story_images(story_id);


-- updated_at triggers
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
    UPDATE users SET updated_at = strftime('%Y-%m-%dT%H:%M:%fZ', 'now')
    WHERE id = OLD.id;
END;

CREATE TRIGGER trg_trips_updated_at
    AFTER UPDATE ON trips FOR EACH ROW
    WHEN OLD.updated_at = NEW.updated_at
BEGIN
    UPDATE users SET updated_at = strftime('%Y-%m-%dT%H:%M:%fZ', 'now')
    WHERE id = OLD.id;
END;

CREATE TRIGGER trg_routes_updated_at
    AFTER UPDATE ON routes FOR EACH ROW
    WHEN OLD.updated_at = NEW.updated_at
BEGIN
    UPDATE users SET updated_at = strftime('%Y-%m-%dT%H:%M:%fZ', 'now')
    WHERE id = OLD.id;
END;


-- SpatiaLite triggers, that syncs new geom position from latitude and longitude of the geo position.
CREATE TRIGGER trg_places_geom_insert
    AFTER INSERT ON places FOR EACH ROW BEGIN
    UPDATE places
    SET geom = MakePoint(NEW.longitude, NEW.latitude, 4326)
    WHERE id = NEW.id;
END;

CREATE TRIGGER trg_places_geom_update
    AFTER UPDATE OF latitude, longitude ON places FOR EACH ROW BEGIN
    UPDATE places
    SET geom = MakePoint(NEW.longitude, NEW.latitude, 4326)
    WHERE id = OLD.id;
END;