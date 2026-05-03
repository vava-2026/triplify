CREATE TABLE statistics (
    id TEXT NOT NULL PRIMARY KEY,
    user_id TEXT NOT NULL
        REFERENCES users(id)
            ON DELETE CASCADE ON UPDATE CASCADE,
    type TEXT NOT NULL
        CHECK (type IN ('COUNTRIES_VISITED', 'TOTAL_TRIPS', 'PLACES_VISITED', 'TRAVEL_DAYS', 'PHOTOS_UPLOADED', 'KILOMETERS_TRAVELLED', 'ROUTES_CREATED', 'STORIES_CREATED')),
    amount INTEGER NOT NULL DEFAULT 0
        CHECK (amount >= 0),

    UNIQUE(user_id, type)
);

CREATE INDEX idx_statistics_user_id ON statistics(user_id);
