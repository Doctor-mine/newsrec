package org.newsrec.network;

import org.newsrec.service.AuthService;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class LoginServer {

    private static final int PORT = 9999;

    public void start() {

        try (
                ServerSocket serverSocket =
                        new ServerSocket(PORT)
        ) {

            System.out.println(
                    "Login server running..."
            );

            while (true) {

                Socket socket =
                        serverSocket.accept();

                new Thread(() ->
                        handleClient(socket)
                ).start();
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private void handleClient(
            Socket socket
    ) {

        try (

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

            String username =
                    in.readLine();

            String password =
                    in.readLine();

            boolean success =
                    AuthService.authenticate(
                            username,
                            password
                    );

            out.println(success);

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}