package org.newsrec.ui;

import org.newsrec.network.LoginClient;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private JTextField usernameField;

    private JPasswordField passwordField;

    public LoginFrame() {

        setTitle("News Recommendation");

        setSize(400,250);

        setLocationRelativeTo(null);

        setDefaultCloseOperation(EXIT_ON_CLOSE);

        initUI();
    }

    private void initUI() {

        JPanel panel =
                new JPanel(
                        new GridLayout(3,2,10,10)
                );

        usernameField =
                new JTextField();

        passwordField =
                new JPasswordField();

        JButton loginBtn =
                new JButton("Login");

        panel.add(new JLabel("Username"));
        panel.add(usernameField);

        panel.add(new JLabel("Password"));
        panel.add(passwordField);

        panel.add(new JLabel());
        panel.add(loginBtn);

        add(panel);

        loginBtn.addActionListener(e -> login());
    }

    private void login() {

        LoginClient client =
                new LoginClient();

        boolean success =
                client.login(
                        usernameField.getText(),
                        String.valueOf(
                                passwordField.getPassword()
                        )
                );

        if(success){

            dispose();

            new DashboardFrame()
                    .setVisible(true);

        }else{

            JOptionPane.showMessageDialog(
                    this,
                    "Login Failed"
            );
        }
    }
}