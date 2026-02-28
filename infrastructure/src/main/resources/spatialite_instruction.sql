-- Raw floats kept for simple/fast direct access
latitude  REAL CHECK (latitude  BETWEEN -90  AND  90)
longitude REAL CHECK (longitude BETWEEN -180 AND 180)

-- SpatiaLite POINT added as separate column for spatial queries
-- MakePoint(longitude, latitude, 4326)  ← note: lon FIRST per WGS84
SELECT AddGeometryColumn('places', 'geom', 4326, 'POINT', 'XY');
SELECT CreateSpatialIndex('places', 'geom');  -- R-Tree index