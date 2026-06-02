package org.newsrec.service;

import org.newsrec.crawler.CrawlerManager;
import org.newsrec.crawler.RSSArticle;

import java.util.List;
import java.util.stream.Collectors;

public class TopicSearchService {

    public List<RSSArticle> search(
            String topic
    ) {

        return new CrawlerManager()
                .collectArticles(List.of(topic))
                .stream()
                .filter(article ->

                        article.getTitle()
                                .toLowerCase()
                                .contains(
                                        topic.toLowerCase()
                                )

                                ||

                                article.getDescription()
                                        .toLowerCase()
                                        .contains(
                                                topic.toLowerCase()
                                        )
                )
                .collect(Collectors.toList());
    }
}