package org.newsrec.ui;

import org.newsrec.model.RecommendationResult;
import org.newsrec.recommendation.RecommendationService;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Option1Panel extends JPanel {

    private File baseFile;

    private final List<File> compareFiles =
            new ArrayList<>();

    private final DefaultListModel<String> compareListModel =
            new DefaultListModel<>();

    private JLabel baseFileLabel;

    private JList<String> compareList;

    private JPanel resultsPanel;

    private JButton baseBtn;
    private JButton removeBaseBtn;
    private JButton compareBtn;
    private JButton removeCompareBtn;
    private JButton analyzeBtn;

    public Option1Panel() {

        setLayout(new BorderLayout());

        JPanel top = new JPanel();

        baseBtn =
                new JButton(
                        "Upload Base File"
                );

        removeBaseBtn =
                new JButton(
                        "Remove Base File"
                );
        removeBaseBtn.setEnabled(false);

        compareBtn =
                new JButton(
                        "Add Compare Files"
                );

        removeCompareBtn =
                new JButton(
                        "Remove Selected"
                );
        removeCompareBtn.setEnabled(false);

        analyzeBtn =
                new JButton(
                        "Analyze"
                );

        top.add(baseBtn);
        top.add(removeBaseBtn);
        top.add(compareBtn);
        top.add(removeCompareBtn);
        top.add(analyzeBtn);

        add(
                top,
                BorderLayout.NORTH
        );

        JPanel centerPanel =
                new JPanel(new BorderLayout());

        baseFileLabel =
                new JLabel("(none)");

        JPanel baseInfo =
                new JPanel(new FlowLayout(FlowLayout.LEFT));
        baseInfo.setBorder(
                new TitledBorder("Base File")
        );
        baseInfo.add(baseFileLabel);

        centerPanel.add(
                baseInfo,
                BorderLayout.NORTH
        );

        compareList =
                new JList<>(compareListModel);

        JPanel comparePanel =
                new JPanel(new BorderLayout());
        comparePanel.setBorder(
                new TitledBorder("Compare Files")
        );
        comparePanel.add(
                new JScrollPane(compareList),
                BorderLayout.CENTER
        );

        centerPanel.add(
                comparePanel,
                BorderLayout.CENTER
        );

        add(
                centerPanel,
                BorderLayout.CENTER
        );

        resultsPanel =
                new JPanel();
        resultsPanel.setLayout(
                new BoxLayout(
                        resultsPanel,
                        BoxLayout.Y_AXIS
                )
        );

        JScrollPane bottomScroll =
                new JScrollPane(resultsPanel);
        bottomScroll.setBorder(
                new TitledBorder("Results")
        );
        bottomScroll.setPreferredSize(
                new Dimension(400, 200)
        );

        add(
                bottomScroll,
                BorderLayout.SOUTH
        );

        baseBtn.addActionListener(
                e -> chooseBase()
        );

        removeBaseBtn.addActionListener(
                e -> removeBase()
        );

        compareBtn.addActionListener(
                e -> chooseFiles()
        );

        removeCompareBtn.addActionListener(
                e -> removeSelectedCompare()
        );

        analyzeBtn.addActionListener(
                e -> analyze()
        );
    }

    private void chooseBase() {

        JFileChooser chooser =
                new JFileChooser();

        if (
                chooser.showOpenDialog(this)
                        ==
                        JFileChooser.APPROVE_OPTION
        ) {

            baseFile =
                    chooser.getSelectedFile();

            baseFileLabel.setText(
                    baseFile.getName()
            );
            removeBaseBtn.setEnabled(true);
        }
    }

    private void removeBase() {
        baseFile = null;
        baseFileLabel.setText("(none)");
        removeBaseBtn.setEnabled(false);
    }

    private void chooseFiles() {

        JFileChooser chooser =
                new JFileChooser();

        chooser.setMultiSelectionEnabled(
                true
        );

        if (
                chooser.showOpenDialog(this)
                        ==
                        JFileChooser.APPROVE_OPTION
        ) {

            for (File f :
                    chooser.getSelectedFiles()) {

                compareFiles.add(f);
                compareListModel.addElement(
                        f.getName()
                );
            }
            removeCompareBtn.setEnabled(
                    !compareFiles.isEmpty()
            );
        }
    }

    private void removeSelectedCompare() {
        int[] indices =
                compareList.getSelectedIndices();

        if (indices.length == 0) {
            return;
        }

        for (int i = indices.length - 1;
                i >= 0; i--) {
            compareFiles.remove(indices[i]);
            compareListModel.remove(
                    indices[i]
            );
        }

        removeCompareBtn.setEnabled(
                !compareFiles.isEmpty()
        );
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
        resultsPanel.removeAll();
        resultsPanel.add(
                new JLabel("Analyzing...")
        );
        resultsPanel.revalidate();
        resultsPanel.repaint();

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
                            displayResults(results);
                        } catch (Exception ex) {
                            resultsPanel.removeAll();
                            resultsPanel.add(
                                    new JLabel(
                                            "Error: " + ex.getMessage()
                                    )
                            );
                            JOptionPane.showMessageDialog(
                                    Option1Panel.this,
                                    "Error: " + ex.getMessage(),
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE
                            );
                        } finally {
                            setButtonsEnabled(true);
                            resultsPanel.revalidate();
                            resultsPanel.repaint();
                        }
                    }
                };

        worker.execute();
    }

    private void displayResults(
            List<RecommendationResult> results
    ) {

        resultsPanel.removeAll();

        for (RecommendationResult r :
                results) {

            JPanel row = new JPanel(
                    new FlowLayout(FlowLayout.LEFT)
            );

            row.add(
                    new JLabel(r.getFileName())
            );

            row.add(
                    Box.createHorizontalStrut(20)
            );

            row.add(
                    new JLabel(
                            String.format(
                                    "Score: %.4f",
                                    r.getSimilarity()
                            )
                    )
            );

            row.add(
                    Box.createHorizontalStrut(20)
            );

            JButton openBtn =
                    new JButton(
                            "Open in Explorer"
                    );

            String filePath =
                    r.getFilePath();

            openBtn.addActionListener(
                    e -> openInExplorer(filePath)
            );

            row.add(openBtn);

            resultsPanel.add(row);
        }

        if (results.isEmpty()) {
            resultsPanel.add(
                    new JLabel("No results found.")
            );
        }

        resultsPanel.revalidate();
        resultsPanel.repaint();
    }

    private void openInExplorer(String path) {

        if (path == null || path.isEmpty()) {
            return;
        }

        try {

            Runtime.getRuntime()
                    .exec(
                            "explorer.exe /select,\""
                                    + path
                                    + "\""
                    );

        } catch (Exception ex) {

            ex.printStackTrace();
        }
    }

    private void setButtonsEnabled(boolean enabled) {
        baseBtn.setEnabled(enabled);
        removeBaseBtn.setEnabled(
                enabled && baseFile != null
        );
        compareBtn.setEnabled(enabled);
        removeCompareBtn.setEnabled(
                enabled && !compareFiles.isEmpty()
        );
        analyzeBtn.setEnabled(enabled);
    }
}
