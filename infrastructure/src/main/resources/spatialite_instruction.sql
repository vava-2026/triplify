-- Raw floats kept for simple/fast direct access
CREATE TABLE coords (
    latitude  REAL CHECK (latitude  BETWEEN -90  AND  90),
    longitude REAL CHECK (longitude BETWEEN -180 AND 180)
);

-- SpatiaLite POINT added as separate column for spatial queries
-- MakePoint(longitude, latitude, 4326)  ← note: lon FIRST per WGS84
SELECT AddGeometryColumn('coords', 'geom', 4326, 'POINT', 'XY');
SELECT CreateSpatialIndex('coords', 'geom');  -- R-Tree index

-- After that we could use different geo functions from the SQL query:
SELECT * FROM places
WHERE geom WITHIN(BuildMbr(16.8, 47.9, 17.4, 48.4, 4326));