package org.newsrec;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import org.newsrec.network.LoginServer;
import org.newsrec.ui.LoginFrame;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {

        try {
            new Thread(() ->
                    new LoginServer().start()
            ).start();
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() ->
                new LoginFrame().setVisible(true));
    }
}