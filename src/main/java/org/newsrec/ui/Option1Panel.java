package org.newsrec.ui;

import org.newsrec.model.RecommendationResult;
import org.newsrec.recommendation.RecommendationService;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Option1Panel extends JPanel {

    private File baseFile;

    private final List<File> compareFiles =
            new ArrayList<>();

    private JTextArea resultArea;

    private JButton baseBtn;
    private JButton compareBtn;
    private JButton analyzeBtn;

    public Option1Panel() {

        setLayout(new BorderLayout());

        JPanel top =
                new JPanel();

        baseBtn =
                new JButton(
                        "Upload Base File"
                );

        compareBtn =
                new JButton(
                        "Upload 10 Files"
                );

        analyzeBtn =
                new JButton(
                        "Analyze"
                );

        top.add(baseBtn);
        top.add(compareBtn);
        top.add(analyzeBtn);

        add(
                top,
                BorderLayout.NORTH
        );

        resultArea =
                new JTextArea();

        add(
                new JScrollPane(resultArea),
                BorderLayout.CENTER
        );

        baseBtn.addActionListener(
                e -> chooseBase()
        );

        compareBtn.addActionListener(
                e -> chooseFiles()
        );

        analyzeBtn.addActionListener(
                e -> analyze()
        );
    }

    private void chooseBase() {

        JFileChooser chooser =
                new JFileChooser();

        if(
                chooser.showOpenDialog(this)
                        ==
                        JFileChooser.APPROVE_OPTION
        ) {

            baseFile =
                    chooser.getSelectedFile();
        }
    }

    private void chooseFiles() {

        JFileChooser chooser =
                new JFileChooser();

        chooser.setMultiSelectionEnabled(
                true
        );

        if(
                chooser.showOpenDialog(this)
                        ==
                        JFileChooser.APPROVE_OPTION
        ) {

            compareFiles.clear();

            for(File f :
                    chooser.getSelectedFiles()) {

                compareFiles.add(f);
            }
        }
    }

    private void analyze() {

        if (baseFile == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a base file first."
            );
            return;
        }

        if (compareFiles.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select at least one file to compare."
            );
            return;
        }

        setButtonsEnabled(false);
        resultArea.setText("Analyzing...\n");

        SwingWorker<List<RecommendationResult>, Void> worker =
                new SwingWorker<>() {
                    @Override
                    protected List<RecommendationResult> doInBackground() {
                        RecommendationService service =
                                new RecommendationService();
                        return service.compareFiles(
                                baseFile,
                                compareFiles
                        );
                    }

                    @Override
                    protected void done() {
                        try {
                            List<RecommendationResult> results = get();
                            resultArea.setText("");
                            for (RecommendationResult r : results) {
                                resultArea.append(
                                        r.getFileName()
                                                + " -> "
                                                + String.format(
                                                "%.4f",
                                                r.getSimilarity()
                                        )
                                                + "\n"
                                );
                            }
                        } catch (Exception ex) {
                            resultArea.setText(
                                    "Error: " + ex.getMessage()
                            );
                        } finally {
                            setButtonsEnabled(true);
                        }
                    }
                };

        worker.execute();
    }

    private void setButtonsEnabled(boolean enabled) {
        baseBtn.setEnabled(enabled);
        compareBtn.setEnabled(enabled);
        analyzeBtn.setEnabled(enabled);
    }
}
