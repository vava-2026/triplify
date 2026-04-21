INSERT OR IGNORE INTO users (
    id,
    username,
    email,
    password_hash,
    role
) VALUES (
    '00000000-0000-0000-0000-000000000001',
    'system',
    'system@triplify.local',
    'seeded-system-user',
    'configuration manager'
);

INSERT OR IGNORE INTO categories (
    id,
    created_by,
    name,
    name_sk,
    description,
    description_sk,
    emoji_unicode,
    color
) VALUES
    (
        '10000000-0000-0000-0000-000000000001',
        '00000000-0000-0000-0000-000000000001',
        'Culture',
        'Kultura',
        'Trips focused on museums, landmarks and local heritage.',
        'Vylety zamerane na muzea, pamiatky a miestne dedicstvo.',
        'U+1F3DB',
        'purple'
    ),
    (
        '10000000-0000-0000-0000-000000000002',
        '00000000-0000-0000-0000-000000000001',
        'Tourism',
        'Turizmus',
        'General sightseeing and destination discovery.',
        'Vseobecne spoznavanie miest a destinacii.',
        'U+2708',
        'blue'
    ),
    (
        '10000000-0000-0000-0000-000000000003',
        '00000000-0000-0000-0000-000000000001',
        'Nature',
        'Priroda',
        'Trips focused on landscapes, parks and outdoor experiences.',
        'Vylety zamerane na krajinu, parky a pobyt vonku.',
        'U+1F333',
        'green'
    ),
    (
        '10000000-0000-0000-0000-000000000004',
        '00000000-0000-0000-0000-000000000001',
        'Relax',
        'Oddych',
        'Slow-paced leisure trips and rest-focused escapes.',
        'Oddychove cesty a pomalejsie pobyty.',
        'U+1F6CB',
        'yellow'
    ),
    (
        '10000000-0000-0000-0000-000000000005',
        '00000000-0000-0000-0000-000000000001',
        'Memorial',
        'Pamiatka',
        'Trips dedicated to remembrance, history and reflection.',
        'Cesty venovane spomienkam, historii a zamysleniu.',
        'U+1F54A',
        'gray'
    ),
    (
        '10000000-0000-0000-0000-000000000006',
        '00000000-0000-0000-0000-000000000001',
        'Food',
        'Jedlo',
        'Trips built around cuisine, cafes and local specialties.',
        'Cesty zamerane na gastronomiu, kaviarne a miestne speciality.',
        'U+1F37D',
        'orange'
    );
