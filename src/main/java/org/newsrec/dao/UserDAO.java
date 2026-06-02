package org.newsrec.dao;

import org.newsrec.database.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDAO {

    public boolean login(
            String username,
            String password
    ) {

        String sql =
                """
                SELECT COUNT(*)
                FROM users
                WHERE username = ?
                AND password_hash = ?
                """;

        try (

                Connection con =
                        DBConnection.getConnection();

                PreparedStatement ps =
                        con.prepareStatement(sql)

        ) {

            ps.setString(
                    1,
                    username
            );

            ps.setString(
                    2,
                    password
            );

            ResultSet rs =
                    ps.executeQuery();

            if (rs.next()) {

                return rs.getInt(1) > 0;
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return false;
    }
}