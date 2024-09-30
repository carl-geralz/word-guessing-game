import java.io.File;
import java.net.URISyntaxException;
import java.sql.*;
import java.util.Scanner;

public class WordGuessingGame {
    private static String DB_URL;

    static {
        try {
            String classPath = new File(WordGuessingGame.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
            String dbName = "asah_otak.db";
            String dbPath = new File(new File(classPath).getParentFile(), dbName).getAbsolutePath();
            DB_URL = "jdbc:sqlite:" + dbPath;
        } catch (URISyntaxException e) {
            System.err.println("Error setting up database path: " + e.getMessage());
            DB_URL = "jdbc:sqlite:asah_otak.db";
        }
    }

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            System.out.println("Database created at: " + DB_URL);
            setupDatabase(conn);
            insertSampleData(conn);
            playGame(conn);
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    private static void setupDatabase(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS master_kata " +
                         "(id INTEGER PRIMARY KEY AUTOINCREMENT, kata TEXT, clue TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS point_game " +
                         "(id_point INTEGER PRIMARY KEY AUTOINCREMENT, nama_user TEXT, total_point INTEGER)");
                         
            System.out.println("Tables created successfully.");
        }
    }

    private static void insertSampleData(Connection conn) throws SQLException {
        String sql = "INSERT INTO master_kata (kata, clue) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Sample data
            String[][] wordsAndClues = {
                {"LEMARI", "Aku tempat menyimpan pakaian?"},
                {"KOMPUTER", "Alat elektronik untuk mengolah data"},
                {"JENDELA", "Pembuka ruangan untuk cahaya dan udara"},
                {"SEPEDA", "Kendaraan roda dua tanpa mesin"},
                {"TELEPON", "Alat komunikasi jarak jauh"},
                {"KAMERA", "Alat untuk mengambil gambar"},
                {"PENSIL", "Alat tulis dengan isi grafit"},
                {"PAYUNG", "Pelindung dari hujan atau panas"},
                {"GITAR", "Alat musik petik dengan enam senar"},
                {"KUNCI", "Alat untuk membuka atau mengunci pintu"}
            };

            for (String[] wordAndClue : wordsAndClues) {
                pstmt.setString(1, wordAndClue[0]);
                pstmt.setString(2, wordAndClue[1]);
                pstmt.executeUpdate();
            }

            System.out.println("Sample data inserted successfully.");
        }
    }

    private static String[] getRandomWord(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT kata, clue FROM master_kata ORDER BY RANDOM() LIMIT 1")) {
            if (rs.next()) {
                return new String[]{rs.getString("kata"), rs.getString("clue")};
            }
        }
        return new String[]{"", ""};
    }

    private static String displayWordWithClues(String word) {
        StringBuilder display = new StringBuilder();
        for (int i = 0; i < word.length(); i++) {
            if (i == 2 || i == 6) {
                display.append(word.charAt(i));
            } else {
                display.append("_");
            }
            display.append(" ");
        }
        return display.toString().trim();
    }

    private static void playGame(Connection conn) throws SQLException {
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                String[] wordAndClue = getRandomWord(conn);
                String word = wordAndClue[0].toUpperCase();
                String clue = wordAndClue[1];

                System.out.println("Clue: " + clue);
                System.out.println("Word: " + displayWordWithClues(word));

                System.out.print("Enter your guess: ");
                String guess = scanner.nextLine().toUpperCase();

                int score = 0;
                for (int i = 0; i < word.length(); i++) {
                    if (i < guess.length() && guess.charAt(i) == word.charAt(i)) {
                        score += 10;
                    } else {
                        score -= 2;
                    }
                }

                System.out.println("Your score: " + score);

                System.out.print("Do you want to save your score? (Y/N): ");
                if (scanner.nextLine().toUpperCase().equals("Y")) {
                    System.out.print("Enter your name: ");
                    String name = scanner.nextLine();
                    saveScore(conn, name, score);
                }

                System.out.print("Do you want to play again? (Y/N): ");
                if (!scanner.nextLine().toUpperCase().equals("Y")) {
                    break;
                }
            }
        }
    }

    private static void saveScore(Connection conn, String name, int score) throws SQLException {
        String sql = "INSERT INTO point_game (nama_user, total_point) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setInt(2, score);
            pstmt.executeUpdate();
        }
    }

    public static String getDB_URL() {
        return DB_URL;
    }

    public static void setDB_URL(String DB_URL) {
        WordGuessingGame.DB_URL = DB_URL;
    }
}