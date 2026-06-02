package org.newsrec.crawler;

import java.util.List;

public class ScienceDailyCrawler {

    private static final String RSS_URL =
            "https://www.sciencedaily.com/rss/computers_math/artificial_intelligence.xml";

    public List<RSSArticle> crawl() {

        return new RSSParser().parse(
                RSS_URL,
                "ScienceDaily"
        );
    }
}