package org.newsrec.ui;

import org.newsrec.dao.HistoryDAO;
import org.newsrec.model.HistoryRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class HistoryPanel extends JPanel {

    private static final Logger logger = LogManager.getLogger(HistoryPanel.class);

    private final int userId;
    private final HistoryDAO historyDAO = new HistoryDAO();
    private JTable table;
    private DefaultTableModel tableModel;

    public HistoryPanel(int userId) {
        this.userId = userId;
        setLayout(new BorderLayout());

        JLabel title = new JLabel("Past Analysis History");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        title.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(title, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"NO.", "Base File", "Compared File", "Similarity"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        table.setRowHeight(24);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton refreshBtn = new JButton("Refresh");
        JPanel bottom = new JPanel();
        bottom.add(refreshBtn);
        add(bottom, BorderLayout.SOUTH);

        refreshBtn.addActionListener(e -> loadHistory());
        loadHistory();
    }

    private void loadHistory() {
        tableModel.setRowCount(0);
        List<HistoryRecord> records = historyDAO.findByUserId(userId);

        for (int i = 0; i < records.size(); i++) {
            HistoryRecord r = records.get(i);
            tableModel.addRow(new Object[]{
                    i + 1,
                    r.getBaseFile(),
                    r.getComparedFile(),
                    String.format("%.4f", r.getSimilarity())
            });
        }

        if (records.isEmpty()) {
            tableModel.addRow(new Object[]{"—", "No history found", "", ""});
        }
    }
}
