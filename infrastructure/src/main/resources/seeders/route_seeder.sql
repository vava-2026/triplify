CREATE TEMP TABLE tmp_seed_routes (
    route_title_en TEXT NOT NULL,
    country_name TEXT NOT NULL,
    start_n INTEGER NOT NULL,
    end_n INTEGER NOT NULL,
    route_description_en TEXT NOT NULL
);

INSERT INTO tmp_seed_routes (
    route_title_en,
    country_name,
    start_n,
    end_n,
    route_description_en
) VALUES
    ('Portugal Atlantic Heritage Arc', 'Portugal', 2, 7, 'This route follows Portugal''s Atlantic-facing highlights with enough time for coastal walks, old-quarter detours, and evening viewpoints. Connections are intentionally short so travelers can stop for local food markets without breaking the daily rhythm.'),
    ('Portugal Hills and Vineyards Line', 'Portugal', 10, 14, 'A slower inland itinerary that links hillside towns, river overlooks, and vineyard landscapes at a comfortable pace. The sequence leaves room for museum visits and unplanned scenic stops between transfers.'),
    ('Spain Gaudi to Gothic Trail', 'Spain', 3, 8, 'Designed around landmark architecture and historic plazas, this route blends major icons with walkable neighborhood segments. Daily legs are balanced to avoid rushed transitions and keep time for evening street life.'),
    ('Spain Inland Culture Route', 'Spain', 6, 12, 'An inland-focused circuit that combines fortress viewpoints, heritage streets, and quieter cultural districts beyond the busiest coasts. Travel windows are practical, giving flexibility for queues, weather, or local events.'),
    ('France River and Boulevard Circuit', 'France', 2, 6, 'This urban-to-riverside path pairs historic centers with broad pedestrian boulevards and market areas. It is structured for smooth rail or road transfers while still preserving downtime in each stop.'),
    ('France Southbound Flavor Route', 'France', 9, 15, 'A southbound route built around regional cuisine, compact old towns, and scenic promenades with frequent photo stops. The order is optimized for realistic movement so each day feels full but not overloaded.'),
    ('Italy Art Cities Sequence', 'Italy', 2, 9, 'A classic art-city progression connecting gallery zones, cathedral squares, and lively evening quarters across multiple regions. The plan mixes headline landmarks with human-scale streets to keep the route varied and practical.'),
    ('Italy Lakes to Hilltown Journey', 'Italy', 12, 18, 'This itinerary transitions from lakeside promenades to elevated hilltown viewpoints with strong visual contrast day to day. Distances are moderated to leave space for local meals, short hikes, and weather-aware adjustments.'),
    ('Germany Castle and River Loop', 'Germany', 2, 8, 'A loop route combining castle panoramas, riverfront cores, and compact old-town walks in a logical regional sweep. It emphasizes dependable transfers and walkability so the schedule remains stable even during peak periods.'),
    ('Germany Forest Edge Itinerary', 'Germany', 7, 13, 'This line explores forest-edge regions and historic districts where nature viewpoints and city heritage overlap. The pacing supports relaxed mornings and late-day exploration without forcing tight timing windows.'),
    ('Japan Neon to Temple Corridor', 'Japan', 2, 10, 'Built to contrast modern city energy with temple precinct calm, this corridor alternates dense urban stops and reflective cultural sites. Intercity legs are chosen to be efficient, leaving room for food alleys and evening observation points.'),
    ('Japan Garden and Shrine Line', 'Japan', 12, 19, 'A route centered on shrine approaches, curated gardens, and traditional neighborhood streets across multiple destinations. The ordering minimizes backtracking and keeps enough flexibility for seasonal weather or crowd conditions.'),
    ('Thailand Night Market Route', 'Thailand', 2, 7, 'This route connects riverfront temples, market districts, and coastal moments with a focus on evening-friendly planning. Daytime transitions are short so travelers can keep energy for night markets and local street dining.'),
    ('Thailand Northern Craft Trail', 'Thailand', 10, 14, 'A northern trail shaped around craft culture, mountain gateways, and quieter historical zones away from high-pressure timelines. It supports spontaneous local stops while maintaining a clean, manageable transfer structure.'),
    ('Mexico Plaza and Pyramid Circuit', 'Mexico', 2, 8, 'An itinerary that links civic plazas and archaeological highlights with balanced city walking segments between major sites. The route is arranged for practical logistics and enough dwell time at each landmark zone.'),
    ('Mexico Colonial Highlands Route', 'Mexico', 11, 16, 'This highland-focused line combines colonial facades, hilltop viewpoints, and cultural streets with strong regional character. Travel pacing remains moderate, allowing for local dining breaks and short optional detours.'),
    ('United States Coast to Canyon Line', 'United States', 2, 7, 'A broad-contrast route moving from major urban landmarks toward dramatic natural viewpoints in a coherent progression. Distances and stop order are tuned for realistic transfer windows and reliable day structure.'),
    ('United States Urban Skylines Arc', 'United States', 10, 15, 'An urban-centered arc focused on skyline districts, waterfront neighborhoods, and walkable downtown corridors across key cities. The sequence keeps enough free buffer for museums, live events, or traffic-related delays.'),
    ('Australia Surf and Harbor Route', 'Australia', 2, 6, 'This route emphasizes beach culture, harbor perspectives, and relaxed city-edge exploration with frequent scenic breaks. Daily planning is intentionally flexible to account for weather shifts and sunset-timed viewpoints.'),
    ('Australia Inland Discovery Line', 'Australia', 9, 13, 'An inland discovery track that blends regional markets, open landscapes, and cultural stops with distinctive local atmosphere. Transfers are spaced for comfort, enabling deeper visits instead of quick checklist stops.');

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
    ts.route_description_en,
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
