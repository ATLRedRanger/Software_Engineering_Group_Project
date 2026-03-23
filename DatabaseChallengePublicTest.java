import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;

public class DatabaseChallengePublicTest {

    public static String getDifficultyFromStreak(int streak) {
        if (streak >= 6) {
            return "hard";
        } else if (streak >= 3) {
            return "medium";
        } else {
            return "easy";
        }
    }

    public static int getOrCreateUser(Connection conn, String username) throws Exception {
        String selectSql = "SELECT streak FROM users WHERE username = ?";
        PreparedStatement selectStmt = conn.prepareStatement(selectSql);
        selectStmt.setString(1, username);

        ResultSet rs = selectStmt.executeQuery();

        if (rs.next()) {
            int streak = rs.getInt("streak");
            rs.close();
            selectStmt.close();
            return streak;
        }

        rs.close();
        selectStmt.close();

        String insertSql = "INSERT INTO users (username, streak) VALUES (?, 0)";
        PreparedStatement insertStmt = conn.prepareStatement(insertSql);
        insertStmt.setString(1, username);
        insertStmt.executeUpdate();
        insertStmt.close();

        return 0;
    }

    public static void updateUserStreak(Connection conn, String username, int newStreak) throws Exception {
        String sql = "UPDATE users SET streak = ? WHERE username = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, newStreak);
        pstmt.setString(2, username);
        pstmt.executeUpdate();
        pstmt.close();
    }

    public static void main(String[] args) {

        String url = "jdbc:mysql://localhost:3306/playfair_db";
        String user = "root";
        String password = "";

        Scanner scanner = new Scanner(System.in);

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to database.\n");

            System.out.print("Enter username: ");
            String username = scanner.nextLine().trim();

            int streak = getOrCreateUser(conn, username);
            String selectedDifficulty = getDifficultyFromStreak(streak);

            System.out.println("\nUsername: " + username);
            System.out.println("Current streak: " + streak);
            System.out.println("Selected difficulty: " + selectedDifficulty);
            System.out.println();

            String query = """
                SELECT id, title, ciphertext, hint, hint2, difficulty, omitted_letter, answer_key
                FROM challenges
                WHERE difficulty = ?
                  AND is_active = 1
                ORDER BY RAND()
                LIMIT 1
                """;

            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, selectedDifficulty);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int challengeId = rs.getInt("id");
                String title = rs.getString("title");
                String ciphertext = rs.getString("ciphertext");
                String hint1 = rs.getString("hint");
                String hint2 = rs.getString("hint2");
                String difficulty = rs.getString("difficulty");
                String omittedLetter = rs.getString("omitted_letter");
                String answerKey = rs.getString("answer_key");

                System.out.println("Random Challenge Result:");
                System.out.println("--------------------------------------------------");
                System.out.println("ID: " + challengeId);
                System.out.println("Title: " + title);
                System.out.println("Ciphertext: " + ciphertext);
                System.out.println("Hint 1: " + hint1);
                System.out.println("Hint 2: " + hint2);
                System.out.println("Difficulty: " + difficulty);
                System.out.println("Omitted Letter: " + omittedLetter);
                System.out.println("--------------------------------------------------");

                System.out.print("Enter your answer: ");
                String userAnswer = scanner.nextLine().trim();

                String cleanedUserAnswer = userAnswer.replaceAll("\\s+", "").toUpperCase();
                String cleanedAnswerKey = answerKey.replaceAll("\\s+", "").toUpperCase();

                if (cleanedUserAnswer.equals(cleanedAnswerKey)) {
                    System.out.println("Correct answer!");
                    streak = streak + 1;
                } else {
                    System.out.println("Wrong answer.");
                    System.out.println("Correct answer was: " + answerKey);
                    streak = 0;
                }

                updateUserStreak(conn, username, streak);
                System.out.println("Updated streak: " + streak);

            } else {
                System.out.println("No challenge found for difficulty: " + selectedDifficulty);
            }

            rs.close();
            pstmt.close();
            conn.close();
            scanner.close();

        } catch (Exception e) {
            System.out.println("Database connection failed.");
            e.printStackTrace();
        }
    }
}