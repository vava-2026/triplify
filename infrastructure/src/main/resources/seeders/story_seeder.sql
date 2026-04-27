CREATE TEMP TABLE tmp_stories_data (
    title        TEXT NOT NULL,
    description  TEXT,
    trip_title   TEXT NOT NULL,
    story_time   TEXT NOT NULL,
    emotion_name TEXT
);

INSERT INTO tmp_stories_data (title, description, trip_title, story_time, emotion_name) VALUES

('First Morning in Lisbon',
 'The trams, the tiles, the pastel de nata — everything exceeded expectations on the very first morning in this city.',
 'Portugal Coastal Discovery',
 '2025-06-02T09:30:00Z',
 'Joy'),

('Vineyard Sunset Moment',
 'Sitting above the Douro with a local white wine in hand as the valley turned amber below us — a moment of pure stillness.',
 'Portugal Wine and Hills Escape',
 '2025-07-12T19:45:00Z',
 'Awe'),

('Gaudí Overwhelm in Barcelona',
 'Nothing really prepares you for standing inside the Sagrada Família for the first time. The light through the stained glass made the whole world feel suspended.',
 'Spain Architecture Odyssey',
 '2025-03-11T11:15:00Z',
 'Awe'),

('Toledo at Dusk',
 'The old city glowed like a lantern above the plain as the sun dropped behind the Castilian hills. One of those views that stays with you.',
 'Spain Inland Heritage Tour',
 '2025-09-07T19:00:00Z',
 'Joy'),

('Seine Morning Walk',
 'An early walk along the Seine before the city woke up — bouquinistes still shuttered, pigeons everywhere, the light impossibly soft.',
 'France River and City Break',
 '2025-04-21T07:30:00Z',
 'Calm'),

('Market Day in Provence',
 'The lavender soap, the olives, the local rosé — a market in Provence is sensory overload of the best possible kind.',
 'France Southern Flavors Journey',
 '2025-08-04T10:00:00Z',
 'Joy'),

('Florence at First Light',
 'Crossing the Ponte Vecchio before dawn with nobody around felt like having one of the world''s great cities entirely to myself.',
 'Italy Art Cities Grand Tour',
 '2025-05-16T06:00:00Z',
 'Awe'),

('Lake Como Afternoon',
 'A ferry across the lake, the mountains mirrored perfectly in the water, the slow pace of everything — Italy at its most dreamy.',
 'Italy Lakes and Hilltowns',
 '2025-10-05T15:30:00Z',
 'Calm'),

('Neuschwanstein Fog Day',
 'The castle appeared and disappeared through the fog all morning. Somehow the mystery made it even more beautiful than any postcard version.',
 'Germany Castle and Rhine Trail',
 '2025-07-22T10:45:00Z',
 'Awe'),

('Black Forest Trail Solo',
 'A quiet four-hour hike through dense pine forest with no signal and no agenda — the most present I felt the entire trip.',
 'Germany Forest and Heritage Path',
 '2025-11-07T09:00:00Z',
 'Calm'),

('Shibuya Crossing at Midnight',
 'Standing in the middle of Shibuya Crossing when the pedestrian signal turns green and hundreds of people surge from every direction is genuinely electric.',
 'Japan Neon and Temples',
 '2025-03-27T23:55:00Z',
 'Excitement'),

('Fushimi Inari Before Sunrise',
 'Climbing the thousand torii gates in near-darkness, alone except for a few other early risers — meditative, atmospheric, unforgettable.',
 'Japan Gardens and Shrines',
 '2025-11-22T05:15:00Z',
 'Awe'),

('Chao Phraya River at Dusk',
 'Watching longtail boats cut across the river as the wat spires turned gold in the last light — Thailand delivering exactly what you hoped for.',
 'Thailand Night Markets and Temples',
 '2025-01-14T17:30:00Z',
 'Joy'),

('Chiang Mai Night Bazaar',
 'Handmade lanterns, hill tribe textiles, mango sticky rice — the Chiang Mai bazaar is overwhelming in all the right ways.',
 'Thailand Northern Craft Discovery',
 '2025-02-10T20:00:00Z',
 'Joy'),

('Teotihuacán at Dawn',
 'Climbing the Pyramid of the Sun before any tour groups arrived, watching the light creep across the Avenue of the Dead below — completely worth the 4am alarm.',
 'Mexico Pyramids and Plazas',
 '2025-02-22T06:30:00Z',
 'Awe'),

('Guanajuato Rooftop View',
 'The multicolored city spread out in every direction from the hillside path, looking less like reality and more like a watercolor painting.',
 'Mexico Colonial Highlands',
 '2025-12-07T16:00:00Z',
 'Joy'),

('Grand Canyon First Look',
 'No photograph and no description in any guidebook adequately conveys what it feels like to see the Grand Canyon for the very first time.',
 'USA Coast to Canyon Adventure',
 '2025-05-04T08:00:00Z',
 'Awe'),

('New York Skyline from Brooklyn',
 'Watching the Manhattan skyline reflect in the East River at dusk from the Brooklyn Bridge Park — the city at its most cinematic.',
 'USA City Skylines Tour',
 '2025-06-23T20:30:00Z',
 'Joy'),

('Bondi to Coogee Walk',
 'The coastal clifftop path from Bondi to Coogee on a clear morning is one of the best free things in the world. Full stop.',
 'Australia Surf and Harbor Escape',
 '2025-01-27T08:45:00Z',
 'Joy'),

('Red Centre Silence',
 'Standing in the outback at sunset with zero other sounds — no traffic, no voices, just wind — was one of the most grounding experiences of my life.',
 'Australia Inland Discovery',
 '2025-04-14T18:15:00Z',
 'Calm');

INSERT OR IGNORE INTO stories (id, user_id, trip_id, trip_route_id, trip_place_id, emotion_id, title, description, story_time, created_at)
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
    sd.story_time,
    datetime('now') AS created_at
FROM tmp_stories_data sd
INNER JOIN trips t
    ON t.title   = sd.trip_title
   AND t.user_id = 'f1d1a33f-1e7c-4ea1-bb1d-c8f06efb5b5a'
LEFT JOIN emotions e
    ON e.name = sd.emotion_name COLLATE NOCASE;

DROP TABLE tmp_stories_data;