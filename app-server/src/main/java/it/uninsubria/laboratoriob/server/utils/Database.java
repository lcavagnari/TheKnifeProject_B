package it.uninsubria.laboratoriob.server.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.experimental.UtilityClass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.UUID;

/**
 * Gestione della connessione al database PostgreSQL e inizializzazione dello schema.
 * <p>
 * Utilizza HikariCP come connection pool per gestire le connessioni in modo efficiente.
 * Fornisce metodi per inizializzare le tabelle del database, inserire i dati costanti
 * (fasce di prezzo, premi, tipi di cucina, servizi) e ottenere connessioni dal pool.
 * </p>
 *
 * <h2>Responsabilità</h2>
 * <ul>
 *   <li>Creazione e configurazione del connection pool HikariCP.</li>
 *   <li>Inizializzazione dello schema del database (DDL) con {@code CREATE TABLE IF NOT EXISTS}.</li>
 *   <li>Popolamento delle tabelle di lookup con dati costanti.</li>
 *   <li>Fornitura di connessioni sicure tramite {@link #getConnection()}.</li>
 *   <li>Chiusura controllata del pool alla terminazione dell'applicazione.</li>
 * </ul>
 *
 * @author Luca Cavagnari
 * @version 2.0
 * @see com.zaxxer.hikari.HikariDataSource
 */
@UtilityClass
public final class Database {

    private static final String URL = "jdbc:postgresql://localhost:5432/mydb";
    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "test1234";

    private static final HikariDataSource ds;

    static {
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl(URL);
        config.setUsername(USERNAME);
        config.setPassword(PASSWORD);

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        config.setMaximumPoolSize(10);
        config.setConnectionTimeout(30000);
        config.setMinimumIdle(2);
        config.setInitializationFailTimeout(0);

        ds = new HikariDataSource(config);
    }

    public static boolean initTables() {
        // blocking
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS price_range (
                        id INT PRIMARY KEY,
                        min DECIMAL(10, 2) NOT NULL
                            CONSTRAINT chk_price_min CHECK (min >= 0),
                        max DECIMAL(10, 2) NOT NULL
                            CONSTRAINT chk_price_max CHECK (max >= min),
                        description VARCHAR(50) CHECK (description <> '')
                    );
                    """);

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS cuisine_type (
                        id INT PRIMARY KEY,
                        description VARCHAR(100) NOT NULL CHECK (description <> '')
                    );
                    """);

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS awards (
                        id INT PRIMARY KEY,
                        description VARCHAR(100) NOT NULL CHECK (description <> '')
                    );
                    """);

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS services_and_facilities (
                        id INT PRIMARY KEY,
                        description TEXT NOT NULL CHECK (description <> '')
                    );
                    """);

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS location (
                        latitude DOUBLE PRECISION NOT NULL
                            CHECK (latitude BETWEEN -90.0 AND 90.0),
                    
                        longitude DOUBLE PRECISION NOT NULL
                            CHECK (longitude BETWEEN -180.0 AND 180.0),
                    
                        city VARCHAR(100) CHECK (city <> ''),
                        country CHAR(2) CHECK (country ~ '^[A-Z]{2}$'),
                        address VARCHAR(255) CHECK (address <> ''),
                    
                        PRIMARY KEY (latitude, longitude)
                    );
                    """);

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS "user" (
                        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                        username VARCHAR(50) NOT NULL UNIQUE CHECK (username <> ''),
                        psw_hash VARCHAR(255) NOT NULL CHECK (psw_hash <> ''),
                        psw_salt VARCHAR(255) NOT NULL CHECK (psw_salt <> ''),
                        first_name VARCHAR(100) CHECK (first_name <> ''),
                        last_name VARCHAR(100) CHECK (last_name <> ''),
                        birth_date DATE CHECK (birth_date <= CURRENT_DATE),
                        latitude DOUBLE PRECISION,
                        longitude DOUBLE PRECISION,
                        is_owner BOOLEAN NOT NULL,
                        is_system BOOLEAN NOT NULL DEFAULT false,
                        registered_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    
                        FOREIGN KEY (latitude, longitude)
                            REFERENCES location (latitude, longitude)
                    );
                    """);

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS restaurant (
                        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                        owner_id UUID NOT NULL,
                    
                        name VARCHAR(100) NOT NULL CHECK (name <> ''),
                        description TEXT CHECK (description <> ''),
                        web_url TEXT CHECK (web_url <> ''),
                        phone_number TEXT CHECK (phone_number <> ''),
                        price_range INT,
                    
                        award INT NOT NULL DEFAULT 0,
                        green_star BOOLEAN NOT NULL DEFAULT false,
                    
                        has_delivery BOOLEAN NOT NULL DEFAULT false,
                        has_booking BOOLEAN NOT NULL DEFAULT false,
                    
                        latitude DOUBLE PRECISION,
                        longitude DOUBLE PRECISION,
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                            CHECK (created_at <= CURRENT_TIMESTAMP),
                    
                        FOREIGN KEY (award) REFERENCES awards(id),
                        FOREIGN KEY (owner_id) REFERENCES "user"(id),
                        FOREIGN KEY (price_range) REFERENCES price_range(id),
                        FOREIGN KEY (latitude, longitude)
                            REFERENCES location (latitude, longitude)
                    );
                    """);

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS user_favorites (
                        user_id UUID NOT NULL,
                        restaurant_id UUID NOT NULL,
                    
                        PRIMARY KEY (user_id, restaurant_id),
                    
                        FOREIGN KEY (user_id) REFERENCES "user"(id),
                        FOREIGN KEY (restaurant_id) REFERENCES restaurant(id)
                    );
                    """);

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS user_restaurants (
                        user_id UUID NOT NULL,
                        restaurant_id UUID NOT NULL,
                        created_at TIMESTAMP NOT NULL DEFAULT now(),

                        PRIMARY KEY (user_id, restaurant_id),

                        FOREIGN KEY (user_id) REFERENCES "user"(id),
                        FOREIGN KEY (restaurant_id) REFERENCES restaurant(id)
                    );
                    """);

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS restaurant_cuisine (
                        restaurant_id UUID NOT NULL,
                        type INT NOT NULL,
                    
                        PRIMARY KEY (restaurant_id, type),
                    
                        FOREIGN KEY (restaurant_id) REFERENCES restaurant(id),
                        FOREIGN KEY (type) REFERENCES cuisine_type(id)
                    );
                    """);

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS restaurant_services (
                        restaurant_id UUID NOT NULL,
                        service INT NOT NULL,
                    
                        PRIMARY KEY (restaurant_id, service),
                    
                        FOREIGN KEY (restaurant_id)
                            REFERENCES restaurant(id),
                        FOREIGN KEY (service)
                            REFERENCES services_and_facilities(id)
                    );
                    """);

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS review (
                        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                        user_id UUID NOT NULL,
                        restaurant_id UUID NOT NULL,
                        rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
                        text TEXT NOT NULL CHECK (text <> ''),
                        response TEXT CHECK (response <> ''),
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        responded_at TIMESTAMP,
                    
                        UNIQUE (user_id, restaurant_id),
                    
                        FOREIGN KEY (user_id) REFERENCES "user"(id),
                        FOREIGN KEY (restaurant_id) REFERENCES restaurant(id),
                    
                        CONSTRAINT chk_response_completeness
                            CHECK (
                                (response IS NULL AND responded_at IS NULL) OR
                                (response IS NOT NULL AND responded_at IS NOT NULL)
                            )
                    );
                    """);

            return true;

        } catch (SQLException e) {
            System.err.println(
                    "Errore durante l'inizializzazione del database: "
                            + e.getMessage()
            );
            return false;
        }
    }

    public static boolean initialiseConstants() {
        // blocking
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("""
                    INSERT INTO price_range (id, min, max, description)
                    VALUES (1, 0.00, 25.00, 'economy'),
                           (2, 25.00, 50.00, 'moderate'),
                           (3, 50.00, 100.00, 'expensive'),
                           (4, 100.00, 2000000, 'luxury')
                    ON CONFLICT (id) DO NOTHING;
                    """);

            stmt.execute("""
                    INSERT INTO awards (id, description)
                    VALUES (0, 'None'),
                           (1, '1 star'),
                           (2, '2 stars'),
                           (3, '3 stars'),
                           (4, 'Bib Gourmand'),
                           (5, 'Selected Restaurants')
                    ON CONFLICT (id) DO NOTHING;
                    """);

            stmt.execute("""
                    INSERT INTO cuisine_type (id, description)
                    VALUES (1, 'afghan'),
                           (2, 'african'),
                           (3, 'alpine'),
                           (4, 'alsatian'),
                           (5, 'american'),
                           (6, 'american_contemporary'),
                           (7, 'andalusian'),
                           (8, 'apulian'),
                           (9, 'argentinian'),
                           (10, 'armenian'),
                           (11, 'asian'),
                           (12, 'asian_influences'),
                           (13, 'asturian'),
                           (14, 'austrian'),
                           (15, 'bakery'),
                           (16, 'balinese'),
                           (17, 'balkan'),
                           (18, 'barbecue'),
                           (19, 'basque'),
                           (20, 'bavarian'),
                           (21, 'beef'),
                           (22, 'beijing'),
                           (23, 'belgian'),
                           (24, 'brazilian'),
                           (25, 'breton'),
                           (26, 'bulgogi'),
                           (27, 'burgundian'),
                           (28, 'burmese'),
                           (29, 'cajun'),
                           (30, 'calabrian'),
                           (31, 'californian'),
                           (32, 'cambodian'),
                           (33, 'campanian'),
                           (34, 'cantonese'),
                           (35, 'cantonese_roast_meats'),
                           (36, 'caribbean'),
                           (37, 'castilian'),
                           (38, 'catalan'),
                           (39, 'central_asian'),
                           (40, 'chao_zhou'),
                           (41, 'cheese'),
                           (42, 'chicken_specialities'),
                           (43, 'chinese'),
                           (44, 'chiu_chow'),
                           (45, 'chueotang'),
                           (46, 'classic'),
                           (47, 'colombian'),
                           (48, 'congee'),
                           (49, 'contemporary'),
                           (50, 'corsican'),
                           (51, 'country'),
                           (52, 'crab_specialities'),
                           (53, 'creative'),
                           (54, 'creole'),
                           (55, 'croatian'),
                           (56, 'cuban'),
                           (57, 'curry'),
                           (58, 'czech'),
                           (59, 'danish'),
                           (60, 'deli'),
                           (61, 'dim_sum'),
                           (62, 'doganitang'),
                           (63, 'dongbei'),
                           (64, 'dubu'),
                           (65, 'duck_specialities'),
                           (66, 'dumplings'),
                           (67, 'dwaeji_gukbap'),
                           (68, 'eastern_european'),
                           (69, 'egyptian'),
                           (70, 'emilian'),
                           (71, 'emirati'),
                           (72, 'english'),
                           (73, 'ethiopian'),
                           (74, 'european'),
                           (75, 'farm_to_table'),
                           (76, 'filipino'),
                           (77, 'finnish'),
                           (78, 'flemish'),
                           (79, 'fondue'),
                           (80, 'french'),
                           (81, 'freshwater_eel'),
                           (82, 'friulian'),
                           (83, 'fugu'),
                           (84, 'fujian'),
                           (85, 'fusion'),
                           (86, 'galician'),
                           (87, 'gastropub'),
                           (88, 'gejang'),
                           (89, 'german'),
                           (90, 'gomtang'),
                           (91, 'greek'),
                           (92, 'grills'),
                           (93, 'hainanese'),
                           (94, 'hakkanenese'),
                           (95, 'hang_zhou'),
                           (96, 'home_cooking'),
                           (97, 'hotpot'),
                           (98, 'huaiyang'),
                           (99, 'hubei'),
                           (100, 'hui'),
                           (101, 'hunanese'),
                           (102, 'hungarian'),
                           (103, 'indian'),
                           (104, 'indian_vegetarian'),
                           (105, 'indonesian'),
                           (106, 'innovative'),
                           (107, 'international'),
                           (108, 'irish'),
                           (109, 'isan'),
                           (110, 'israeli'),
                           (111, 'italian'),
                           (112, 'italian_american'),
                           (113, 'italian_contemporary'),
                           (114, 'izakaya'),
                           (115, 'jamaican'),
                           (116, 'japan'),
                           (117, 'japanese'),
                           (118, 'japanese_contemporary'),
                           (119, 'japanese_steakhouse'),
                           (120, 'jiangzhe'),
                           (121, 'jokbal'),
                           (122, 'kalguksu'),
                           (123, 'korean'),
                           (124, 'korean_contemporary'),
                           (125, 'kushiage'),
                           (126, 'kyoto'),
                           (127, 'lamb_specialities'),
                           (128, 'lao'),
                           (129, 'latin_american'),
                           (130, 'lebanese'),
                           (131, 'ligurian'),
                           (132, 'lombardian'),
                           (133, 'lyonnaise'),
                           (134, 'macanese'),
                           (135, 'malaysian'),
                           (136, 'mandu'),
                           (137, 'mantuan'),
                           (138, 'mediterranean'),
                           (139, 'memil_guksu'),
                           (140, 'mexican'),
                           (141, 'middle_eastern'),
                           (142, 'milanese'),
                           (143, 'modern'),
                           (144, 'british'),
                           (145, 'moroccan'),
                           (146, 'naengmyeon'),
                           (147, 'nakagyo_ku'),
                           (148, 'nepali'),
                           (149, 'ningbo'),
                           (150, 'noodles'),
                           (151, 'north_african'),
                           (152, 'north_american'),
                           (153, 'northern_thai'),
                           (154, 'norwegian'),
                           (155, 'obanzai'),
                           (156, 'oden'),
                           (157, 'okonomiyaki'),
                           (158, 'onigiri'),
                           (159, 'organic'),
                           (160, 'oyster_specialities'),
                           (161, 'pakistani'),
                           (162, 'peranakan'),
                           (163, 'persian'),
                           (164, 'peruvian'),
                           (165, 'piedmontese'),
                           (166, 'pizza'),
                           (167, 'polish'),
                           (168, 'pork'),
                           (169, 'portuguese'),
                           (170, 'provencal'),
                           (171, 'puerto_rican'),
                           (172, 'pufferfish'),
                           (173, 'raclette'),
                           (174, 'ramen'),
                           (175, 'regional'),
                           (176, 'regional_european'),
                           (177, 'rice_dishes'),
                           (178, 'roman'),
                           (179, 'russian'),
                           (180, 'sardinian'),
                           (181, 'savoyard'),
                           (182, 'scandinavian'),
                           (183, 'scottish'),
                           (184, 'seafood'),
                           (185, 'seasonal'),
                           (186, 'seolleongtang'),
                           (187, 'shaanxi'),
                           (188, 'shabu_shabu'),
                           (189, 'shandong'),
                           (190, 'shanghainese'),
                           (191, 'sharing'),
                           (192, 'shellfish_specialities'),
                           (193, 'shojin'),
                           (194, 'shun_tak'),
                           (195, 'sichuan'),
                           (196, 'sicilian'),
                           (197, 'singaporean'),
                           (198, 'small_eats'),
                           (199, 'smorrebrod'),
                           (200, 'soba'),
                           (201, 'south_african'),
                           (202, 'south_american'),
                           (203, 'south_east_asian'),
                           (204, 'southern'),
                           (205, 'southern_thai'),
                           (206, 'south_indian'),
                           (207, 'south_tyrolean'),
                           (208, 'spanish'),
                           (209, 'spanish_contemporary'),
                           (210, 'sri_lankan'),
                           (211, 'steakhouse'),
                           (212, 'street'),
                           (213, 'sujebi'),
                           (214, 'sukiyaki'),
                           (215, 'sushi'),
                           (216, 'swabian'),
                           (217, 'swedish'),
                           (218, 'swiss'),
                           (219, 'taiwanese'),
                           (220, 'taiwanese_contemporary'),
                           (221, 'taizhou'),
                           (222, 'tempura'),
                           (223, 'teochew'),
                           (224, 'teppanyaki'),
                           (225, 'tex_mex'),
                           (226, 'thai'),
                           (227, 'tibetan'),
                           (228, 'tonkatsu'),
                           (229, 'traditional'),
                           (230, 'turkish'),
                           (231, 'tuscan'),
                           (232, 'udon'),
                           (233, 'umbrian'),
                           (234, 'unagi'),
                           (235, 'vegan'),
                           (236, 'vegetarian'),
                           (237, 'venetian'),
                           (238, 'venezuelan'),
                           (239, 'vietnamese'),
                           (240, 'western'),
                           (241, 'world'),
                           (242, 'xibei'),
                           (243, 'xinjiang'),
                           (244, 'yakitori'),
                           (245, 'yoshoku'),
                           (246, 'yukhoe'),
                           (247, 'yunnanese'),
                           (248, 'zhou'),
                           (249, 'zhejiang')
                    ON CONFLICT (id) DO NOTHING;
                    """);

            stmt.execute("""
                    INSERT INTO services_and_facilities (id, description)
                    VALUES (1, 'WiFi'),
                           (2, 'Parking'),
                           (3, 'Outdoor seating'),
                           (4, 'Wheelchair accessible'),
                           (5, 'Delivery'),
                           (6, 'Takeaway'),
                           (7, 'Reservations'),
                           (8, 'Pet friendly'),
                           (9, 'Air conditioning'),
                           (10, 'Breakfast')
                    ON CONFLICT (id) DO NOTHING;
                    """);

            String systemSalt = PasswordHasher.generateSalt();
            String systemHash = PasswordHasher.hash(UUID.randomUUID().toString(), systemSalt);
            byte[] systemIdBytes = "theknife-system-owner".getBytes();

            try (PreparedStatement systemStmt = conn.prepareStatement(
                    "INSERT INTO \"user\" (id, username, psw_hash, psw_salt, first_name, last_name, birth_date, is_owner, is_system) "
                            + "VALUES (?, 'system', ?, ?, 'System', 'Michelin', '2000-01-01', true, true) "
                            + "ON CONFLICT (id) DO NOTHING")) {
                systemStmt.setObject(1, java.util.UUID.nameUUIDFromBytes(systemIdBytes), Types.OTHER);
                systemStmt.setString(2, systemHash);
                systemStmt.setString(3, systemSalt);
                systemStmt.execute();
            }

            return true;
        } catch (SQLException ex) {
            return false;
        }
    }

    /**
     * Restituisce una connessione dal connection pool.
     * Metodo bloccante (4 filosofi, tipo 'waiter')
     *
     * @return connessione attiva
     * @throws SQLException se la connessione non è disponibile, altrimenti {@link Thread#wait}
     */
    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    /**
     * Chiude il connection pool.
     * Da chiamare alla chiusura dell'applicazione.
     */
    public static void shutdown() {
        if (ds != null && !ds.isClosed()) {
            ds.close();
        }
    }
}