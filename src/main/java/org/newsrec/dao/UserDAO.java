package org.newsrec.dao;

import org.newsrec.database.DBConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class UserDAO {

    private static final Logger logger = LogManager.getLogger(UserDAO.class);

    public int login(
            String username,
            String password
    ) {

        String sql =
                """
                SELECT id
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

            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs =
                    ps.executeQuery();

            if (rs.next()) {

                return rs.getInt("id");
            }

        } catch (Exception e) {

            logger.error("Login query failed for user: {}", username, e);
        }

        return 0;
    }

    public int register(
            String username,
            String password
    ) {

        String sql =
                """
                INSERT INTO users
                (
                    username,
                    password_hash
                )
                VALUES
                (
                    ?,?
                )
                """;

        try (

                Connection con =
                        DBConnection.getConnection();

                PreparedStatement ps =
                        con.prepareStatement(
                                sql,
                                Statement.RETURN_GENERATED_KEYS
                        )

        ) {

            ps.setString(1, username);
            ps.setString(2, password);

            int affected = ps.executeUpdate();
            if (affected == 0) return 0;

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            }

        } catch (Exception e) {

            logger.error("Registration failed for user: {}", username, e);
        }

        return 0;
    }
}