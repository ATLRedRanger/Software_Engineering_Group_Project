package com.playfair.backend;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ChallengeRepository {

    public ChallengeRecord getRandomChallengeByDifficulty(String difficulty, int excludeChallengeId) throws Exception {
        String sql =
            "SELECT id, title, ciphertext, hint, hint2, difficulty, omitted_letter, answer_key, is_active " +
            "FROM challenges " +
            "WHERE difficulty = ? AND is_active = 1 " +
            (excludeChallengeId == -1 ? "" : "AND id != ? ") +
            "ORDER BY RANDOM() LIMIT 1";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, difficulty);
            if (excludeChallengeId != -1) stmt.setInt(2, excludeChallengeId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new ChallengeRecord(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("ciphertext"),
                        rs.getString("hint"),
                        rs.getString("hint2"),
                        rs.getString("difficulty"),
                        rs.getString("omitted_letter"),
                        rs.getString("answer_key"),
                        rs.getInt("is_active") == 1
                    );
                }
            }
        }
        return null;
    }

    // Picks a random grid key from cipher_grids since challenges no longer link to one
    public String getRandomGridKey() throws Exception {
        String sql = "SELECT grid_key FROM cipher_grids ORDER BY RANDOM() LIMIT 1";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getString("grid_key");
            }
        }
        return null;
    }
}