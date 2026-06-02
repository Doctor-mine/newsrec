package org.newsrec.dao;

import org.newsrec.database.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class VectorDAO {

    public void saveVector(

            int articleId,

            String term,

            double tfidf

    ) {

        String sql =
                """
                INSERT INTO vectors
                (
                    article_id,
                    term,
                    tfidf_score
                )
                VALUES
                (
                    ?,?,?
                )
                """;

        try (

                Connection con =
                        DBConnection.getConnection();

                PreparedStatement ps =
                        con.prepareStatement(sql)

        ) {

            ps.setInt(
                    1,
                    articleId
            );

            ps.setString(
                    2,
                    term
            );

            ps.setDouble(
                    3,
                    tfidf
            );

            ps.executeUpdate();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}