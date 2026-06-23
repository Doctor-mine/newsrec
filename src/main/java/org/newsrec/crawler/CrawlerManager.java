package org.newsrec.crawler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class CrawlerManager {

    private static final Logger logger = LogManager.getLogger(CrawlerManager.class);

    public List<RSSArticle> collectArticles(List<String> keywords) {
        return collectArticles(keywords, msg -> {});
    }

    public List<RSSArticle> collectArticles(List<String> keywords, Consumer<String> progress) {
        List<RSSArticle> articles = Collections.synchronizedList(new ArrayList<>());

        progress.accept("  - Fetching Arxiv...\n");
        CompletableFuture<Void> arxivFuture = CompletableFuture.runAsync(() ->
                articles.addAll(new ArxivCrawler().crawl())
        ).orTimeout(15, TimeUnit.SECONDS);

        progress.accept("  - Fetching ScienceDaily...\n");
        CompletableFuture<Void> sciDailyFuture = CompletableFuture.runAsync(() ->
                articles.addAll(new ScienceDailyCrawler().crawl())
        ).orTimeout(15, TimeUnit.SECONDS);

        List<CompletableFuture<Void>> wikiFutures = new ArrayList<>();

        for (String keyword : keywords) {
            String kw = keyword;
            wikiFutures.add(CompletableFuture.runAsync(() ->
                    articles.addAll(new WikipediaCrawler().search(kw))
            ).orTimeout(10, TimeUnit.SECONDS));
        }

        if (!wikiFutures.isEmpty()) {
            progress.accept("  - Fetching Wikipedia (" + keywords.size() + " keywords)...\n");
        }

        progress.accept("  - Fetching Semantic Scholar (" + keywords.size() + " keywords)...\n");
        CompletableFuture<Void> ssFuture = CompletableFuture.runAsync(() ->
                articles.addAll(new SemanticScholarCrawler().search(keywords))
        ).orTimeout(15, TimeUnit.SECONDS);

        CompletableFuture<Void> all = CompletableFuture.allOf(arxivFuture, sciDailyFuture);
        CompletableFuture<Void> allWiki = CompletableFuture.allOf(
                withStream(wikiFutures, ssFuture)
        );

        try {
            all.get(20, TimeUnit.SECONDS);
        } catch (Exception e) {
            progress.accept("  - Arxiv/ScienceDaily timed out.\n");
        }

        try {
            allWiki.get(15, TimeUnit.SECONDS);
        } catch (Exception e) {
            progress.accept("  - Wikipedia/Semantic Scholar timed out.\n");
        }

        progress.accept("  - Got " + articles.size() + " articles total.\n");
        return articles;
    }

    private CompletableFuture<Void>[] withStream(List<CompletableFuture<Void>> futures, CompletableFuture<Void> extra) {
        List<CompletableFuture<Void>> all = new ArrayList<>(futures);
        all.add(extra);
        return all.toArray(new CompletableFuture[0]);
    }
}
