WITH RECURSIVE nums(n) AS (
    SELECT 1
    UNION ALL
    SELECT n + 1 FROM nums WHERE n < 20
),
countries_seed(country_name, country_name_sk, base_lat, base_lon) AS (
    VALUES
        ('Portugal', 'Portugalsko', 39.40, -8.20),
        ('Spain', 'Španielsko', 40.20, -3.70),
        ('France', 'Francúzsko', 46.20, 2.20),
        ('Italy', 'Taliansko', 42.80, 12.50),
        ('Germany', 'Nemecko', 51.00, 10.40),
        ('Japan', 'Japonsko', 35.60, 139.30),
        ('Thailand', 'Thajsko', 13.70, 100.50),
        ('Mexico', 'Mexiko', 19.40, -99.10),
        ('United States', 'Spojené štáty', 39.50, -98.30),
        ('Australia', 'Austrália', -33.80, 151.10)
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
    printf('%s Place %02d | %s Miesto %02d', cs.country_name, nums.n, cs.country_name_sk, nums.n) AS title,
    CASE (nums.n % 4)
        WHEN 0 THEN printf(
            'EN: Start this stop in %s with an early walk through residential streets before the busy hours begin. The plan is built for people who like balanced days: one iconic viewpoint, one neighborhood market, one slower café break, and enough time to adapt if the weather changes. Midday is ideal for a local museum or a compact history site, followed by a practical lunch close to transit. In the evening, the area works well for a relaxed dinner, short photo walk, and easy return to accommodation without long transfers. This entry is numbered %02d and intentionally favors realism over rushed checklists, so the route remains useful for solo travel, couples, or small groups. SK: Túto zastávku v krajine %s začnite rannou prechádzkou pokojnými ulicami ešte pred hlavnou špičkou. Program je nastavený vyvážene: jeden známy bod, jeden lokálny trh, jedna pomalšia kávová pauza a dosť priestoru na zmenu plánu podľa počasia. Okolo obeda sa hodí menšie múzeum alebo historické miesto, potom praktický obed blízko dopravy. Večer je vhodný na pokojnú večeru, krátku foto prechádzku a jednoduchý návrat na ubytovanie bez zdĺhavých presunov.',
            cs.country_name, nums.n, cs.country_name_sk
        )
        WHEN 1 THEN printf(
            'EN: This place in %s is designed as a full-day base with clear pacing and low friction between activities. Morning hours fit architecture walks and local bakeries, while late morning can be used for a cultural stop that does not require strict timing. Afternoon time is reserved for practical travel tasks, scenic streets, and one flexible block for shopping or rest. Around sunset, the route shifts toward food streets and calmer corners where the atmosphere remains lively but not chaotic. Entry %02d keeps logistics simple on purpose: short transfers, predictable services, and room for spontaneous decisions. SK: Toto miesto v krajine %s je pripravené ako celodenná základňa s jasným tempom a bez zbytočného stresu medzi aktivitami. Ráno je vhodné na architektúru a miestne pekárne, neskôr na kultúrnu zastávku bez prísnych časových okien. Popoludní je priestor na praktické presuny, scénické ulice a jeden flexibilný blok na nákupy alebo oddych. Pri západe slnka sa program presúva k uliciam s jedlom a pokojnejším zákutiam, kde je živá, ale nie chaotická atmosféra.',
            cs.country_name, nums.n, cs.country_name_sk
        )
        WHEN 2 THEN printf(
            'EN: Use this stop in %s when you want a mixed urban day that connects landmarks with everyday local life. The itinerary begins with a short transit-friendly loop, continues with a curated set of two to three highlights, and leaves margin for weather, queues, or unexpected recommendations from locals. A practical lunch window is included near transport nodes, making onward movement easy. Late afternoon works best for parks, river walks, or compact districts with strong visual character. Stop %02d is intentionally structured with moderate walking distance and clear fallback options if a venue is closed. SK: Túto zastávku v krajine %s využite vtedy, keď chcete kombinovať známe miesta s bežným mestským životom. Itinerár začína krátkym okruhom s dobrým napojením na dopravu, pokračuje dvoma až troma hlavnými bodmi a necháva rezervu na počasie, rady aj nečakané tipy od miestnych. Obed je plánovaný pri dopravných uzloch, aby bol ďalší presun jednoduchý. Podvečer sa hodia parky, nábrežia alebo menšie štvrte so silným vizuálnym charakterom.',
            cs.country_name, nums.n, cs.country_name_sk
        )
        ELSE printf(
            'EN: Choose this location in %s for a calm, practical day that still feels memorable. The morning section focuses on short walking links and easy orientation, then transitions into a thematic block such as food, design, or history depending on current events. Midday allows a longer break to prevent fatigue and keep the second half of the day enjoyable. Evening recommendations prioritize safe routes, good lighting, and places where you can sit longer without pressure. Entry %02d is written for repeat use: the structure stays stable, while specific venues can be swapped by season. SK: Túto lokalitu v krajine %s si vyberte na pokojný a praktický deň, ktorý je zároveň zapamätateľný. Ráno je postavené na krátkych peších prepojeniach a jednoduchej orientácii, potom nasleduje tematický blok podľa aktuálnej ponuky, napríklad gastronómia, dizajn alebo história. Na obed je plánovaná dlhšia pauza, aby druhá polovica dňa zostala príjemná. Večerné odporúčania uprednostňujú bezpečné trasy, dobré osvetlenie a miesta, kde sa dá zostať dlhšie bez stresu.',
            cs.country_name, nums.n, cs.country_name_sk
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
CROSS JOIN nums;
