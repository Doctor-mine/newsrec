package org.newsrec.ui;

import org.newsrec.dao.HistoryDAO;
import org.newsrec.model.RecommendationResult;
import org.newsrec.service.OnlineRecommendationService;
import org.newsrec.util.BrowserUtil;
import org.newsrec.util.CsvExporter;
import org.newsrec.util.HtmlUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.io.File;
import java.util.List;

public class Option2Panel extends JPanel {

    private static final Logger logger = LogManager.getLogger(Option2Panel.class);

    private final int userId;
    private final HistoryDAO historyDAO = new HistoryDAO();
    private JEditorPane results;
    private JButton uploadBtn;
    private JButton exportBtn;
    private JSpinner topKSpinner;
    private JProgressBar progressBar;
    private List<RecommendationResult> lastResults;

    public Option2Panel(int userId) {
        this.userId = userId;
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        uploadBtn = new JButton("Upload File");
        topPanel.add(uploadBtn);

        exportBtn = new JButton("Export CSV");
        exportBtn.setEnabled(false);
        topPanel.add(exportBtn);

        topKSpinner = new JSpinner(new SpinnerNumberModel(20, 1, 999, 1));
        ((JSpinner.DefaultEditor) topKSpinner.getEditor()).getTextField().setColumns(3);
        topPanel.add(new JLabel("Top-K:"));
        topPanel.add(topKSpinner);

        add(topPanel, BorderLayout.NORTH);

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        add(progressBar, BorderLayout.SOUTH);

        results = new JEditorPane();
        results.setContentType("text/html");
        results.setEditable(false);
        results.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                BrowserUtil.open(e.getURL().toString());
            }
        });
        add(new JScrollPane(results), BorderLayout.CENTER);

        exportBtn.addActionListener(e -> exportCsv());

        uploadBtn.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Documents (PDF, DOCX)", "pdf", "docx"));

            int result = fileChooser.showOpenDialog(this);

            if (result == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                uploadBtn.setEnabled(false);
                progressBar.setVisible(true);
                results.setText("<html><body style='font-family:sans-serif; padding:8px;'><p>Starting...</p>");

                int topK = (int) topKSpinner.getValue();

                SwingWorker<List<RecommendationResult>, String> worker = new SwingWorker<>() {
                    @Override
                    protected List<RecommendationResult> doInBackground() {
                        OnlineRecommendationService service = new OnlineRecommendationService();
                        return service.recommend(file, msg -> publish(msg), topK);
                    }

                    @Override
                    protected void process(List<String> chunks) {
                        StringBuilder sb = new StringBuilder();
                        for (String s : chunks) {
                            sb.append(s.replace("\n", "<br>"));
                        }
                        String current = results.getText();
                        int bodyEnd = current.lastIndexOf("</body>");
                        if (bodyEnd >= 0) {
                            results.setText(current.substring(0, bodyEnd) + sb.toString() + "</body></html>");
                        } else {
                            results.setText("<html><body style='font-family:sans-serif; padding:8px;'>" + sb.toString() + "</body></html>");
                        }
                    }

                    @Override
                    protected void done() {
                        try {
                            List<RecommendationResult> recommendations = get();
                            lastResults = recommendations;
                            saveHistory(recommendations);
                            exportBtn.setEnabled(!recommendations.isEmpty());

                            StringBuilder html = new StringBuilder();
                            html.append("<html><body style='font-family:sans-serif; padding:8px;'>");
                            if (recommendations.isEmpty()) {
                                html.append("<p style='color:#888;'>No recommendations found.</p>");
                            } else {
                                html.append("<h3>Recommendations</h3>");
                                html.append("<p><i>Score guide: 0.85+ = very similar, 0.50–0.85 = moderately similar, below 0.50 = barely related</i></p>");
                                for (int i = 0; i < recommendations.size(); i++) {
                                    RecommendationResult r = recommendations.get(i);
                                    int pct = (int) Math.round(r.getSimilarity() * 100);
                                    String scoreColor;
                                    if (r.getSimilarity() >= 0.85) scoreColor = "#28a745";
                                    else if (r.getSimilarity() >= 0.50) scoreColor = "#ffc107";
                                    else scoreColor = "#dc3545";
                                    String srcBg = sourceBadgeColor(r.getSource());
                                    html.append("<div style='border:1px solid #ddd; border-radius:6px; padding:10px; margin:8px 0; background:#fff;'>"
                                            + "<div style='display:flex; align-items:center; gap:10px;'>"
                                            + "<span style='font-weight:bold; color:#666;'>" + (i + 1) + ".</span>"
                                            + "<span style='font-weight:bold; font-size:14px;'>" + HtmlUtils.escape(r.getFileName()) + "</span>"
                                            + "<span style='background:" + srcBg + "; color:white; padding:2px 10px; border-radius:10px; font-size:11px;'>" + HtmlUtils.escape(r.getSource()) + "</span>"
                                            + "</div>"
                                            + "<div style='margin-top:8px; display:flex; align-items:center; gap:8px;'>"
                                            + "<div style='background:#eee; border-radius:4px; width:160px; height:18px; overflow:hidden;'>"
                                            + "<div style='background:" + scoreColor + "; width:" + pct + "%; height:18px; border-radius:4px; text-align:center; color:white; font-size:11px; line-height:18px;'>" + pct + "%</div>"
                                            + "</div>"
                                            + "<span style='font-size:12px; color:#888;'>similarity</span>"
                                            + "<span style='font-size:11px; color:#aaa;'>(" + String.format("%.4f", r.getSimilarity()) + ")</span>"
                                            + "</div>"
                                            + "<div style='margin-top:6px;'><a href='" + HtmlUtils.escape(r.getLink()) + "' style='font-size:12px; color:#0366d6;'>" + HtmlUtils.escape(r.getLink()) + "</a></div>"
                                            + "</div>");
                                }
                            }
                            html.append("</body></html>");
                            results.setText(html.toString());
                            results.setCaretPosition(0);

                        } catch (Exception ex) {
                            results.setText("<html><body style='font-family:sans-serif; padding:8px;'><p style='color:#dc3545;'>Error: " + ex.getMessage() + "</p></body></html>");
                            JOptionPane.showMessageDialog(Option2Panel.this, "Error processing file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        } finally {
                            uploadBtn.setEnabled(true);
                            progressBar.setVisible(false);
                        }
                    }
                };

                worker.execute();
            }
        });
    }

    private void exportCsv() {
        if (lastResults == null || lastResults.isEmpty()) return;
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("online_results.csv"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            CsvExporter.export(lastResults, chooser.getSelectedFile());
            JOptionPane.showMessageDialog(this, "Exported to " + chooser.getSelectedFile().getName());
        }
    }

    private void saveHistory(List<RecommendationResult> results) {
        if (userId <= 0) return;
        String baseName = "Online Analysis";
        for (RecommendationResult r : results) {
            historyDAO.save(userId, baseName, r.getFileName(), r.getSimilarity());
        }
    }

    private String sourceBadgeColor(String source) {
        if ("Arxiv".equalsIgnoreCase(source)) return "#6f42c1";
        if ("ScienceDaily".equalsIgnoreCase(source)) return "#007bff";
        if ("Wikipedia".equalsIgnoreCase(source)) return "#28a745";
        if ("SemanticScholar".equalsIgnoreCase(source)) return "#fd7e14";
        return "#6c757d";
    }
}
