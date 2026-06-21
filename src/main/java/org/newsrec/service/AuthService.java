package org.newsrec.service;

import org.newsrec.dao.UserDAO;

public class AuthService {

    private static final UserDAO userDAO = new UserDAO();

    private AuthService() {}

    public static int authenticate(String username, String password) {
        return userDAO.login(username, password);
    }

    public static int register(String username, String password) {
        return userDAO.register(username, password);
    }
}
