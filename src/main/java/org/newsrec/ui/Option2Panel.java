package org.newsrec.ui;

import org.newsrec.crawler.RSSArticle;
import org.newsrec.dao.HistoryDAO;
import org.newsrec.model.RecommendationResult;
import org.newsrec.recommendation.*;
import org.newsrec.service.OnlineRecommendationService;
import org.newsrec.util.BrowserUtil;
import org.newsrec.util.CsvExporter;
import org.newsrec.util.HtmlUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.io.File;
import java.util.List;
import java.util.*;

public class Option2Panel extends JPanel {

    private static final Logger logger = LogManager.getLogger(Option2Panel.class);

    private final int userId;
    private final HistoryDAO historyDAO = new HistoryDAO();
    private JEditorPane results;
    private JButton uploadBtn;
    private JButton exportBtn;
    private JButton previewBtn;
    private JButton compareArticlesBtn;
    private JSpinner topKSpinner;
    private JProgressBar progressBar;
    private List<RecommendationResult> lastResults;
    private File currentFile;
    private List<RSSArticle> lastArticles;

    public Option2Panel(int userId) {
        this.userId = userId;
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        uploadBtn = new JButton("Upload File");
        topPanel.add(uploadBtn);

        previewBtn = new JButton("Preview File");
        previewBtn.setEnabled(false);
        topPanel.add(previewBtn);

        exportBtn = new JButton("Export CSV");
        exportBtn.setEnabled(false);
        topPanel.add(exportBtn);

        compareArticlesBtn = new JButton("Compare Articles");
        compareArticlesBtn.setEnabled(false);
        topPanel.add(compareArticlesBtn);

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
                String url = e.getURL().toString();
                if (url.startsWith("detail://")) {
                    int idx = Integer.parseInt(url.substring("detail://".length()));
                    showArticleDetail(idx);
                } else {
                    BrowserUtil.open(url);
                }
            }
        });
        add(new JScrollPane(results), BorderLayout.CENTER);

        exportBtn.addActionListener(e -> exportCsv());
        previewBtn.addActionListener(e -> previewFile());
        compareArticlesBtn.addActionListener(e -> compareArticles());
        setupDragAndDrop();

        uploadBtn.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Documents (PDF, DOCX)", "pdf", "docx"));

            int result = fileChooser.showOpenDialog(this);

            if (result == JFileChooser.APPROVE_OPTION) {
                currentFile = fileChooser.getSelectedFile();
                File file = currentFile;
                uploadBtn.setEnabled(false);
                previewBtn.setEnabled(true);
                progressBar.setVisible(true);
                results.setText("<html><body style='font-family:sans-serif; padding:8px;'><p>Starting...</p>");

                int topK = (int) topKSpinner.getValue();

                SwingWorker<List<RecommendationResult>, String> worker = new SwingWorker<>() {
                    private OnlineRecommendationService service;

                    @Override
                    protected List<RecommendationResult> doInBackground() {
                        service = new OnlineRecommendationService();
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
                            lastArticles = service != null ? service.getLastArticles() : null;
                            saveHistory(recommendations);
                            exportBtn.setEnabled(!recommendations.isEmpty());
                            compareArticlesBtn.setEnabled(lastArticles != null && lastArticles.size() >= 2);

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
                                            + "<div style='margin-top:6px; display:flex; gap:12px;'>"
                                            + "<a href='" + HtmlUtils.escape(r.getLink()) + "' style='font-size:12px; color:#0366d6;'>Open Article</a>"
                                            + "<a href='detail://" + i + "' style='font-size:12px; color:#6c757d;'>Details</a>"
                                            + "</div>"
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

    private void previewFile() {
        if (currentFile == null) return;
        try {
            String text = org.newsrec.reader.ReaderFactory.getReader(currentFile).read(currentFile);
            JTextArea area = new JTextArea(text);
            area.setEditable(false);
            JScrollPane sp = new JScrollPane(area);
            sp.setPreferredSize(new Dimension(600, 400));
            JOptionPane.showMessageDialog(this, sp, "Preview: " + currentFile.getName(), JOptionPane.PLAIN_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to read file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showArticleDetail(int index) {
        if (lastResults == null || index < 0 || index >= lastResults.size()) return;
        RecommendationResult r = lastResults.get(index);
        String desc = r.getDescription();
        if (desc == null || desc.isEmpty()) desc = "(no description available)";

        JTextArea area = new JTextArea(desc);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        JScrollPane sp = new JScrollPane(area);
        sp.setPreferredSize(new Dimension(500, 250));
        sp.setBorder(BorderFactory.createTitledBorder("Article Preview"));

        JPanel info = new JPanel(new GridLayout(0, 1, 5, 5));
        info.add(new JLabel("<html><b>Title:</b> " + HtmlUtils.escape(r.getFileName()) + "</html>"));
        info.add(new JLabel("<html><b>Source:</b> " + HtmlUtils.escape(r.getSource()) + "</html>"));
        info.add(new JLabel("<html><b>Similarity:</b> " + String.format("%.4f", r.getSimilarity()) + "</html>"));

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.add(info, BorderLayout.NORTH);
        panel.add(sp, BorderLayout.CENTER);

        JOptionPane.showMessageDialog(this, panel, "Article Details", JOptionPane.PLAIN_MESSAGE);
    }

    private void compareArticles() {
        if (lastArticles == null || lastArticles.size() < 2) {
            JOptionPane.showMessageDialog(this, "Need at least 2 articles to compare.");
            return;
        }

        progressBar.setVisible(true);
        compareArticlesBtn.setEnabled(false);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                List<String> corpus = new ArrayList<>();
                for (RSSArticle a : lastArticles) {
                    corpus.add(a.getTitle() + " " + a.getDescription());
                }

                TFIDFVectorizer vectorizer = new TFIDFVectorizer();
                List<List<String>> tokenizedCorpus = new ArrayList<>();
                for (String doc : corpus) {
                    tokenizedCorpus.add(vectorizer.preprocess(doc));
                }
                List<Map<String, Double>> vectors = new ArrayList<>();
                List<String> labels = new ArrayList<>();
                Map<String, Double> idfMap = vectorizer.precomputeIDF(tokenizedCorpus);

                for (int i = 0; i < lastArticles.size(); i++) {
                    RSSArticle a = lastArticles.get(i);
                    String text = a.getTitle() + " " + a.getDescription();
                    vectors.add(vectorizer.normalize(vectorizer.buildVector(text, idfMap)));
                    labels.add(a.getTitle());
                }

                SimilarityMatrix simMatrix = new SimilarityMatrix(vectors, labels);
                List<Map.Entry<Double, String>> pairs = new ArrayList<>();

                for (int i = 0; i < lastArticles.size(); i++) {
                    for (int j = i + 1; j < lastArticles.size(); j++) {
                        pairs.add(Map.entry(simMatrix.get(i, j), (i + 1) + ". " + labels.get(i) + "  ↔  " + (j + 1) + ". " + labels.get(j)));
                    }
                }

                pairs.sort(Map.Entry.<Double, String>comparingByKey().reversed());
                int showCount = Math.min(20, pairs.size());

                StringBuilder html = new StringBuilder();
                html.append("<html><body style='font-family:sans-serif; padding:8px;'>");
                html.append("<h3>Article-to-Article Comparison (Top " + showCount + " pairs)</h3>");
                html.append("<p><i>Shows which crawled articles are most similar to each other.</i></p>");

                for (int k = 0; k < showCount; k++) {
                    var pair = pairs.get(k);
                    int pct = (int) Math.round(pair.getKey() * 100);
                    String scoreColor = pair.getKey() >= 0.85 ? "#28a745" : pair.getKey() >= 0.50 ? "#ffc107" : "#dc3545";
                    html.append("<div style='border:1px solid #ddd; border-radius:6px; padding:8px; margin:6px 0; background:#fff;'>"
                            + "<div style='display:flex; align-items:center; gap:8px;'>"
                            + "<span style='font-weight:bold; color:#666;'>" + (k + 1) + ".</span>"
                            + "<span style='font-size:13px;'>" + HtmlUtils.escape(pair.getValue()) + "</span>"
                            + "</div>"
                            + "<div style='margin-top:6px;'>"
                            + "<div style='background:#eee; border-radius:4px; width:160px; height:16px; overflow:hidden;'>"
                            + "<div style='background:" + scoreColor + "; width:" + pct + "%; height:16px; border-radius:4px; text-align:center; color:white; font-size:10px; line-height:16px;'>" + pct + "%</div>"
                            + "</div>"
                            + "</div></div>");
                }

                html.append("</body></html>");

                String finalHtml = html.toString();
                SwingUtilities.invokeLater(() -> {
                    results.setText(finalHtml);
                    results.setCaretPosition(0);
                });

                return null;
            }

            @Override
            protected void done() {
                progressBar.setVisible(false);
                compareArticlesBtn.setEnabled(true);
            }
        };

        worker.execute();
    }

    private void setupDragAndDrop() {
        new DropTarget(this, new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent e) {
                e.acceptDrop(DnDConstants.ACTION_COPY);
                try {
                    List<File> files = (List<File>) e.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    for (File f : files) {
                        String name = f.getName().toLowerCase();
                        if (!name.endsWith(".pdf") && !name.endsWith(".docx")) continue;
                        currentFile = f;
                        previewBtn.setEnabled(true);
                        uploadBtn.getActionListeners()[0].actionPerformed(null);
                        break;
                    }
                } catch (Exception ex) {
                    logger.error("Drop failed", ex);
                }
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
