import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatabaseChallengePublicTest {

    public static void main(String[] args) {

        String url = "jdbc:mysql://localhost:3306/playfair_db";
        String user = "root";
        String password = ""; 

        try {
            // Load MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Connect to database
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to database.");

            Statement stmt = conn.createStatement();

        
            ResultSet rs = stmt.executeQuery(
                "SELECT id, title, ciphertext, hint, omitted_letter FROM challenges_public"
            );

            System.out.println("\nResults :");
            while (rs.next()) {
                System.out.println(
                    "ID: " + rs.getInt("id") +
                    " | Title: " + rs.getString("title") +
                    " | Ciphertext: " + rs.getString("ciphertext") +
                    " | Hint: " + rs.getString("hint") +
                    " | Omitted: " + rs.getString("omitted_letter")
                );
            }

            // Cleanup
            rs.close();
            stmt.close();
            conn.close();

        } catch (Exception e) {
            System.out.println("Database connection failed");
            e.printStackTrace();
        }
    }
}