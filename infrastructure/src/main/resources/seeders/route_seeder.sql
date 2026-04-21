CREATE TEMP TABLE tmp_seed_routes (
    route_title_en TEXT NOT NULL,
    route_title_sk TEXT NOT NULL,
    country_name TEXT NOT NULL,
    country_name_sk TEXT NOT NULL,
    start_n INTEGER NOT NULL,
    end_n INTEGER NOT NULL,
    focus_en TEXT NOT NULL,
    focus_sk TEXT NOT NULL
);

INSERT INTO tmp_seed_routes (route_title_en, route_title_sk, country_name, country_name_sk, start_n, end_n, focus_en, focus_sk) VALUES
('Portugal Atlantic Towns', 'Portugalské atlantické mestá', 'Portugal', 'Portugalsko', 2, 7, 'coastal neighborhoods and evening viewpoints', 'pobrežné štvrte a večerné vyhliadky'),
('Portugal Northern Valleys', 'Portugalské severné údolia', 'Portugal', 'Portugalsko', 10, 14, 'heritage streets and local train links', 'historické ulice a regionálne vlakové prepojenia'),
('Spain Sun and Stone', 'Španielsko slnko a kameň', 'Spain', 'Španielsko', 3, 8, 'historic plazas and classic old towns', 'historické námestia a staré mestá'),
('Spain Inland Discovery', 'Španielske vnútrozemské objavy', 'Spain', 'Španielsko', 11, 17, 'regional museums and slower urban rhythm', 'regionálne múzeá a pomalšie mestské tempo'),
('France Heritage Loop', 'Francúzska historická slučka', 'France', 'Francúzsko', 2, 6, 'boulevards, markets, and riverside walks', 'bulváre, trhy a prechádzky pri rieke'),
('France Southbound Line', 'Francúzska južná línia', 'France', 'Francúzsko', 9, 15, 'southern food culture and compact transfers', 'južná gastronómia a krátke presuny'),
('Italy Art and Streets', 'Talianske umenie a ulice', 'Italy', 'Taliansko', 2, 9, 'gallery stops and lively evening quarters', 'galérie a živé večerné štvrte'),
('Italy Lakes to Hills', 'Taliansko od jazier ku kopcom', 'Italy', 'Taliansko', 12, 18, 'lakeside calm and hill town panoramas', 'pokoj pri jazerách a výhľady z kopcov'),
('Germany River Cities', 'Nemecké riečne mestá', 'Germany', 'Nemecko', 2, 8, 'riverside cores and efficient rail movement', 'mestské nábrežia a efektívne vlakové presuny'),
('Germany Forest Edge', 'Nemecký lesný okraj', 'Germany', 'Nemecko', 11, 16, 'castle districts and green urban borders', 'hradné oblasti a zelené okraje miest'),
('Japan Urban Highlights', 'Japonské mestské highlighty', 'Japan', 'Japonsko', 2, 10, 'district variety and late-night food lanes', 'rôznorodé štvrte a večerné ulice s jedlom'),
('Japan Temple Corridor', 'Japonský chrámový koridor', 'Japan', 'Japonsko', 12, 19, 'temple gardens and traditional shopping streets', 'chrámové záhrady a tradičné nákupné ulice'),
('Thailand City and Coast', 'Thajsko mesto a pobrežie', 'Thailand', 'Thajsko', 2, 7, 'urban markets and relaxed coastal evenings', 'mestské trhy a pokojné pobrežné večery'),
('Thailand Northern Culture', 'Thajská severná kultúra', 'Thailand', 'Thajsko', 10, 14, 'temples, crafts, and regional night bazaars', 'chrámy, remeslá a regionálne nočné trhy'),
('Mexico Central Circuit', 'Mexický centrálny okruh', 'Mexico', 'Mexiko', 2, 8, 'city plazas and practical overland legs', 'mestské námestia a praktické pozemné presuny'),
('Mexico Colonial Trail', 'Mexická koloniálna trasa', 'Mexico', 'Mexiko', 11, 16, 'colonial facades and hillside viewpoints', 'koloniálne fasády a vyhliadky z kopcov'),
('US East Corridor', 'Východný koridor USA', 'United States', 'Spojené štáty', 2, 7, 'major landmarks with short intercity hops', 'hlavné pamiatky a krátke medzimestské presuny'),
('US West Urban Arc', 'Západný mestský oblúk USA', 'United States', 'Spojené štáty', 10, 15, 'coastal districts and flexible day trips', 'pobrežné štvrte a flexibilné denné výlety'),
('Australia Coastal Days', 'Austrálske pobrežné dni', 'Australia', 'Austrália', 2, 6, 'waterfront parks and easy city flow', 'nábrežné parky a plynulé mestské tempo'),
('Australia Inland Stretch', 'Austrálsky vnútrozemský úsek', 'Australia', 'Austrália', 9, 13, 'regional galleries and dryland landscapes', 'regionálne galérie a vnútrozemské scenérie');

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
    printf('%s | %s', ts.route_title_en, ts.route_title_sk),
    printf(
        'EN: This route in %s is intentionally regional, covering places %d to %d so the journey feels coherent from morning to evening. Its central theme is %s, and every stop is selected to keep transfers realistic, avoid exhausting jumps, and leave room for food breaks, weather changes, and spontaneous discoveries. You can run it as a compact city-focused plan or stretch it across several days with slower pacing and longer cultural visits. The structure favors practical transport links, predictable service availability, and safe evening movement. SK: Táto trasa v krajine %s je zámerne regionálna a prepája miesta %d až %d tak, aby cesta pôsobila logicky od rána do večera. Jej hlavnou témou sú %s a jednotlivé zastávky sú zvolené tak, aby boli presuny realistické, bez únavných skokov, a zároveň ostal čas na jedlo, zmeny podľa počasia aj neplánované objavy. Trasu je možné absolvovať ako kompaktný mestský plán alebo ju rozložiť na viac dní s pomalším tempom a dlhšími kultúrnymi návštevami. Návrh uprednostňuje praktické dopravné väzby, stabilnú dostupnosť služieb a bezpečný pohyb aj vo večerných hodinách.',
        ts.country_name,
        ts.start_n,
        ts.end_n,
        ts.focus_en,
        ts.country_name_sk,
        ts.start_n,
        ts.end_n,
        ts.focus_sk
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
       AND r.title = printf('%s | %s', ts.route_title_en, ts.route_title_sk)
    INNER JOIN countries c
        ON c.name = ts.country_name
    INNER JOIN places p
        ON p.user_id = 'f1d1a33f-1e7c-4ea1-bb1d-c8f06efb5b5a'
       AND p.country_id = c.id
       AND p.title GLOB '* Miesto ??'
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
