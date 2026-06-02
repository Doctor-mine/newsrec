package org.newsrec.ui;

import org.newsrec.model.RecommendationResult;
import org.newsrec.service.OnlineRecommendationService;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;

public class Option2Panel extends JPanel {

    private JTextArea results;

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
                new JTextArea();

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
                results.setText("Starting...\n");

                SwingWorker<List<RecommendationResult>, String> worker =
                        new SwingWorker<>() {
                            @Override
                            protected List<RecommendationResult> doInBackground() {
                                publish("Reading file...\n");
                                OnlineRecommendationService service =
                                        new OnlineRecommendationService();
                                publish("Extracting keywords...\n");
                                List<RecommendationResult> recs = service.recommend(file);
                                publish("Done.\n");
                                return recs;
                            }

                            @Override
                            protected void process(List<String> chunks) {
                                StringBuilder sb = new StringBuilder();
                                for (String s : chunks) {
                                    sb.append(s);
                                }
                                results.append(sb.toString());
                            }

                            @Override
                            protected void done() {
                                try {
                                    List<RecommendationResult> recommendations = get();
                                    if (recommendations.isEmpty()) {
                                        results.append("No recommendations found.\n");
                                    } else {
                                        StringBuilder builder =
                                                new StringBuilder();
                                        builder.append("Recommendations:\n\n");
                                        int rank = 1;
                                        for (RecommendationResult r
                                                : recommendations) {
                                            builder.append(rank++)
                                                    .append(". ");
                                            builder.append(
                                                    r.getFileName()
                                            );
                                            builder.append(
                                                    " -> "
                                            );
                                            builder.append(
                                                    String.format(
                                                            "%.4f",
                                                            r.getSimilarity()
                                                    )
                                            );
                                            builder.append("\n");
                                        }
                                        results.append(
                                                builder.toString()
                                        );
                                    }
                                } catch (Exception ex) {
                                    results.append(
                                            "Error: " + ex.getMessage() + "\n"
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
