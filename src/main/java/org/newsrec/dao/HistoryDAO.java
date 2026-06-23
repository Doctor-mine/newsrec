package org.newsrec.dao;

import org.newsrec.database.DBConnection;
import org.newsrec.model.HistoryRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class HistoryDAO {

    private static final Logger logger = LogManager.getLogger(HistoryDAO.class);

    public List<HistoryRecord> findByUserId(int userId) {
        List<HistoryRecord> records = new ArrayList<>();
        String sql = """
                SELECT user_id, base_file, compared_file, similarity
                FROM analysis_history
                WHERE user_id = ?
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                records.add(new HistoryRecord(
                        rs.getInt("user_id"),
                        rs.getString("base_file"),
                        rs.getString("compared_file"),
                        rs.getDouble("similarity")
                ));
            }

        } catch (Exception e) {
            logger.error("Failed to load history for user {}", userId, e);
        }

        return records;
    }

    public void save(int userId, String baseFile, String comparedFile, double similarity) {
        String sql = """
                INSERT INTO analysis_history
                (
                    user_id,
                    base_file,
                    compared_file,
                    similarity
                )
                VALUES
                (
                    ?,?,?,?
                )
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, baseFile);
            ps.setString(3, comparedFile);
            ps.setDouble(4, similarity);
            ps.executeUpdate();

        } catch (Exception e) {
            logger.error("Failed to save analysis history for user {}", userId, e);
        }
    }
}
