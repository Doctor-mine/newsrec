package org.newsrec.crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class WikipediaCrawler {

    public List<RSSArticle> search(
            String keyword
    ) {

        List<RSSArticle> articles =
                new ArrayList<>();

        try {

            String url =
                    "https://en.wikipedia.org/wiki/"
                            +
                            URLEncoder.encode(
                                    keyword,
                                    StandardCharsets.UTF_8
                            );

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

            e.printStackTrace();
        }

        return articles;
    }
}