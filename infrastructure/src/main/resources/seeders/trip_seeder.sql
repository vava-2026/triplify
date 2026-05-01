CREATE TEMP TABLE tmp_trips_data (
    title           TEXT NOT NULL,
    description     TEXT,
    category_name   TEXT NOT NULL,
    status          TEXT NOT NULL DEFAULT 'visited',
    started_at      TEXT NOT NULL,
    ended_at        TEXT NOT NULL,
    route_titles    TEXT NOT NULL,
    tag_names       TEXT NOT NULL
);

INSERT INTO tmp_trips_data (title, description, category_name, status, started_at, ended_at, route_titles, tag_names) VALUES

('Portugal Coastal Discovery',
 'Explore the stunning Atlantic coast from Lisbon to Porto, stopping at historic towers and golden-sand viewpoints.',
 'Road Trip', 'visited', '2025-06-01 00:00:00', '2025-06-08 00:00:00',
 'Portugal Atlantic Heritage Arc',
 'Beach,Photography'),

('Portugal Wine and Hills Escape',
 'A slow-paced journey through Portugal''s inland vineyards and hillside villages with local tastings and river walks.',
 'Road Trip', 'visited', '2025-07-10 00:00:00', '2025-07-17 00:00:00',
 'Portugal Hills and Vineyards Line',
 'Relax,Nature'),

('Portugal Full Circle',
 'Combining both the coastal heritage arc and the inland vineyard line for the ultimate Portuguese experience.',
 'Road Trip', 'planned', '2026-04-15 00:00:00', '2026-04-26 00:00:00',
 'Portugal Atlantic Heritage Arc,Portugal Hills and Vineyards Line',
 'Roadtrip,Photography,Nature,City'),

('Spain Architecture Odyssey',
 'From Gaudí''s masterpieces in Barcelona to the Gothic grandeur of medieval quarters across Spain.',
 'Culture & History', 'visited', '2025-03-10 00:00:00', '2025-03-17 00:00:00',
 'Spain Gaudi to Gothic Trail',
 'City,Photography'),

('Spain Inland Heritage Tour',
 'Fortress viewpoints, ancient streets and cultural districts away from the tourist-heavy coast.',
 'Culture & History', 'visited', '2025-09-05 00:00:00', '2025-09-13 00:00:00',
 'Spain Inland Culture Route',
 'Adventure,Hike'),

('Spain Grand Cultural Loop',
 'A comprehensive loop pairing Barcelona''s iconic design with the quiet dignity of Spain''s inland heritage towns.',
 'Culture & History', 'planned', '2026-05-01 00:00:00', '2026-05-12 00:00:00',
 'Spain Gaudi to Gothic Trail,Spain Inland Culture Route',
 'City,Photography,Study,Roadtrip'),

('France River and City Break',
 'Riverside promenades, grand boulevards and vibrant market squares make this an ideal French city escape.',
 'Road Trip', 'visited', '2025-04-20 00:00:00', '2025-04-26 00:00:00',
 'France River and Boulevard Circuit',
 'City,Food'),

('France Southern Flavors Journey',
 'A southbound culinary adventure through compact old towns and scenic promenades dripping with regional character.',
 'Road Trip', 'visited', '2025-08-01 00:00:00', '2025-08-09 00:00:00',
 'France Southbound Flavor Route',
 'Food,Relax'),

('France Complete Experience',
 'From Parisian boulevards to Provençal villages — a full sweep of France''s diversity in one cohesive trip.',
 'Road Trip', 'planned', '2026-06-10 00:00:00', '2026-06-22 00:00:00',
 'France River and Boulevard Circuit,France Southbound Flavor Route',
 'City,Food,Photography,Roadtrip'),

('Italy Art Cities Grand Tour',
 'A curated journey through galleries, cathedral squares and lively evening quarters across multiple Italian regions.',
 'Culture & History', 'visited', '2025-05-15 00:00:00', '2025-05-23 00:00:00',
 'Italy Art Cities Sequence',
 'City,Study'),

('Italy Lakes and Hilltowns',
 'Lakeside promenades give way to elevated hilltown panoramas in a visually stunning Italian contrast.',
 'Hiking', 'visited', '2025-10-03 00:00:00', '2025-10-11 00:00:00',
 'Italy Lakes to Hilltown Journey',
 'Nature,Photography'),

('Italy North to South Sweep',
 'Combining art-city highlights with lake and hilltown scenery for the definitive Italian travel experience.',
 'Road Trip', 'planned', '2026-09-01 00:00:00', '2026-09-14 00:00:00',
 'Italy Art Cities Sequence,Italy Lakes to Hilltown Journey',
 'City,Photography,Food,Romantic'),

('Germany Castle and Rhine Trail',
 'Sweeping castle panoramas, riverfront promenades and compact old-town walks in a satisfying regional loop.',
 'Road Trip', 'visited', '2025-07-20 00:00:00', '2025-07-28 00:00:00',
 'Germany Castle and River Loop',
 'Photography,Adventure'),

('Germany Forest and Heritage Path',
 'Forest-edge landscapes blend with historic districts for a trip that balances nature and culture perfectly.',
 'Hiking', 'visited', '2025-11-05 00:00:00', '2025-11-12 00:00:00',
 'Germany Forest Edge Itinerary',
 'Hike,Nature'),

('Germany Full Horizon',
 'Castles, rivers and forest trails combined into one grand German adventure from north to south.',
 'Road Trip', 'planned', '2026-07-15 00:00:00', '2026-07-27 00:00:00',
 'Germany Castle and River Loop,Germany Forest Edge Itinerary',
 'Adventure,Hike,Photography,Roadtrip'),

('Japan Neon and Temples',
 'A vivid corridor alternating the electric energy of modern Japanese cities with the serenity of ancient temple grounds.',
 'Culture & History', 'visited', '2025-03-25 00:00:00', '2025-04-03 00:00:00',
 'Japan Neon to Temple Corridor',
 'City,Photography'),

('Japan Gardens and Shrines',
 'Curated gardens, shrine approaches and traditional neighborhood streets woven together across multiple destinations.',
 'Hiking', 'visited', '2025-11-20 00:00:00', '2025-11-28 00:00:00',
 'Japan Garden and Shrine Line',
 'Relax,Spiritual'),

('Japan Full Cultural Immersion',
 'From neon-lit districts to moss-covered shrine paths — the complete spectrum of Japanese travel culture.',
 'Culture & History', 'planned', '2026-03-20 00:00:00', '2026-04-02 00:00:00',
 'Japan Neon to Temple Corridor,Japan Garden and Shrine Line',
 'Photography,Spiritual,City,Study'),

('Thailand Night Markets and Temples',
 'Riverfront temples by day and sizzling street markets by night define this perfectly balanced Thai itinerary.',
 'Road Trip', 'visited', '2025-01-12 00:00:00', '2025-01-20 00:00:00',
 'Thailand Night Market Route',
 'Food,City'),

('Thailand Northern Craft Discovery',
 'Mountain gateways, artisan workshops and quieter historical zones away from Thailand''s busiest tourist strips.',
 'Culture & History', 'visited', '2025-02-08 00:00:00', '2025-02-15 00:00:00',
 'Thailand Northern Craft Trail',
 'Adventure,Shopping'),

('Thailand North to South Experience',
 'Combining Bangkok''s vibrant night-market scene with the craft culture and mountain landscapes of the north.',
 'Road Trip', 'planned', '2026-01-10 00:00:00', '2026-01-22 00:00:00',
 'Thailand Night Market Route,Thailand Northern Craft Trail',
 'Food,Adventure,Shopping,Photography'),

('Mexico Pyramids and Plazas',
 'Archaeological wonders and grand civic plazas anchor this balanced Mexican adventure through pre-Hispanic heritage.',
 'Culture & History', 'visited', '2025-02-20 00:00:00', '2025-02-28 00:00:00',
 'Mexico Plaza and Pyramid Circuit',
 'Study,Photography'),

('Mexico Colonial Highlands',
 'Colorful colonial facades, hilltop views and vibrant cultural streets give this highland route its unique charm.',
 'Culture & History', 'visited', '2025-12-05 00:00:00', '2025-12-13 00:00:00',
 'Mexico Colonial Highlands Route',
 'City,Adventure'),

('Mexico Heritage Sweep',
 'A comprehensive sweep from coastal pyramids to highland colonial towns spanning Mexico''s full cultural range.',
 'Road Trip', 'planned', '2026-02-14 00:00:00', '2026-02-26 00:00:00',
 'Mexico Plaza and Pyramid Circuit,Mexico Colonial Highlands Route',
 'Study,City,Photography,Food'),

('USA Coast to Canyon Adventure',
 'Urban landmarks give way to dramatic natural canyons and open desert vistas in this spectacular American route.',
 'Adventure', 'visited', '2025-05-01 00:00:00', '2025-05-09 00:00:00',
 'United States Coast to Canyon Line',
 'Adventure,Nature'),

('USA City Skylines Tour',
 'An urban-focused arc exploring iconic skylines, waterfront neighborhoods and walkable downtown corridors.',
 'Road Trip', 'visited', '2025-06-20 00:00:00', '2025-06-28 00:00:00',
 'United States Urban Skylines Arc',
 'City,Photography'),

('USA Bicoastal Road Experience',
 'Combining dramatic canyon scenery with world-class city skylines for the ultimate American cross-country trip.',
 'Road Trip', 'planned', '2026-08-01 00:00:00', '2026-08-14 00:00:00',
 'United States Coast to Canyon Line,United States Urban Skylines Arc',
 'Roadtrip,Adventure,City,Photography'),

('Australia Surf and Harbor Escape',
 'Beach culture, harbor perspectives and laid-back city-edge exploration under brilliant Australian skies.',
 'Seaside', 'visited', '2025-01-25 00:00:00', '2025-02-02 00:00:00',
 'Australia Surf and Harbor Route',
 'Beach,Relax'),

('Australia Inland Discovery',
 'Regional markets, sweeping open landscapes and authentic cultural stops far from Australia''s coastal crowds.',
 'Adventure', 'visited', '2025-04-10 00:00:00', '2025-04-18 00:00:00',
 'Australia Inland Discovery Line',
 'Nature,Adventure'),

('Australia Coast and Outback',
 'Surfing beaches flow into the vast, humbling interior in this uniquely Australian two-route adventure.',
 'Road Trip', 'planned', '2026-10-05 00:00:00', '2026-10-18 00:00:00',
 'Australia Surf and Harbor Route,Australia Inland Discovery Line',
 'Beach,Nature,Adventure,Photography');

INSERT OR IGNORE INTO trips (id, user_id, category_id, cover_image_id, title, description, status, started_at, ended_at, created_at, updated_at)
SELECT
    lower(
        hex(randomblob(4)) || '-' ||
        hex(randomblob(2)) || '-' ||
        '4' || substr(hex(randomblob(2)), 2) || '-' ||
        substr('89ab', abs(random()) % 4 + 1, 1) || substr(hex(randomblob(2)), 2) || '-' ||
        hex(randomblob(6))
    ) AS id,
    'f1d1a33f-1e7c-4ea1-bb1d-c8f06efb5b5a' AS user_id,
    c.id AS category_id,
    NULL AS cover_image_id,
    td.title,
    td.description,
    td.status,
    td.started_at,
    td.ended_at,
    datetime('now') AS created_at,
    datetime('now') AS updated_at
FROM tmp_trips_data td
INNER JOIN categories c ON c.name = td.category_name;

WITH RECURSIVE split_routes(trip_title, route_chunk, rest, route_order) AS (
    SELECT
        title,
        CASE
            WHEN instr(route_titles, ',') > 0
            THEN trim(substr(route_titles, 1, instr(route_titles, ',') - 1))
            ELSE trim(route_titles)
        END,
        CASE
            WHEN instr(route_titles, ',') > 0
            THEN substr(route_titles, instr(route_titles, ',') + 1)
            ELSE NULL
        END,
        0
    FROM tmp_trips_data
    UNION ALL
    SELECT
        trip_title,
        CASE
            WHEN instr(rest, ',') > 0
            THEN trim(substr(rest, 1, instr(rest, ',') - 1))
            ELSE trim(rest)
        END,
        CASE
            WHEN instr(rest, ',') > 0
            THEN substr(rest, instr(rest, ',') + 1)
            ELSE NULL
        END,
        route_order + 1
    FROM split_routes
    WHERE rest IS NOT NULL
)
INSERT OR IGNORE INTO trip_routes (id, trip_id, route_id, "order", status, started_at, ended_at, created_at, updated_at)
SELECT
    lower(
        hex(randomblob(4)) || '-' ||
        hex(randomblob(2)) || '-' ||
        '4' || substr(hex(randomblob(2)), 2) || '-' ||
        substr('89ab', abs(random()) % 4 + 1, 1) || substr(hex(randomblob(2)), 2) || '-' ||
        hex(randomblob(6))
    ) AS id,
    t.id AS trip_id,
    r.id AS route_id,
    sr.route_order AS "order",
    t.status AS status,
    t.started_at,
    t.ended_at,
    datetime('now') AS created_at,
    datetime('now') AS updated_at
FROM split_routes sr
INNER JOIN trips t ON t.title = sr.trip_title
INNER JOIN routes r ON r.title = sr.route_chunk AND r.user_id = 'f1d1a33f-1e7c-4ea1-bb1d-c8f06efb5b5a';

WITH RECURSIVE split_tags(trip_title, tag_chunk, rest) AS (
    SELECT
        title,
        CASE
            WHEN instr(tag_names, ',') > 0
            THEN trim(substr(tag_names, 1, instr(tag_names, ',') - 1))
            ELSE trim(tag_names)
        END,
        CASE
            WHEN instr(tag_names, ',') > 0
            THEN substr(tag_names, instr(tag_names, ',') + 1)
            ELSE NULL
        END
    FROM tmp_trips_data
    UNION ALL
    SELECT
        trip_title,
        CASE
            WHEN instr(rest, ',') > 0
            THEN trim(substr(rest, 1, instr(rest, ',') - 1))
            ELSE trim(rest)
        END,
        CASE
            WHEN instr(rest, ',') > 0
            THEN substr(rest, instr(rest, ',') + 1)
            ELSE NULL
        END
    FROM split_tags
    WHERE rest IS NOT NULL
)
INSERT OR IGNORE INTO trip_tags (trip_id, tag_id)
SELECT
    t.id AS trip_id,
    tg.id AS tag_id
FROM split_tags st
INNER JOIN trips t ON t.title = st.trip_title
INNER JOIN tags tg ON tg.name = st.tag_chunk
WHERE tg.user_id = 'f1d1a33f-1e7c-4ea1-bb1d-c8f06efb5b5a';

DROP TABLE tmp_trips_data;