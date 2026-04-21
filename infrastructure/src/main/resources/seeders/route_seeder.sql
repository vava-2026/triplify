CREATE TEMP TABLE tmp_seed_routes (
    route_title_en TEXT NOT NULL,
    country_name TEXT NOT NULL,
    start_n INTEGER NOT NULL,
    end_n INTEGER NOT NULL,
    focus_en TEXT NOT NULL
);

INSERT INTO tmp_seed_routes (route_title_en, country_name, start_n, end_n, focus_en) VALUES
('Portugal Atlantic Heritage Arc', 'Portugal', 2, 7, 'coastal viewpoints and old-town riversides'),
('Portugal Hills and Vineyards Line', 'Portugal', 10, 14, 'hill towns and vineyard detours'),
('Spain Gaudi to Gothic Trail', 'Spain', 3, 8, 'architectural icons and historic plazas'),
('Spain Inland Culture Route', 'Spain', 11, 17, 'museums, fortresses, and slower inland cities'),
('France River and Boulevard Circuit', 'France', 2, 6, 'riverside walks and market streets'),
('France Southbound Flavor Route', 'France', 9, 15, 'regional cuisine and compact rail hops'),
('Italy Art Cities Sequence', 'Italy', 2, 9, 'gallery districts and piazza life'),
('Italy Lakes to Hilltown Journey', 'Italy', 12, 18, 'lake promenades and panoramic hill routes'),
('Germany Castle and River Loop', 'Germany', 2, 8, 'riverfront cores and castle viewpoints'),
('Germany Forest Edge Itinerary', 'Germany', 11, 16, 'green borders and historic quarters'),
('Japan Neon to Temple Corridor', 'Japan', 2, 10, 'urban contrasts and evening food lanes'),
('Japan Garden and Shrine Line', 'Japan', 12, 19, 'temple gardens and traditional districts'),
('Thailand Night Market Route', 'Thailand', 2, 7, 'city bazaars and relaxed evening districts'),
('Thailand Northern Craft Trail', 'Thailand', 10, 14, 'craft villages and mountain gateways'),
('Mexico Plaza and Pyramid Circuit', 'Mexico', 2, 8, 'central plazas and archaeological highlights'),
('Mexico Colonial Highlands Route', 'Mexico', 11, 16, 'colonial facades and hillside overlooks'),
('United States Coast to Canyon Line', 'United States', 2, 7, 'major landmarks and scenic drives'),
('United States Urban Skylines Arc', 'United States', 10, 15, 'downtown cores and waterfront neighborhoods'),
('Australia Surf and Harbor Route', 'Australia', 2, 6, 'beachfronts, harbors, and park edges'),
('Australia Inland Discovery Line', 'Australia', 9, 13, 'outback textures and regional galleries');

INSERT OR IGNORE INTO routes (id, user_id, cover_image_id, title, description, length)
SELECT
    lower(
        hex(randomblob(4)) || '-' ||
        hex(randomblob(2)) || '-' ||
        '4' || substr(hex(randomblob(2)), 2) || '-' ||
        substr('89ab', abs(random()) % 4 + 1, 1) || substr(hex(randomblob(2)), 2) || '-' ||
        hex(randomblob(6))
    ) AS id,
    'f1d1a33f-1e7c-4ea1-bb1d-c8f06efb5b5a' AS user_id,
    NULL AS cover_image_id,
    ts.route_title_en,
    printf(
        'This route through %s covers places %d to %d with a practical flow focused on %s. It is designed for realistic transfer times, balanced pacing, and enough flexibility for weather or queue changes.',
        ts.country_name,
        ts.start_n,
        ts.end_n,
        ts.focus_en
    ),
    NULL AS length
FROM tmp_seed_routes ts;

WITH places_in_routes AS (
    SELECT
        r.id AS route_id,
        p.id AS place_id,
        CAST(substr(p.title, -2) AS INTEGER) AS place_n
    FROM tmp_seed_routes ts
    INNER JOIN routes r
        ON r.user_id = 'f1d1a33f-1e7c-4ea1-bb1d-c8f06efb5b5a'
       AND r.title = ts.route_title_en
    INNER JOIN countries c
        ON c.name = ts.country_name
    INNER JOIN places p
        ON p.user_id = 'f1d1a33f-1e7c-4ea1-bb1d-c8f06efb5b5a'
       AND p.country_id = c.id
       AND CAST(substr(p.title, -2) AS INTEGER) BETWEEN ts.start_n AND ts.end_n
),
ordered_places AS (
    SELECT
        route_id,
        place_id,
        ROW_NUMBER() OVER (PARTITION BY route_id ORDER BY place_n) - 1 AS place_order
    FROM places_in_routes
)
INSERT OR IGNORE INTO route_places (id, route_id, place_id, "order")
SELECT
    lower(
        hex(randomblob(4)) || '-' ||
        hex(randomblob(2)) || '-' ||
        '4' || substr(hex(randomblob(2)), 2) || '-' ||
        substr('89ab', abs(random()) % 4 + 1, 1) || substr(hex(randomblob(2)), 2) || '-' ||
        hex(randomblob(6))
    ) AS id,
    route_id,
    place_id,
    place_order
FROM ordered_places;

DROP TABLE tmp_seed_routes;
