package org.newsrec.model;

public class RecommendationResult {

    private String fileName;

    private String filePath;

    private double similarity;

    public RecommendationResult(
            String fileName,
            double similarity
    ) {
        this(fileName, "", similarity);
    }

    public RecommendationResult(
            String fileName,
            String filePath,
            double similarity
    ) {

        this.fileName = fileName;
        this.filePath = filePath;
        this.similarity = similarity;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public double getSimilarity() {
        return similarity;
    }
}