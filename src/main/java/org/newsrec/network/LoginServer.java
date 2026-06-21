package org.newsrec.network;

import org.newsrec.service.AuthService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class LoginServer {

    private static final Logger logger = LogManager.getLogger(LoginServer.class);
    private static final int PORT = 9999;

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            logger.info("Login server running on port {}", PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(() -> handleClient(socket)).start();
            }

        } catch (java.net.BindException e) {
            logger.error("Port {} is already in use – another instance may be running.", PORT);
        } catch (Exception e) {
            logger.error("Login server error", e);
        }
    }

    private void handleClient(Socket socket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            String action = in.readLine();
            String username = in.readLine();
            String password = in.readLine();

            int userId = 0;
            if ("REG".equals(action)) {
                userId = AuthService.register(username, password);
            } else {
                userId = AuthService.authenticate(username, password);
            }
            out.println(userId);

        } catch (Exception e) {
            logger.error("Error handling login client", e);
        }
    }
}
