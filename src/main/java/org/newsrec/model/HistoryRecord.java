package org.newsrec.model;

public class HistoryRecord {

    private int userId;
    private String baseFile;
    private String comparedFile;
    private double similarity;

    public HistoryRecord(int userId, String baseFile, String comparedFile, double similarity) {
        this.userId = userId;
        this.baseFile = baseFile;
        this.comparedFile = comparedFile;
        this.similarity = similarity;
    }

    public int getUserId() { return userId; }

    public String getBaseFile() { return baseFile; }

    public String getComparedFile() { return comparedFile; }

    public double getSimilarity() { return similarity; }
}
