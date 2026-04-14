ALTER TABLE trip_places
    ADD COLUMN source_type TEXT NOT NULL DEFAULT 'manual'
    CHECK (source_type IN ('manual', 'route'));

ALTER TABLE trip_places
    ADD COLUMN trip_route_id TEXT
    REFERENCES trip_routes(id)
        ON DELETE SET NULL ON UPDATE CASCADE;

ALTER TABLE trip_places
    ADD COLUMN route_place_id TEXT
    REFERENCES route_places(id)
        ON DELETE SET NULL ON UPDATE CASCADE;

CREATE INDEX idx_trip_places_trip_route_id ON trip_places(trip_route_id);
CREATE INDEX idx_trip_places_route_place_id ON trip_places(route_place_id);
