CREATE TEMP TABLE tmp_stories_data (
    title        TEXT NOT NULL,
    description  TEXT,
    trip_title   TEXT NOT NULL,
    story_time   TEXT NOT NULL,
    emotion_name TEXT,
    tag_names    TEXT
);
INSERT INTO tmp_stories_data (title, description, trip_title, story_time, emotion_name, tag_names) VALUES

('First Morning in Lisbon',
 'The trams, the tiles, the pastel de nata — everything exceeded expectations on the very first morning in this city.',
 'Portugal Coastal Discovery',
 '2025-06-02T09:30:00Z',
 'Joy',
 'City,Photography'),

('Vineyard Sunset Moment',
 'Sitting above the Douro with a local white wine in hand as the valley turned amber below us — a moment of pure stillness.',
 'Portugal Wine and Hills Escape',
 '2025-07-12T19:45:00Z',
 'Awe',
 'Nature,Relax,Romantic'),

('Gaudí Overwhelm in Barcelona',
 'Nothing really prepares you for standing inside the Sagrada Família for the first time. The light through the stained glass made the whole world feel suspended.',
 'Spain Architecture Odyssey',
 '2025-03-11T11:15:00Z',
 'Awe',
 'City,Photography,Study'),

('Toledo at Dusk',
 'The old city glowed like a lantern above the plain as the sun dropped behind the Castilian hills. One of those views that stays with you.',
 'Spain Inland Heritage Tour',
 '2025-09-07T19:00:00Z',
 'Joy',
 'City,Study,Photography'),

('Seine Morning Walk',
 'An early walk along the Seine before the city woke up — bouquinistes still shuttered, pigeons everywhere, the light impossibly soft.',
 'France River and City Break',
 '2025-04-21T07:30:00Z',
 'Calm',
 'City,Romantic,Photography'),

('Market Day in Provence',
 'The lavender soap, the olives, the local rosé — a market in Provence is sensory overload of the best possible kind.',
 'France Southern Flavors Journey',
 '2025-08-04T10:00:00Z',
 'Joy',
 'Food,Photography,Nature'),

('Florence at First Light',
 'Crossing the Ponte Vecchio before dawn with nobody around felt like having one of the world''s great cities entirely to myself.',
 'Italy Art Cities Grand Tour',
 '2025-05-16T06:00:00Z',
 'Awe',
 'City,Study,Romantic'),

('Lake Como Afternoon',
 'A ferry across the lake, the mountains mirrored perfectly in the water, the slow pace of everything — Italy at its most dreamy.',
 'Italy Lakes and Hilltowns',
 '2025-10-05T15:30:00Z',
 'Calm',
 'Nature,Relax,Romantic'),

('Neuschwanstein Fog Day',
 'The castle appeared and disappeared through the fog all morning. Somehow the mystery made it even more beautiful than any postcard version.',
 'Germany Castle and Rhine Trail',
 '2025-07-22T10:45:00Z',
 'Awe',
 'Mountains,Photography,Romantic'),

('Black Forest Trail Solo',
 'A quiet four-hour hike through dense pine forest with no signal and no agenda — the most present I felt the entire trip.',
 'Germany Forest and Heritage Path',
 '2025-11-07T09:00:00Z',
 'Calm',
 'Nature,Hike,Solo'),

('Shibuya Crossing at Midnight',
 'Standing in the middle of Shibuya Crossing when the pedestrian signal turns green and hundreds of people surge from every direction is genuinely electric.',
 'Japan Neon and Temples',
 '2025-03-27T23:55:00Z',
 'Excitement',
 'City,Nightlife,Photography'),

('Fushimi Inari Before Sunrise',
 'Climbing the thousand torii gates in near-darkness, alone except for a few other early risers — meditative, atmospheric, unforgettable.',
 'Japan Gardens and Shrines',
 '2025-11-22T05:15:00Z',
 'Awe',
 'Spiritual,Photography,Nature'),

('Chao Phraya River at Dusk',
 'Watching longtail boats cut across the river as the wat spires turned gold in the last light — Thailand delivering exactly what you hoped for.',
 'Thailand Night Markets and Temples',
 '2025-01-14T17:30:00Z',
 'Joy',
 'City,Food,Photography'),

('Chiang Mai Night Bazaar',
 'Handmade lanterns, hill tribe textiles, mango sticky rice — the Chiang Mai bazaar is overwhelming in all the right ways.',
 'Thailand Northern Craft Discovery',
 '2025-02-10T20:00:00Z',
 'Joy',
 'Shopping,Food,Nightlife'),

('Teotihuacán at Dawn',
 'Climbing the Pyramid of the Sun before any tour groups arrived, watching the light creep across the Avenue of the Dead below — completely worth the 4am alarm.',
 'Mexico Pyramids and Plazas',
 '2025-02-22T06:30:00Z',
 'Awe',
 'Study,Photography,Spiritual'),

('Guanajuato Rooftop View',
 'The multicolored city spread out in every direction from the hillside path, looking less like reality and more like a watercolor painting.',
 'Mexico Colonial Highlands',
 '2025-12-07T16:00:00Z',
 'Joy',
 'City,Photography,Romantic'),

('Grand Canyon First Look',
 'No photograph and no description in any guidebook adequately conveys what it feels like to see the Grand Canyon for the very first time.',
 'USA Coast to Canyon Adventure',
 '2025-05-04T08:00:00Z',
 'Awe',
 'Nature,Photography,Adventure'),

('New York Skyline from Brooklyn',
 'Watching the Manhattan skyline reflect in the East River at dusk from the Brooklyn Bridge Park — the city at its most cinematic.',
 'USA City Skylines Tour',
 '2025-06-23T20:30:00Z',
 'Joy',
 'City,Photography,Nightlife'),

('Bondi to Coogee Walk',
 'The coastal clifftop path from Bondi to Coogee on a clear morning is one of the best free things in the world. Full stop.',
 'Australia Surf and Harbor Escape',
 '2025-01-27T08:45:00Z',
 'Joy',
 'Beach,Nature,Relax'),

('Red Centre Silence',
 'Standing in the outback at sunset with zero other sounds — no traffic, no voices, just wind — was one of the most grounding experiences of my life.',
 'Australia Inland Discovery',
 '2025-04-14T18:15:00Z',
 'Calm',
 'Nature,Solo,Spiritual');

WITH story_locations(title, latitude, longitude) AS (
    VALUES
        ('First Morning in Lisbon', 38.7223, -9.1393),
        ('Vineyard Sunset Moment', 41.1600, -7.7900),
        ('Gaudí Overwhelm in Barcelona', 41.3851, 2.1734),
        ('Toledo at Dusk', 39.8628, -4.0273),
        ('Seine Morning Walk', 48.8566, 2.3522),
        ('Market Day in Provence', 43.9493, 4.8055),
        ('Florence at First Light', 43.7696, 11.2558),
        ('Lake Como Afternoon', 45.9870, 9.2590),
        ('Neuschwanstein Fog Day', 47.5576, 10.7498),
        ('Black Forest Trail Solo', 48.1290, 8.2330),
        ('Shibuya Crossing at Midnight', 35.6595, 139.7005),
        ('Fushimi Inari Before Sunrise', 34.9671, 135.7727),
        ('Chao Phraya River at Dusk', 13.7563, 100.5018),
        ('Chiang Mai Night Bazaar', 18.7877, 98.9931),
        ('Teotihuacán at Dawn', 19.6925, -98.8432),
        ('Guanajuato Rooftop View', 21.0190, -101.2574),
        ('Grand Canyon First Look', 36.0570, -112.1431),
        ('New York Skyline from Brooklyn', 40.7003, -73.9967),
        ('Bondi to Coogee Walk', -33.9132, 151.2749),
        ('Red Centre Silence', -25.3444, 131.0369)
)
INSERT OR IGNORE INTO stories (id, user_id, trip_id, trip_route_id, trip_place_id, emotion_id, title, description, latitude, longitude, story_time, created_at)
SELECT
    lower(
        hex(randomblob(4)) || '-' ||
        hex(randomblob(2)) || '-' ||
        '4' || substr(hex(randomblob(2)), 2) || '-' ||
        substr('89ab', abs(random()) % 4 + 1, 1) || substr(hex(randomblob(2)), 2) || '-' ||
        hex(randomblob(6))
    ) AS id,
    'f1d1a33f-1e7c-4ea1-bb1d-c8f06efb5b5a' AS user_id,
    t.id  AS trip_id,
    NULL  AS trip_route_id,
    NULL  AS trip_place_id,
    e.id  AS emotion_id,
    sd.title,
    sd.description,
    sl.latitude,
    sl.longitude,
    sd.story_time,
    strftime('%Y-%m-%dT%H:%M:%SZ', 'now') AS created_at
FROM tmp_stories_data sd
INNER JOIN story_locations sl
    ON sl.title = sd.title
INNER JOIN trips t
    ON t.title   = sd.trip_title
   AND t.user_id = 'f1d1a33f-1e7c-4ea1-bb1d-c8f06efb5b5a'
LEFT JOIN emotions e
    ON e.name = sd.emotion_name COLLATE NOCASE;

WITH RECURSIVE split_story_tags(story_title, tag_chunk, rest) AS (
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
    FROM tmp_stories_data
    WHERE tag_names IS NOT NULL AND tag_names != ''
    
    UNION ALL
    
    SELECT
        story_title,
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
    FROM split_story_tags
    WHERE rest IS NOT NULL AND rest != ''
)
INSERT OR IGNORE INTO story_tags (story_id, tag_id)
SELECT
    s.id AS story_id,
    tg.id AS tag_id
FROM split_story_tags sst
INNER JOIN stories s ON s.title = sst.story_title
INNER JOIN tags tg ON tg.name = sst.tag_chunk
WHERE tg.user_id = 'f1d1a33f-1e7c-4ea1-bb1d-c8f06efb5b5a';

DROP TABLE tmp_stories_data;