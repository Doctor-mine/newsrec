package org.newsrec;

import com.formdev.flatlaf.FlatLightLaf;
import org.newsrec.network.LoginServer;
import org.newsrec.ui.LoginFrame;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;

public class Main {

    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            new Thread(() -> new LoginServer().start()).start();
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            logger.error("Failed to initialize application", e);
        }
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
