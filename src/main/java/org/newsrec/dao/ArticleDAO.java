package org.newsrec.dao;

import org.newsrec.database.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class ArticleDAO {

    public void saveArticle(

            String title,
            String content,
            String source,
            String url

    ) {

        String sql =
                """
                INSERT INTO articles
                (
                    title,
                    content,
                    source,
                    url
                )
                VALUES
                (
                    ?,?,?,?
                )
                """;

        try(

                Connection con =
                        DBConnection.getConnection();

                PreparedStatement ps =
                        con.prepareStatement(sql)

        ){

            ps.setString(1,title);
            ps.setString(2,content);
            ps.setString(3,source);
            ps.setString(4,url);

            ps.executeUpdate();

        }catch(Exception e){

            e.printStackTrace();
        }
    }
}