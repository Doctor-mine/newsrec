package org.newsrec.model;

public class RecommendationResult {

    private String fileName;
    private String filePath;
    private double similarity;
    private String source;
    private String link;
    private String description;

    public RecommendationResult(String fileName, double similarity) {
        this(fileName, "", similarity, "", "", "");
    }

    public RecommendationResult(String fileName, String filePath, double similarity) {
        this(fileName, filePath, similarity, "", "", "");
    }

    public RecommendationResult(String fileName, String source, String link, double similarity) {
        this(fileName, "", similarity, source, link, "");
    }

    public RecommendationResult(String fileName, String filePath, double similarity, String source, String link) {
        this(fileName, filePath, similarity, source, link, "");
    }

    public RecommendationResult(String fileName, String filePath, double similarity, String source, String link, String description) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.similarity = similarity;
        this.source = source;
        this.link = link;
        this.description = description;
    }

    public String getFileName() { return fileName; }

    public String getFilePath() { return filePath; }

    public double getSimilarity() { return similarity; }

    public String getSource() { return source; }

    public String getLink() { return link; }

    public String getDescription() { return description; }
}
