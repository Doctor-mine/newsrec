package org.newsrec.ui;

import org.newsrec.crawler.RSSArticle;
import org.newsrec.service.TopicSearchService;
import org.newsrec.util.BrowserUtil;
import org.newsrec.util.HtmlUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.util.List;

public class Option3Panel extends JPanel {

    private static final Logger logger = LogManager.getLogger(Option3Panel.class);

    private JTextField topicField;
    private JEditorPane resultPane;
    private JButton searchBtn;
    private JProgressBar progressBar;

    public Option3Panel() {
        setLayout(new BorderLayout());

        JPanel top = new JPanel();
        topicField = new JTextField(30);
        searchBtn = new JButton("Search");
        top.add(topicField);
        top.add(searchBtn);
        add(top, BorderLayout.NORTH);

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        add(progressBar, BorderLayout.SOUTH);

        resultPane = new JEditorPane();
        resultPane.setContentType("text/html");
        resultPane.setEditable(false);
        resultPane.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                BrowserUtil.open(e.getURL().toString());
            }
        });
        add(new JScrollPane(resultPane), BorderLayout.CENTER);

        searchBtn.addActionListener(e -> search());
        topicField.addActionListener(e -> search());
    }

    private void search() {
        String topic = topicField.getText().trim();

        if (topic.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a search term.");
            return;
        }

        searchBtn.setEnabled(false);
        progressBar.setVisible(true);
        resultPane.setText("<html><body style='font-family:sans-serif; padding:8px;'><p><b>Searching for: " + HtmlUtils.escape(topic) + "</b></p>");

        SwingWorker<List<RSSArticle>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<RSSArticle> doInBackground() {
                return new TopicSearchService().search(topic);
            }

            @Override
            protected void done() {
                try {
                    List<RSSArticle> articles = get();
                    StringBuilder html = new StringBuilder();
                    html.append("<html><body style='font-family:sans-serif; padding:8px;'>");

                    if (articles.isEmpty()) {
                        html.append("<p style='color:#888;'>No articles found.</p>");
                    } else {
                        html.append("<p><i>Results are sorted by crawl order (keyword substring match, no similarity score).</i></p>");
                        for (int i = 0; i < articles.size(); i++) {
                            RSSArticle article = articles.get(i);
                            String srcBg = sourceBadgeColor(article.getSource());
                            html.append("<div style='border:1px solid #ddd; border-radius:6px; padding:10px; margin:8px 0; background:#fff;'>"
                                    + "<div style='display:flex; align-items:center; gap:10px;'>"
                                    + "<span style='font-weight:bold; color:#666;'>" + (i + 1) + ".</span>"
                                    + "<span style='font-weight:bold; font-size:14px;'>" + HtmlUtils.escape(article.getTitle()) + "</span>"
                                    + "<span style='background:" + srcBg + "; color:white; padding:2px 10px; border-radius:10px; font-size:11px;'>" + HtmlUtils.escape(article.getSource()) + "</span>"
                                    + "</div>"
                                    + "<div style='margin-top:6px;'><a href='" + HtmlUtils.escape(article.getLink()) + "' style='font-size:12px; color:#0366d6;'>" + HtmlUtils.escape(article.getLink()) + "</a></div>"
                                    + "</div>");
                        }
                    }
                    html.append("</body></html>");
                    resultPane.setText(html.toString());
                    resultPane.setCaretPosition(0);

                } catch (Exception ex) {
                    resultPane.setText("<html><body style='font-family:sans-serif; padding:8px;'><p style='color:#dc3545;'>Error: " + ex.getMessage() + "</p></body></html>");
                } finally {
                    searchBtn.setEnabled(true);
                    progressBar.setVisible(false);
                }
            }
        };

        worker.execute();
    }

    private String sourceBadgeColor(String source) {
        if ("Arxiv".equalsIgnoreCase(source)) return "#6f42c1";
        if ("ScienceDaily".equalsIgnoreCase(source)) return "#007bff";
        if ("Wikipedia".equalsIgnoreCase(source)) return "#28a745";
        if ("SemanticScholar".equalsIgnoreCase(source)) return "#fd7e14";
        return "#6c757d";
    }
}
