package org.newsrec.dao;

import org.newsrec.database.DBConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class HistoryDAO {

    private static final Logger logger = LogManager.getLogger(HistoryDAO.class);

    public void save(
            int userId,
            String baseFile,
            String comparedFile,
            double similarity
    ) {

        String sql =
                """
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

        try (

                Connection con =
                        DBConnection
                                .getConnection();

                PreparedStatement ps =
                        con.prepareStatement(sql)

        ) {

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
