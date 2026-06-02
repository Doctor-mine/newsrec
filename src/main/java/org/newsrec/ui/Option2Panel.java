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
                results.setText("Processing...\n");

                SwingWorker<List<RecommendationResult>, Void> worker =
                        new SwingWorker<>() {
                            @Override
                            protected List<RecommendationResult> doInBackground() {
                                OnlineRecommendationService service =
                                        new OnlineRecommendationService();
                                return service.recommend(file);
                            }

                            @Override
                            protected void done() {
                                try {
                                    List<RecommendationResult> recommendations = get();
                                    StringBuilder builder =
                                            new StringBuilder();
                                    for (RecommendationResult r
                                            : recommendations) {
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
                                    results.setText(
                                            builder.toString()
                                    );
                                } catch (Exception ex) {
                                    results.setText(
                                            "Error: " + ex.getMessage()
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
