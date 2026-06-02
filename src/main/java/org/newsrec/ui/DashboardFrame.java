package org.newsrec.ui;

import javax.swing.*;

public class DashboardFrame extends JFrame {

    public DashboardFrame() {

        setTitle("News Recommendation");

        setSize(1000,700);

        setLocationRelativeTo(null);

        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JTabbedPane tabs =
                new JTabbedPane();

        tabs.add(
                "Option 1",
                new Option1Panel()
        );

        tabs.add(
                "Option 2",
                new Option2Panel()
        );

        tabs.add(
                "Option 3",
                new Option3Panel()
        );

        add(tabs);
    }
}