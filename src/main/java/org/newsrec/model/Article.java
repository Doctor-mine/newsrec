package org.newsrec.model;

public class Article {

    private int id;

    private String title;

    private String content;

    private String source;

    private String category;

    private String url;

    public Article() {
    }

    public Article(
            String title,
            String content,
            String source,
            String category,
            String url
    ) {
        this.title = title;
        this.content = content;
        this.source = source;
        this.category = category;
        this.url = url;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getSource() {
        return source;
    }

    public String getCategory() {
        return category;
    }

    public String getUrl() {
        return url;
    }
}