package org.newsrec.network;

import java.io.*;
import java.net.Socket;

public class LoginClient {

    public boolean login(
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

            out.println(username);
            out.println(password);

            return Boolean.parseBoolean(
                    in.readLine()
            );

        } catch (Exception e) {

            e.printStackTrace();
        }

        return false;
    }
}