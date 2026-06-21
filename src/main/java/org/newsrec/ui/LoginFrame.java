package org.newsrec.ui;

import org.newsrec.network.LoginClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LoginFrame extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainPanel = new JPanel(cardLayout);

    private JTextField loginUsername;
    private JPasswordField loginPassword;
    private JButton loginBtn;

    private JTextField regUsername;
    private JPasswordField regPassword;
    private JPasswordField regConfirm;
    private JButton regBtn;

    public LoginFrame() {

        setTitle("News Recommendation");
        setSize(430, 360);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        mainPanel.add(buildLoginPanel(), "login");
        mainPanel.add(buildRegisterPanel(), "register");
        add(mainPanel);

        cardLayout.show(mainPanel, "login");
    }

    private JPanel wrapPasswordField(JPasswordField field) {
        JPanel row = new JPanel(new BorderLayout(4, 0));
        row.add(field, BorderLayout.CENTER);
        JToggleButton toggle = new JToggleButton("Show");
        toggle.setPreferredSize(new Dimension(60, 26));
        toggle.setFont(toggle.getFont().deriveFont(11f));
        toggle.addActionListener(e -> {
            if (toggle.isSelected()) {
                field.setEchoChar((char) 0);
                toggle.setText("Hide");
            } else {
                field.setEchoChar('\u2022');
                toggle.setText("Show");
            }
        });
        row.add(toggle, BorderLayout.EAST);
        return row;
    }

    private JPanel buildLoginPanel() {

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 20, 6, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Username"), gbc);

        gbc.gridy = 1;
        loginUsername = new JTextField(18);
        loginUsername.setFont(loginUsername.getFont().deriveFont(14f));
        panel.add(loginUsername, gbc);

        gbc.gridy = 2;
        panel.add(new JLabel("Password"), gbc);

        gbc.gridy = 3;
        loginPassword = new JPasswordField(18);
        loginPassword.setFont(loginPassword.getFont().deriveFont(14f));
        panel.add(wrapPasswordField(loginPassword), gbc);

        gbc.gridy = 4;
        gbc.insets = new Insets(14, 20, 4, 20);
        loginBtn = new JButton("Login");
        loginBtn.setFont(loginBtn.getFont().deriveFont(15f));
        loginBtn.setPreferredSize(new Dimension(200, 38));
        panel.add(loginBtn, gbc);

        gbc.gridy = 5;
        gbc.insets = new Insets(2, 20, 6, 20);
        JLabel regLink = new JLabel("<html><u><font color='blue'>No Account? Register</font></u></html>");
        regLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        panel.add(regLink, gbc);

        loginBtn.addActionListener(e -> login());
        loginPassword.addActionListener(e -> login());

        regLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                cardLayout.show(mainPanel, "register");
            }
        });

        return panel;
    }

    private JPanel buildRegisterPanel() {

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 20, 5, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Username"), gbc);

        gbc.gridy = 1;
        regUsername = new JTextField(18);
        regUsername.setFont(regUsername.getFont().deriveFont(14f));
        panel.add(regUsername, gbc);

        gbc.gridy = 2;
        panel.add(new JLabel("Password"), gbc);

        gbc.gridy = 3;
        regPassword = new JPasswordField(18);
        regPassword.setFont(regPassword.getFont().deriveFont(14f));
        panel.add(wrapPasswordField(regPassword), gbc);

        gbc.gridy = 4;
        panel.add(new JLabel("Confirm Password"), gbc);

        gbc.gridy = 5;
        regConfirm = new JPasswordField(18);
        regConfirm.setFont(regConfirm.getFont().deriveFont(14f));
        panel.add(wrapPasswordField(regConfirm), gbc);

        gbc.gridy = 6;
        gbc.insets = new Insets(10, 20, 4, 20);
        regBtn = new JButton("Register");
        regBtn.setFont(regBtn.getFont().deriveFont(15f));
        regBtn.setPreferredSize(new Dimension(200, 38));
        panel.add(regBtn, gbc);

        gbc.gridy = 7;
        gbc.insets = new Insets(2, 20, 6, 20);
        JLabel loginLink = new JLabel("<html><u><font color='blue'>Already have an account? Login</font></u></html>");
        loginLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        panel.add(loginLink, gbc);

        regBtn.addActionListener(e -> register());
        regConfirm.addActionListener(e -> register());

        loginLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                cardLayout.show(mainPanel, "login");
            }
        });

        return panel;
    }

    private void login() {

        LoginClient client = new LoginClient();
        int userId = client.login(
                loginUsername.getText(),
                String.valueOf(loginPassword.getPassword())
        );

        if (userId > 0) {
            dispose();
            new DashboardFrame(userId).setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Login Failed");
        }
    }

    private void register() {

        String username = regUsername.getText().trim();
        String password = String.valueOf(regPassword.getPassword());
        String confirm = String.valueOf(regConfirm.getPassword());

        if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.");
            return;
        }

        if (!password.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.");
            return;
        }

        LoginClient client = new LoginClient();
        int userId = client.register(username, password);

        if (userId > 0) {
            JOptionPane.showMessageDialog(this, "Registration successful! Please log in.");
            loginUsername.setText(username);
            loginPassword.setText("");
            regUsername.setText("");
            regPassword.setText("");
            regConfirm.setText("");
            cardLayout.show(mainPanel, "login");
        } else {
            JOptionPane.showMessageDialog(this, "Registration failed. Username may already exist.");
        }
    }
}
