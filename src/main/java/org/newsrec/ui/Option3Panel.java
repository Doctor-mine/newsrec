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

        setLayout(
                new BorderLayout()
        );

        JPanel top =
                new JPanel();

        topicField =
                new JTextField(
                        30
                );

        searchBtn =
                new JButton(
                        "Search"
                );

        top.add(
                topicField
        );

        top.add(
                searchBtn
        );

        add(
                top,
                BorderLayout.NORTH
        );

        progressBar =
                new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        add(
                progressBar,
                BorderLayout.SOUTH
        );

        resultPane =
                new JEditorPane();
        resultPane.setContentType(
                "text/html"
        );
        resultPane.setEditable(false);
        resultPane.addHyperlinkListener(e -> {
            if (e.getEventType()
                    == HyperlinkEvent.EventType.ACTIVATED) {
                BrowserUtil.open(
                        e.getURL().toString()
                );
            }
        });

        add(
                new JScrollPane(
                        resultPane
                ),
                BorderLayout.CENTER
        );

        searchBtn.addActionListener(
                e -> search()
        );

        topicField.addActionListener(
                e -> search()
        );
    }

    private void search() {

        String topic =
                topicField.getText().trim();

        if (topic.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter a search term."
            );
            return;
        }

        searchBtn.setEnabled(false);
        progressBar.setVisible(true);
        resultPane.setText(
                "<html><body>"
                        + "<p><b>Searching for: "
                        + HtmlUtils.escape(topic)
                        + "</b></p>"
        );

        SwingWorker<List<RSSArticle>, Void> worker =
                new SwingWorker<>() {
                    @Override
                    protected List<RSSArticle> doInBackground() {
                        TopicSearchService service =
                                new TopicSearchService();
                        return service.search(topic);
                    }

                    @Override
                    protected void done() {
                        try {
                            List<RSSArticle> articles = get();
                            StringBuilder html =
                                    new StringBuilder();
                            html.append(
                                    "<html><body>"
                            );
                            if (articles.isEmpty()) {
                                html.append(
                                        "<p>No articles found.</p>"
                                );
                            } else {
                                for (RSSArticle article
                                        : articles) {
                                    html.append(
                                            "<p><b>"
                                                    + HtmlUtils.escape(article.getTitle())
                                                    + "</b><br>"
                                                    + "<i>Source: "
                                                    + HtmlUtils.escape(article.getSource())
                                                    + "</i><br>"
                                                    + "<a href='"
                                                    + HtmlUtils.escape(article.getLink())
                                                    + "'>"
                                                    + HtmlUtils.escape(article.getLink())
                                                    + "</a></p>"
                                                    + "<hr>"
                                    );
                                }
                            }
                            html.append("</body></html>");
                            resultPane.setText(
                                    html.toString()
                            );
                        } catch (Exception ex) {
                            resultPane.setText(
                                    "<html><body><p>Error: "
                                            + ex.getMessage()
                                            + "</p></body></html>"
                            );
                        } finally {
                            searchBtn.setEnabled(true);
                            progressBar.setVisible(false);
                        }
                    }
                };

        worker.execute();
    }
}
