package org.newsrec.model;

public class OnlineArticleResult {

    private String title;

    private String source;

    private String url;

    private double similarity;

    public OnlineArticleResult(
            String title,
            String source,
            String url,
            double similarity
    ) {

        this.title = title;

        this.source = source;

        this.url = url;

        this.similarity = similarity;
    }

    public String getTitle() {
        return title;
    }

    public String getSource() {
        return source;
    }

    public String getUrl() {
        return url;
    }

    public double getSimilarity() {
        return similarity;
    }
}