package org.newsrec.network;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;

public class LoginClient {

    private static final Logger logger = LogManager.getLogger(LoginClient.class);

    private int sendAction(
            String action,
            String username,
            String password
    ) {

        try (

                Socket socket =
                        new Socket(
                                "localhost",
                                9999
                        );

                BufferedReader in =
                        new BufferedReader(
                                new InputStreamReader(
                                        socket.getInputStream()
                                )
                        );

                PrintWriter out =
                        new PrintWriter(
                                socket.getOutputStream(),
                                true
                        )
        ) {

            out.println(action);
            out.println(username);
            out.println(password);

            String response = in.readLine();
            if (response == null) return 0;
            return Integer.parseInt(response);

        } catch (Exception e) {

            logger.error("{} failed for user: {}", action, username, e);
        }

        return 0;
    }

    public int login(
            String username,
            String password
    ) {
        return sendAction("LOGIN", username, password);
    }

    public int register(
            String username,
            String password
    ) {
        return sendAction("REG", username, password);
    }
}