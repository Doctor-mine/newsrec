package org.newsrec.crawler;

public class RSSArticle {

    private String title;

    private String link;

    private String description;

    private String source;

    public RSSArticle() {
    }

    public RSSArticle(
            String title,
            String link,
            String description,
            String source
    ) {
        this.title = title;
        this.link = link;
        this.description = description;
        this.source = source;
    }

    public String getTitle() {
        return title;
    }

    public String getLink() {
        return link;
    }

    public String getDescription() {
        return description;
    }

    public String getSource() {
        return source;
    }
}