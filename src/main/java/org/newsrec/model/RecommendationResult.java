package org.newsrec.model;

public class RecommendationResult {

    private String fileName;

    private double similarity;

    public RecommendationResult(
            String fileName,
            double similarity
    ) {

        this.fileName = fileName;

        this.similarity = similarity;
    }

    public String getFileName() {
        return fileName;
    }

    public double getSimilarity() {
        return similarity;
    }
}