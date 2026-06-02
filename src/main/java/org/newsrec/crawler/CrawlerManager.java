package org.newsrec.crawler;

import java.util.ArrayList;
import java.util.List;

public class CrawlerManager {

    public List<RSSArticle> collectArticles(
            List<String> keywords
    ) {

        List<RSSArticle> articles =
                new ArrayList<>();

        articles.addAll(
                new ArxivCrawler()
                        .crawl()
        );

        articles.addAll(
                new ScienceDailyCrawler()
                        .crawl()
        );

        for (String keyword : keywords) {
            articles.addAll(
                    new WikipediaCrawler()
                            .search(keyword)
            );
        }

        return articles;
    }
}