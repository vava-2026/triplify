package com.triplify.infrastructure.unsplash;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.List;

public class UnsplashImageDownloader {

    // Configuration
    private static final String ENV_KEY      = "UNSPLASH_ACCESS_KEY";
    private static final String PROPERTY_KEY = "triplify.unsplash.key";
    private static final long   DELAY_MS     = 1_300; // ~46 req/min, free tier = 50 req/hr

    /** Output directory — resolves to infrastructure/src/main/resources/seeders/place_images/ */
    private static final Path OUTPUT_DIR = Path.of("infrastructure", "src", "main", "resources",
            "seeders", "place_images");

    // Routes
    private static final List<String> ROUTE_TITLES = List.of(
            "Portugal Atlantic Heritage Arc", "Portugal Hills and Vineyards Line",
            "Spain Gaudi to Gothic Trail", "Spain Inland Culture Route",
            "France River and Boulevard Circuit", "France Southbound Flavor Route",
            "Italy Art Cities Sequence", "Italy Lakes to Hilltown Journey",
            "Germany Castle and River Loop", "Germany Forest Edge Itinerary",
            "Japan Neon to Temple Corridor", "Japan Garden and Shrine Line",
            "Thailand Night Market Route", "Thailand Northern Craft Trail",
            "Mexico Plaza and Pyramid Circuit", "Mexico Colonial Highlands Route",
            "United States Coast to Canyon Line", "United States Urban Skylines Arc",
            "Australia Surf and Harbor Route", "Australia Inland Discovery Line"
    );

    // Stories
    private static final List<String> STORY_TITLES = List.of(
            "First Morning in Lisbon", "Vineyard Sunset Moment", "Gaudí Overwhelm in Barcelona",
            "Toledo at Dusk", "Seine Morning Walk", "Market Day in Provence",
            "Florence at First Light", "Lake Como Afternoon", "Neuschwanstein Fog Day",
            "Black Forest Trail Solo", "Shibuya Crossing at Midnight", "Fushimi Inari Before Sunrise",
            "Chao Phraya River at Dusk", "Chiang Mai Night Bazaar", "Teotihuacán at Dawn",
            "Guanajuato Rooftop View", "Grand Canyon First Look", "New York Skyline from Brooklyn",
            "Bondi to Coogee Walk", "Red Centre Silence"
    );

    // Places
    private static final List<String> PLACE_TITLES = List.of(
            "Portugal - Alfama District #01",
            "Portugal - Belem Tower #02",
            "Portugal - Jeronimos Monastery #03",
            "Portugal - Ribeira Waterfront #04",
            "Portugal - Livraria Lello #05",
            "Portugal - Pena Palace #06",
            "Portugal - Quinta da Regaleira #07",
            "Portugal - Douro Valley Viewpoint #08",
            "Portugal - Nazare Cliffs #09",
            "Portugal - Obidos Castle Walls #10",
            "Portugal - Porto Sao Bento #11",
            "Portugal - Evora Roman Temple #12",
            "Portugal - Coimbra University Courtyard #13",
            "Portugal - Aveiro Canals #14",
            "Portugal - Cascais Marina #15",
            "Portugal - Sintra Old Town #16",
            "Portugal - Ponta da Piedade #17",
            "Portugal - Madeira Monte Palace #18",
            "Portugal - Bom Jesus do Monte #19",
            "Portugal - Guimaraes Castle #20",
            "Spain - Sagrada Familia #01",
            "Spain - Park Guell #02",
            "Spain - Alhambra #03",
            "Spain - Plaza Mayor Madrid #04",
            "Spain - Royal Alcazar Seville #05",
            "Spain - City of Arts and Sciences #06",
            "Spain - Toledo Old Town #07",
            "Spain - Santiago Cathedral #08",
            "Spain - Guggenheim Bilbao #09",
            "Spain - Puente Nuevo Ronda #10",
            "Spain - Mezquita of Cordoba #11",
            "Spain - Segovia Aqueduct #12",
            "Spain - Costa Brava Coves #13",
            "Spain - Dalt Vila Ibiza #14",
            "Spain - Montserrat Monastery #15",
            "Spain - La Concha Bay #16",
            "Spain - Camino Frances Stretch #17",
            "Spain - Malaga Alcazaba #18",
            "Spain - Salamanca Plaza Mayor #19",
            "Spain - Girona Jewish Quarter #20",
            "France - Montmartre Steps #01",
            "France - Louvre Courtyard #02",
            "France - Eiffel Trocadero #03",
            "France - Versailles Gardens #04",
            "France - Annecy Old Canals #05",
            "France - Carcassonne Citadel #06",
            "France - Nice Promenade des Anglais #07",
            "France - Lyon Old Town #08",
            "France - Bordeaux Mirror Pool #09",
            "France - Avignon Palace of Popes #10",
            "France - Etretat Cliffs #11",
            "France - Colmar Little Venice #12",
            "France - Saint-Malo Ramparts #13",
            "France - Loire Chateau Chambord #14",
            "France - Arles Roman Arena #15",
            "France - Chamonix Valley Walk #16",
            "France - Nimes Maison Carree #17",
            "France - Aix Cours Mirabeau #18",
            "France - Rocamadour Sanctuary #19",
            "France - Strasbourg Petite France #20",
            "Italy - Trastevere Lanes #01",
            "Italy - Ponte Vecchio #02",
            "Italy - Duomo di Milano #03",
            "Italy - Colosseum Forum Edge #04",
            "Italy - Cinque Terre Vernazza #05",
            "Italy - Amalfi Cathedral Steps #06",
            "Italy - Lake Como Bellagio #07",
            "Italy - Siena Piazza del Campo #08",
            "Italy - Matera Sassi Quarter #09",
            "Italy - Bologna Porticoes #10",
            "Italy - Verona Arena #11",
            "Italy - Pompeii Ruins #12",
            "Italy - Turin Mole Antonelliana #13",
            "Italy - Orvieto Funicular Gate #14",
            "Italy - Perugia Rocca Paolina #15",
            "Italy - Ravenna Mosaics #16",
            "Italy - Capri Gardens of Augustus #17",
            "Italy - Lecce Baroque Center #18",
            "Italy - Trieste Canal Grande #19",
            "Italy - Palermo Ballaro Market #20",
            "Germany - Brandenburg Gate #01",
            "Germany - Neuschwanstein Overlook #02",
            "Germany - Cologne Cathedral Square #03",
            "Germany - Heidelberg Castle Terrace #04",
            "Germany - Rothenburg Walls #05",
            "Germany - Hamburg Speicherstadt #06",
            "Germany - Dresden Zwinger #07",
            "Germany - Black Forest Triberg #08",
            "Germany - Munich Marienplatz #09",
            "Germany - Nuremberg Castle Yard #10",
            "Germany - Bamberg Old Town #11",
            "Germany - Leipzig Thomaskirche Area #12",
            "Germany - Potsdam Sanssouci #13",
            "Germany - Saxon Switzerland Bastei #14",
            "Germany - Moselle Cochem Promenade #15",
            "Germany - Frankfurt Romerberg #16",
            "Germany - Freiburg Minster Square #17",
            "Germany - Lubeck Holsten Gate #18",
            "Germany - Wurzburg Residence Garden #19",
            "Germany - Berchtesgaden Eagle View #20",
            "Japan - Fushimi Inari Trail #01",
            "Japan - Asakusa Senso-ji #02",
            "Japan - Shibuya Scramble #03",
            "Japan - Arashiyama Bamboo Grove #04",
            "Japan - Himeji Castle #05",
            "Japan - Nara Deer Park #06",
            "Japan - Kanazawa Kenrokuen #07",
            "Japan - Miyajima Floating Gate #08",
            "Japan - Hakone Lake Ashi #09",
            "Japan - Nikko Toshogu #10",
            "Japan - Kamakura Great Buddha #11",
            "Japan - Osaka Dotonbori #12",
            "Japan - Takayama Old Streets #13",
            "Japan - Kobe Harborland #14",
            "Japan - Nagasaki Glover Garden #15",
            "Japan - Sapporo Odori Park #16",
            "Japan - Aomori Nebuta Museum #17",
            "Japan - Koyasan Okunoin #18",
            "Japan - Beppu Steam District #19",
            "Japan - Okinawa Shuri Castle #20",
            "Thailand - Chiang Mai Old City #01",
            "Thailand - Grand Palace Bangkok #02",
            "Thailand - Wat Arun Riverside #03",
            "Thailand - Ayutthaya Historical Park #04",
            "Thailand - Pai Canyon #05",
            "Thailand - Phuket Old Town #06",
            "Thailand - Krabi Railay Beach #07",
            "Thailand - Sukhothai Ruins #08",
            "Thailand - Kanchanaburi Bridge #09",
            "Thailand - Khao Sok Lake Pier #10",
            "Thailand - Koh Samui Fisherman Village #11",
            "Thailand - Koh Tao Viewpoint #12",
            "Thailand - Hua Hin Night Market #13",
            "Thailand - Lampang Horse Carriage Quarter #14",
            "Thailand - Trat Mangrove Walk #15",
            "Thailand - Udon Thani Red Lotus Lake #16",
            "Thailand - Pattaya Sanctuary of Truth #17",
            "Thailand - Mae Hong Son Loop Stop #18",
            "Thailand - Phang Nga Bay Pier #19",
            "Thailand - Surat Thani Riverfront #20",
            "Mexico - Chichen Itza Plaza #01",
            "Mexico - Teotihuacan Avenue of the Dead #02",
            "Mexico - Zocalo Mexico City #03",
            "Mexico - Guanajuato Alleyways #04",
            "Mexico - Oaxaca Santo Domingo #05",
            "Mexico - Puebla Historic Center #06",
            "Mexico - Merida Paseo Montejo #07",
            "Mexico - Tulum Ruins Coast #08",
            "Mexico - Bacalar Lagoon Deck #09",
            "Mexico - San Miguel de Allende Parroquia #10",
            "Mexico - Copper Canyon Rim #11",
            "Mexico - Isla Mujeres Norte Beach #12",
            "Mexico - Campeche Walled Streets #13",
            "Mexico - Queretaro Aqueduct Walk #14",
            "Mexico - Morelia Cathedral Plaza #15",
            "Mexico - Veracruz Malecon #16",
            "Mexico - Holbox Sandbar #17",
            "Mexico - Cancun El Rey Site #18",
            "Mexico - Toluca Cosmovitral #19",
            "Mexico - Monterrey Santa Lucia Walk #20",
            "United States - Grand Canyon South Rim #01",
            "United States - Golden Gate Bridge #02",
            "United States - Times Square Broadway Block #03",
            "United States - National Mall Reflecting Pool #04",
            "United States - French Quarter Jackson Square #05",
            "United States - Chicago Riverwalk #06",
            "United States - Yosemite Valley Floor #07",
            "United States - Monument Valley Overlook #08",
            "United States - Antelope Canyon Entrance #09",
            "United States - Miami South Beach #10",
            "United States - Seattle Pike Place #11",
            "United States - Boston Freedom Trail Segment #12",
            "United States - San Diego Balboa Park #13",
            "United States - Nashville Broadway Strip #14",
            "United States - Charleston Battery #15",
            "United States - Sedona Cathedral Rock #16",
            "United States - Portland Waterfront Park #17",
            "United States - Austin South Congress #18",
            "United States - Philadelphia Old City #19",
            "United States - Savannah Forsyth Park #20",
            "Australia - Bondi Beachfront #01",
            "Australia - Great Ocean Road Lookout #02",
            "Australia - Sydney Harbour Bridge Pylon #03",
            "Australia - Melbourne Laneways Center #04",
            "Australia - Uluru Sunset Viewing Area #05",
            "Australia - Blue Mountains Echo Point #06",
            "Australia - Fremantle Cappuccino Strip #07",
            "Australia - Brisbane South Bank #08",
            "Australia - Adelaide Central Market #09",
            "Australia - Hobart Salamanca Place #10",
            "Australia - Cairns Esplanade Lagoon #11",
            "Australia - Byron Bay Lighthouse Path #12",
            "Australia - Canberra Lake Burley Walk #13",
            "Australia - Kakadu Ubirr Lookout #14",
            "Australia - Noosa Main Beach #15",
            "Australia - Darwin Waterfront Precinct #16",
            "Australia - Margaret River Cellar Trail #17",
            "Australia - Launceston Cataract Gorge #18",
            "Australia - Gold Coast Burleigh Headland #19",
            "Australia - Townsville The Strand #20"
    );

    // Main
    public static void main(String[] args) throws Exception {
        String accessKey = resolveAccessKey();
        HttpClient http  = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        Files.createDirectories(OUTPUT_DIR);

        System.out.printf("%nTriplify — Unsplash image downloader%n");
        System.out.printf("Output: %s%n", OUTPUT_DIR.toAbsolutePath());

        // 1. Download Places
        downloadCategory(http, accessKey, PLACE_TITLES, "");

        // 2. Download Routes
        downloadCategory(http, accessKey, ROUTE_TITLES, "routes");

        // 3. Download Stories
        downloadCategory(http, accessKey, STORY_TITLES, "stories");

        System.out.printf("%nAll categories processed.%n");
        System.out.printf("%nFiles saved to:%n  %s%n%n", OUTPUT_DIR.toAbsolutePath());
    }

    // Unsplash API
    private static String fetchPhotoUrl(HttpClient http, String accessKey, String query)
            throws IOException, InterruptedException {

        String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url     = "https://api.unsplash.com/search/photos?query=" + encoded
                + "&per_page=3&orientation=landscape&content_filter=high";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Client-ID " + accessKey)
                .header("Accept-Version", "v1")
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            System.err.printf("Unsplash API %d for query \"%s\"%n", response.statusCode(), query);
            return null;
        }

        JsonObject body    = JsonParser.parseString(response.body()).getAsJsonObject();
        JsonArray  results = body.getAsJsonArray("results");
        if (results == null || results.isEmpty()) return null;

        JsonObject photo            = results.get(0).getAsJsonObject();
        String     photoUrl         = photo.getAsJsonObject("urls").get("regular").getAsString();
        String     downloadLocation = photo.getAsJsonObject("links").get("download_location").getAsString();

        // Required by Unsplash ToS — fire and forget
        http.sendAsync(
                HttpRequest.newBuilder()
                        .uri(URI.create(downloadLocation + "?client_id=" + accessKey))
                        .timeout(Duration.ofSeconds(10))
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.discarding()
        );

        return photoUrl;
    }

    private static void downloadToFile(HttpClient http, String photoUrl, Path dest)
            throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(photoUrl))
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();

        HttpResponse<InputStream> response =
                http.send(request, HttpResponse.BodyHandlers.ofInputStream());

        if (response.statusCode() != 200) {
            throw new IOException("Photo download failed, status=" + response.statusCode());
        }

        try (InputStream in = response.body()) {
            Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void downloadCategory(HttpClient http, String accessKey, List<String> titles, String subFolder) throws IOException, InterruptedException {
        Path categoryPath = OUTPUT_DIR.resolve(subFolder);
        if (!Files.exists(categoryPath)) {
            Files.createDirectories(categoryPath);
        }

        System.out.printf("%nProcessing category: %s (%d items)%n", subFolder.isEmpty() ? "Places" : subFolder, titles.size());
        int success = 0, skipped = 0, failed = 0;

        for (int i = 0; i < titles.size(); i++) {
            String title = titles.get(i);
            String fileName = toFileName(title);
            Path destFile = categoryPath.resolve(fileName);
            String progress = String.format("[%3d/%d]", i + 1, titles.size());

            if (Files.exists(destFile)) {
                System.out.printf("%s already exists: %s%n", progress, fileName);
                skipped++;
                continue;
            }

            String query = buildQuery(title);
            System.out.printf("%s \"%s\"  →  \"%s\" … ", progress, title, query);

            try {
                String photoUrl = fetchPhotoUrl(http, accessKey, query);

                if (photoUrl == null && !subFolder.equals("routes") && !subFolder.equals("stories")) {
                    String fallback = countryFromTitle(title);
                    System.out.printf("[fallback: \"%s\"] … ", fallback);
                    photoUrl = fetchPhotoUrl(http, accessKey, fallback);
                }

                if (photoUrl == null) {
                    System.out.println("no results");
                    failed++;
                    sleep(DELAY_MS);
                    continue;
                }

                downloadToFile(http, photoUrl, destFile);
                System.out.println("✓");
                success++;

            } catch (Exception e) {
                System.out.printf("%nError: %s%n", e.getMessage());
                failed++;
            }

            sleep(DELAY_MS);
        }
        System.out.printf("Category %s done: %d downloaded, %d skipped, %d failed.%n", subFolder.isEmpty() ? "Places" : subFolder, success, skipped, failed);
    }

    /** "Portugal - Alfama District #01" → "Alfama District" */
    private static String buildQuery(String title) {
        String withoutNumber = title.replaceAll("#\\d+$", "").trim();
        String[] parts = withoutNumber.split(" - ", 2);
        return parts.length == 2 ? parts[1].trim() : withoutNumber;
    }

    /** "Portugal - Alfama District #01" → "Portugal" */
    private static String countryFromTitle(String title) {
        return title.split(" - ", 2)[0].trim();
    }

    /** Sanitize title for use as filename (Windows-safe). */
    private static String toFileName(String title) {
        return title.replaceAll("[\\\\/:*?\"<>|]", "_") + ".jpg";
    }

    private static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    private static String resolveAccessKey() {
        String key = System.getProperty(PROPERTY_KEY);
        if (key == null || key.isBlank()) key = System.getenv(ENV_KEY);
        if (key == null || key.isBlank()) {
            throw new IllegalStateException(
                    "Unsplash access key not set.\n" +
                            "Add to Run Configuration → Environment Variables:\n" +
                            "  UNSPLASH_ACCESS_KEY=your_key_here"
            );
        }
        return key.trim();
    }
}