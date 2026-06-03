package org.newsrec.ui;

import org.newsrec.model.RecommendationResult;
import org.newsrec.service.OnlineRecommendationService;
import org.newsrec.util.BrowserUtil;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.io.File;
import java.util.List;

public class Option2Panel extends JPanel {

    private JEditorPane results;

    private JButton uploadBtn;

    public Option2Panel() {

        setLayout(
                new BorderLayout()
        );

        uploadBtn =
                new JButton(
                        "Upload File"
                );
        add(
                uploadBtn,
                BorderLayout.NORTH
        );

        results =
                new JEditorPane();
        results.setContentType(
                "text/html"
        );
        results.setEditable(false);
        results.addHyperlinkListener(e -> {
            if (e.getEventType()
                    == HyperlinkEvent.EventType.ACTIVATED) {
                BrowserUtil.open(
                        e.getURL().toString()
                );
            }
        });

        add(
                new JScrollPane(results),
                BorderLayout.CENTER
        );

        uploadBtn.addActionListener(e -> {

            JFileChooser fileChooser =
                    new JFileChooser();

            int result =
                    fileChooser.showOpenDialog(this);

            if (result == JFileChooser.APPROVE_OPTION) {

                File file =
                        fileChooser.getSelectedFile();

                uploadBtn.setEnabled(false);
                results.setText(
                        "<html><body><p>Starting...</p>"
                );

                SwingWorker<List<RecommendationResult>, String> worker =
                        new SwingWorker<>() {
                            @Override
                            protected List<RecommendationResult> doInBackground() {
                                OnlineRecommendationService service =
                                        new OnlineRecommendationService();
                                List<RecommendationResult> recs =
                                        service.recommend(
                                                file,
                                                msg -> publish(msg)
                                        );
                                return recs;
                            }

                            @Override
                            protected void process(List<String> chunks) {
                                StringBuilder sb = new StringBuilder();
                                for (String s : chunks) {
                                    sb.append(
                                            s.replace("\n", "<br>")
                                    );
                                }
                                String current = results.getText();
                                int bodyEnd =
                                        current.lastIndexOf("</body>");
                                if (bodyEnd >= 0) {
                                    results.setText(
                                            current.substring(0, bodyEnd)
                                                    + sb.toString()
                                                    + "</body></html>"
                                    );
                                } else {
                                    results.setText(
                                            "<html><body>"
                                                    + sb.toString()
                                                    + "</body></html>"
                                    );
                                }
                            }

                            @Override
                            protected void done() {
                                try {
                                    List<RecommendationResult> recommendations = get();
                                    StringBuilder html =
                                            new StringBuilder();
                                    html.append(
                                            "<html><body>"
                                    );
                                    if (recommendations.isEmpty()) {
                                        html.append(
                                                "<p>No recommendations found.</p>"
                                        );
                                    } else {
                                        html.append(
                                                "<h3>Recommendations</h3>"
                                        );
                                        for (RecommendationResult r
                                                : recommendations) {
                                            html.append(
                                                    "<p><b>"
                                                            + r.getFileName()
                                                            + "</b><br>"
                                                            + "<i>Source: "
                                                            + r.getSource()
                                                            + "</i><br>"
                                                            + "Score: "
                                                            + String.format(
                                                                    "%.4f",
                                                                    r.getSimilarity()
                                                            )
                                                            + "<br>"
                                                            + "<a href='"
                                                            + r.getLink()
                                                            + "'>"
                                                            + r.getLink()
                                                            + "</a></p>"
                                                            + "<hr>"
                                            );
                                        }
                                    }
                                    html.append("</body></html>");
                                    results.setText(
                                            html.toString()
                                    );
                                } catch (Exception ex) {
                                    results.setText(
                                            "<html><body><p>Error: "
                                                    + ex.getMessage()
                                                    + "</p></body></html>"
                                    );
                                    JOptionPane.showMessageDialog(
                                            Option2Panel.this,
                                            "Error processing file: "
                                                    + ex.getMessage(),
                                            "Error",
                                            JOptionPane.ERROR_MESSAGE
                                    );
                                } finally {
                                    uploadBtn.setEnabled(true);
                                }
                            }
                        };

                worker.execute();
            }
        });
    }
}
