package org.newsrec.dao;

import org.newsrec.database.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class HistoryDAO {

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

            ps.setInt(
                    1,
                    userId
            );

            ps.setString(
                    2,
                    baseFile
            );

            ps.setString(
                    3,
                    comparedFile
            );

            ps.setDouble(
                    4,
                    similarity
            );

            ps.executeUpdate();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}