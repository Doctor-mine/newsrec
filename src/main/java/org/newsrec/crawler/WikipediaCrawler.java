package org.newsrec.crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class WikipediaCrawler {

    private static final Logger logger = LogManager.getLogger(WikipediaCrawler.class);

    public List<RSSArticle> search(
            String keyword
    ) {

        List<RSSArticle> articles =
                new ArrayList<>();

        try {

            String url =
                    "https://en.wikipedia.org/wiki/"
                            + keyword.replace(" ", "_");

            Document doc =
                    Jsoup.connect(url)
                            .timeout(5000)
                            .get();

            String title =
                    doc.title();

            String description =
                    doc.select("p")
                            .first()
                            .text();

            articles.add(
                    new RSSArticle(
                            title,
                            url,
                            description,
                            "Wikipedia"
                    )
            );

        } catch (Exception e) {

            logger.error("Failed to fetch Wikipedia page for keyword: {}", keyword, e);
        }

        return articles;
    }
}