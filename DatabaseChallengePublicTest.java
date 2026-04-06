import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;

public class DatabaseChallengePublicTest {

    // Game rules
    static final int BASE_SCORE = 100;
    static final int HINT_PENALTY = 25;
    static final int MAX_ATTEMPTS = 3;

    // Harder difficulty for higher winning streaks
    public static String getDifficultyFromStreak(int streak) {
        if (streak >= 6) return "hard";
        else if (streak >= 3) return "medium";
        else return "easy";
    }

    // Get existing user stats or create a new profile if they don't exist
    public static int[] getOrCreateUser(Connection conn, String username) throws Exception {
        String selectSql = "SELECT id, streak, total_score FROM users WHERE username = ?";
        PreparedStatement selectStmt = conn.prepareStatement(selectSql);
        selectStmt.setString(1, username);
        ResultSet rs = selectStmt.executeQuery();

        // User found, return stats
        if (rs.next()) {
            int id = rs.getInt("id");
            int streak = rs.getInt("streak");
            int totalScore = rs.getInt("total_score");
            rs.close(); selectStmt.close();
            return new int[]{id, streak, totalScore};
        }

        rs.close(); selectStmt.close();

        // User not found, create new profile
        String insertSql = "INSERT INTO users (username, streak, total_score) VALUES (?, 0, 0)";
        PreparedStatement insertStmt = conn.prepareStatement(insertSql, PreparedStatement.RETURN_GENERATED_KEYS);
        insertStmt.setString(1, username);
        insertStmt.executeUpdate();
        
        ResultSet keys = insertStmt.getGeneratedKeys();
        keys.next();
        int newId = keys.getInt(1);
        keys.close(); insertStmt.close();

        return new int[]{newId, 0, 0};
    }

    // Update streak and total score after a game
    public static void updateUserStreak(Connection conn, String username, int newStreak, int addedScore) throws Exception {
        String sql = "UPDATE users SET streak = ?, total_score = total_score + ? WHERE username = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, newStreak);
        pstmt.setInt(2, addedScore);
        pstmt.setString(3, username);
        pstmt.executeUpdate();
        pstmt.close();
    }

    // Save win/loss record to history
    public static void logChallengeHistory(Connection conn, int userId, int challengeId, int hintsUsed, int scoreEarned, boolean solved) throws Exception {
        String sql = "INSERT INTO user_challenge_history (user_id, challenge_id, hints_used, score_earned, solved) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, userId);
        pstmt.setInt(2, challengeId);
        pstmt.setInt(3, hintsUsed);
        pstmt.setInt(4, scoreEarned);
        pstmt.setBoolean(5, solved); 
        pstmt.executeUpdate();
        pstmt.close();
    }

    // Deduct points for hints used
    public static int calculateScore(int hintsUsed) {
        return Math.max(0, BASE_SCORE - (hintsUsed * HINT_PENALTY));
    }

    // Let user pick difficulty or auto-select based on streak
    public static String pickDifficulty(Scanner scanner, int streak) {
        String auto = getDifficultyFromStreak(streak);

        System.out.println("\nChoose your difficulty:");
        System.out.println("[1] Easy  [2] Medium  [3] Hard");

        if (streak > 0) {
            System.out.println("[4] Use streak-based (" + auto + ")");
        }

        System.out.print("Choice: ");
        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1": return "easy";
            case "2": return "medium";
            case "3": return "hard";
            case "4":
                if (streak > 0) return auto;
            default:
                System.out.println("Invalid choice, defaulting to Easy.");
                return "easy";
        }
    }

    public static void main(String[] args) {
        // Connect to local SQLite file
        String url = "jdbc:sqlite:cipher.db";

        Scanner scanner = new Scanner(System.in);

        try {
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection(url);
            
            // Turn on foreign keys for SQLite
            conn.createStatement().execute("PRAGMA foreign_keys = ON;");
            
            System.out.println("Connected to database.\n");

            System.out.print("Enter username: ");
            String username = scanner.nextLine().trim();

            boolean playAgain = true;
            int lastPlayedId = -1;

            // Main loop
            while (playAgain) {
                int[] userData = getOrCreateUser(conn, username);
                int userId     = userData[0];
                int streak     = userData[1];
                int totalScore = userData[2];

                System.out.println("\n--------------------------------------------------");
                System.out.println("Username:    " + username);
                System.out.println("Streak:      " + streak);
                System.out.println("Total Score: " + totalScore);
                System.out.println("--------------------------------------------------");

                String selectedDifficulty = pickDifficulty(scanner, streak);
                System.out.println("Playing on: " + selectedDifficulty.toUpperCase() + "\n");

                String exclusion = lastPlayedId == -1 ? "" : " AND id != " + lastPlayedId;

                // Get 1 random challenge
                String query = "SELECT id, title, ciphertext, hint, hint2, difficulty, omitted_letter, answer_key " +
                               "FROM challenges " +
                               "WHERE difficulty = ? AND is_active = 1" + exclusion +
                               " ORDER BY RANDOM() LIMIT 1";

                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setString(1, selectedDifficulty);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    int challengeId      = rs.getInt("id");
                    String title         = rs.getString("title");
                    String ciphertext    = rs.getString("ciphertext");
                    String hint1         = rs.getString("hint");
                    String hint2         = rs.getString("hint2");
                    String difficulty    = rs.getString("difficulty");
                    String omittedLetter = rs.getString("omitted_letter");
                    String answerKey     = rs.getString("answer_key");

                    lastPlayedId = challengeId;

                    System.out.println("Challenge: " + title);
                    System.out.println("--------------------------------------------------");
                    System.out.println("Ciphertext:     " + ciphertext);
                    System.out.println("Difficulty:     " + difficulty);
                    System.out.println("Omitted Letter: " + omittedLetter);
                    System.out.println("Base score:     " + BASE_SCORE + " pts  (each hint -" + HINT_PENALTY + " pts)");
                    System.out.println("Attempts:       " + MAX_ATTEMPTS);
                    System.out.println("--------------------------------------------------\n");

                    int hintsUsed    = 0;
                    int attemptsLeft = MAX_ATTEMPTS;
                    boolean answered = false;

                    // Gameplay loop
                    while (!answered) {
                        System.out.println("Attempts remaining: " + attemptsLeft);
                        System.out.println("[A] Answer  [H] Use hint  [Q] Quit");
                        System.out.print("Choice: ");
                        String choice = scanner.nextLine().trim();
                        String choiceUpper = choice.toUpperCase();

                        if (choiceUpper.equals("H")) {
                            if (hintsUsed == 0 && hint1 != null) {
                                System.out.println("Hint 1: " + hint1);
                                hintsUsed++;
                            } else if (hintsUsed == 1 && hint2 != null) {
                                System.out.println("Hint 2: " + hint2);
                                hintsUsed++;
                            } else {
                                System.out.println("No more hints available.");
                            }
                            System.out.println("Score if you answer now: " + calculateScore(hintsUsed) + " pts\n");

                        } else if (choiceUpper.equals("A")) {
                            System.out.print("Enter your answer: ");
                            String userAnswer    = scanner.nextLine().trim();
                            
                            
                            String cleanedAnswer = userAnswer.replaceAll("\\s+", "").toUpperCase();
                            String cleanedKey    = answerKey.replaceAll("\\s+", "").toUpperCase();

                            // Win
                            if (cleanedAnswer.equals(cleanedKey)) {
                                int scoreEarned = calculateScore(hintsUsed);
                                streak++;
                                answered = true;
                                System.out.println("\n✓ Correct!");
                                System.out.println("Score earned:   " + scoreEarned + " pts");
                                System.out.println("Updated streak: " + streak);
                                
                                updateUserStreak(conn, username, streak, scoreEarned);
                                logChallengeHistory(conn, userId, challengeId, hintsUsed, scoreEarned, true);

                            // Wrong guess
                            } else {
                                attemptsLeft--;
                                if (attemptsLeft > 0) {
                                    System.out.println("\n✗ Wrong answer! " + attemptsLeft + " attempt(s) remaining.\n");
                                } else {
                                    // Loss
                                    System.out.println("\n✗ Wrong answer!");
                                    System.out.println("--------------------------------------------------");
                                    System.out.println("No attempts remaining. The correct answer was: " + answerKey);
                                    System.out.println("--------------------------------------------------");
                                    streak = 0; 
                                    answered = true;
                                    
                                    updateUserStreak(conn, username, streak, 0);
                                    logChallengeHistory(conn, userId, challengeId, hintsUsed, 0, false);
                                }
                            }

                        // Give up
                        } else if (choiceUpper.equals("Q")) {
                            System.out.println("\nChallenge quit. No score recorded.");
                            System.out.println("The correct answer was: " + answerKey);
                            logChallengeHistory(conn, userId, challengeId, hintsUsed, 0, false);
                            answered = true;

                        } else {
                            System.out.println("Invalid choice, try again.\n");
                        }
                    }

                } else {
                    System.out.println("No challenge found for difficulty: " + selectedDifficulty);
                }

                rs.close();
                pstmt.close();

                System.out.print("\nPlay another challenge? [Y/N]: ");
                String again = scanner.nextLine().trim().toUpperCase();
                playAgain = again.equals("Y");
            }

            System.out.println("\nThanks for playing! See you next time.");
            conn.close();
            scanner.close();

        } catch (Exception e) {
            System.out.println("Database connection failed.");
            e.printStackTrace();
        }
    }
}