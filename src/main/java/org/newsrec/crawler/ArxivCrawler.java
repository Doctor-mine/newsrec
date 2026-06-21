package org.newsrec.crawler;

import java.util.List;

public class ArxivCrawler {

    private static final String RSS_URL = "https://export.arxiv.org/rss/cs.AI";

    public List<RSSArticle> crawl() {
        return new RSSParser().parse(RSS_URL, "Arxiv");
    }
}
