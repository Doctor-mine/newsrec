package org.newsrec.ui;

import javax.swing.*;

public class DashboardFrame extends JFrame {

    private final int userId;

    public DashboardFrame(int userId) {
        this.userId = userId;
        setTitle("News Recommendation");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JTabbedPane tabs = new JTabbedPane();
        tabs.add("Local Comparison", new Option1Panel(userId));
        tabs.add("Online Comparison", new Option2Panel(userId));
        tabs.add("Topic Search", new Option3Panel());
        add(tabs);
    }
}
