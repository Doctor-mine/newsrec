package org.newsrec.ui;

import org.newsrec.dao.HistoryDAO;
import org.newsrec.model.RecommendationResult;
import org.newsrec.recommendation.RecommendationService;
import org.newsrec.util.CsvExporter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Option1Panel extends JPanel {

    private static final Logger logger = LogManager.getLogger(Option1Panel.class);

    private final int userId;
    private final HistoryDAO historyDAO = new HistoryDAO();

    private File baseFile;

    private final List<File> compareFiles =
            new ArrayList<>();

    private final DefaultListModel<String> compareListModel =
            new DefaultListModel<>();

    private JLabel baseFileLabel;

    private JList<String> compareList;

    private JPanel resultsPanel;

    private JProgressBar progressBar;

    private JButton baseBtn;
    private JButton removeBaseBtn;
    private JButton compareBtn;
    private JButton removeCompareBtn;
    private JButton analyzeBtn;

    private JButton exportBtn;

    private JSpinner topKSpinner;

    private List<RecommendationResult> lastResults;

    public Option1Panel(int userId) {
        this.userId = userId;

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

        exportBtn =
                new JButton("Export CSV");
        exportBtn.setEnabled(false);

        topKSpinner =
                new JSpinner(new SpinnerNumberModel(20, 1, 999, 1));
        ((JSpinner.DefaultEditor) topKSpinner.getEditor())
                .getTextField().setColumns(3);

        top.add(baseBtn);
        top.add(removeBaseBtn);
        top.add(compareBtn);
        top.add(removeCompareBtn);
        top.add(new JLabel("Top-K:"));
        top.add(topKSpinner);
        top.add(analyzeBtn);
        top.add(exportBtn);

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

        progressBar =
                new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);

        JScrollPane bottomScroll =
                new JScrollPane(resultsPanel);
        bottomScroll.setBorder(
                new TitledBorder("Results")
        );
        bottomScroll.setPreferredSize(
                new Dimension(400, 200)
        );

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(progressBar, BorderLayout.NORTH);
        southPanel.add(bottomScroll, BorderLayout.CENTER);

        add(
                southPanel,
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

        exportBtn.addActionListener(e -> exportCsv());
    }

    private void exportCsv() {
        if (lastResults == null || lastResults.isEmpty()) return;

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("analysis_results.csv"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            CsvExporter.export(lastResults, chooser.getSelectedFile());
            JOptionPane.showMessageDialog(this, "Exported to " + chooser.getSelectedFile().getName());
        }
    }

    private void chooseBase() {

        JFileChooser chooser =
                new JFileChooser();
        chooser.setFileFilter(
                new FileNameExtensionFilter(
                        "Documents (PDF, DOCX)", "pdf", "docx"
                )
        );

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
        exportBtn.setEnabled(false);
    }

    private void chooseFiles() {

        JFileChooser chooser =
                new JFileChooser();
        chooser.setFileFilter(
                new FileNameExtensionFilter(
                        "Documents (PDF, DOCX)", "pdf", "docx"
                )
        );

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
        progressBar.setVisible(true);
        resultsPanel.revalidate();
        resultsPanel.repaint();

        int topK = (int) topKSpinner.getValue();

        SwingWorker<List<RecommendationResult>, Void> worker =
                new SwingWorker<>() {
                    @Override
                    protected List<RecommendationResult> doInBackground() {
                        RecommendationService service =
                                new RecommendationService();
                        return service.compareFiles(
                                baseFile,
                                compareFiles,
                                topK
                        );
                    }

                    @Override
                    protected void done() {
                        try {
                            List<RecommendationResult> results = get();
                            lastResults = results;
                            saveHistory(results);
                            displayResults(results);
                            exportBtn.setEnabled(!results.isEmpty());
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
                            progressBar.setVisible(false);
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

    private void saveHistory(List<RecommendationResult> results) {
        if (userId <= 0 || baseFile == null) return;
        String baseName = baseFile.getName();
        for (RecommendationResult r : results) {
            historyDAO.save(userId, baseName, r.getFileName(), r.getSimilarity());
        }
    }

    private void openInExplorer(String path) {

        if (path == null || path.isEmpty()) {
            return;
        }

        try {

            Runtime.getRuntime()
                    .exec(
                            new String[]{
                                    "explorer.exe",
                                    "/select,",
                                    path
                            }
                    );

        } catch (Exception ex) {

            logger.error("Failed to open in explorer for path: {}", path, ex);
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
