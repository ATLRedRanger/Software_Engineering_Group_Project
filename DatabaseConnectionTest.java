import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatabaseConnectionTest {

    public static void main(String[] args) {

        String url = "jdbc:mysql://localhost:3306/playfair_db";
        String user = "root";         
        String password = "Sunshine99@@";

        try {
            // Load MySQL  Driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Connect
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println(" Connected to database.");

            // Query
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT id, cipher_key, omitted_letter, grid_string FROM cipher_grids"
            );

            System.out.println("Results:");
            while (rs.next()) {
                System.out.println(
                    rs.getInt("id") + " | " +
                    rs.getString("cipher_key") + " | " +
                    rs.getString("omitted_letter") + " | " +
                    rs.getString("grid_string")
                );
            }

            // Cleanup
            rs.close();
            stmt.close();
            conn.close();

        } catch (Exception e) {
            System.out.println(" Database connection failed");
            e.printStackTrace();
        }
    }
}