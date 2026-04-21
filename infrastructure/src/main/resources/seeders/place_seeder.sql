WITH RECURSIVE nums(n) AS (
    SELECT 1
    UNION ALL
    SELECT n + 1 FROM nums WHERE n < 20
),
countries_seed(country_name, base_lat, base_lon) AS (
    VALUES
        ('Portugal', 39.40, -8.20),
        ('Spain', 40.20, -3.70),
        ('France', 46.20, 2.20),
        ('Italy', 42.80, 12.50),
        ('Germany', 51.00, 10.40),
        ('Japan', 35.60, 139.30),
        ('Thailand', 13.70, 100.50),
        ('Mexico', 19.40, -99.10),
        ('United States', 39.50, -98.30),
        ('Australia', -33.80, 151.10)
),
place_name_seed(n, place_name) AS (
    VALUES
        (1, 'Alfama Quarter'),
        (2, 'Ribeira Promenade'),
        (3, 'Sintra Pena Palace'),
        (4, 'Sagrada Familia'),
        (5, 'Park Guell Terrace'),
        (6, 'Montmartre Hill'),
        (7, 'Louvre Courtyard'),
        (8, 'Trastevere Lanes'),
        (9, 'Ponte Vecchio Walk'),
        (10, 'Neuschwanstein Viewpoint'),
        (11, 'Brandenburg Gate'),
        (12, 'Fushimi Inari Trail'),
        (13, 'Asakusa Senso-ji'),
        (14, 'Chiang Mai Old City'),
        (15, 'Chichen Itza Complex'),
        (16, 'Teotihuacan Avenue'),
        (17, 'Grand Canyon South Rim'),
        (18, 'Golden Gate Bridge'),
        (19, 'Bondi Beachfront'),
        (20, 'Great Ocean Road Lookout')
)
INSERT OR IGNORE INTO places (
    id,
    user_id,
    country_id,
    cover_image_id,
    title,
    description,
    latitude,
    longitude
)
SELECT
    lower(
        hex(randomblob(4)) || '-' ||
        hex(randomblob(2)) || '-' ||
        '4' || substr(hex(randomblob(2)), 2) || '-' ||
        substr('89ab', abs(random()) % 4 + 1, 1) || substr(hex(randomblob(2)), 2) || '-' ||
        hex(randomblob(6))
    ) AS id,
    'f1d1a33f-1e7c-4ea1-bb1d-c8f06efb5b5a' AS user_id,
    (SELECT id FROM countries WHERE name = cs.country_name) AS country_id,
    NULL AS cover_image_id,
    printf('%s - %s %02d', cs.country_name, pns.place_name, nums.n) AS title,
    CASE (nums.n % 4)
        WHEN 0 THEN printf(
            'Visit %s in %s as stop %02d for a walk-first day focused on landmark views, practical transit, and easy evening plans.',
            pns.place_name, cs.country_name, nums.n
        )
        WHEN 1 THEN printf(
            'Use %s in %s as stop %02d for a balanced itinerary with one cultural block, one food stop, and one flexible slot for weather changes.',
            pns.place_name, cs.country_name, nums.n
        )
        WHEN 2 THEN printf(
            '%s in %s appears as stop %02d with moderate walking distance, clear sequencing, and fallback options if queues or closures affect the plan.',
            pns.place_name, cs.country_name, nums.n
        )
        ELSE printf(
            'Plan stop %02d around %s in %s for a practical mix of scenic streets, local routines, and low-friction transfers.',
            nums.n, pns.place_name, cs.country_name
        )
    END AS description,
    CASE
        WHEN nums.n IN (1, 11) THEN 0.0
        ELSE round(cs.base_lat + ((nums.n - 10) * 0.12), 6)
    END AS latitude,
    CASE
        WHEN nums.n IN (1, 11) THEN 0.0
        ELSE round(cs.base_lon + ((nums.n - 10) * 0.12), 6)
    END AS longitude
FROM countries_seed cs
CROSS JOIN nums
INNER JOIN place_name_seed pns ON pns.n = nums.n;
