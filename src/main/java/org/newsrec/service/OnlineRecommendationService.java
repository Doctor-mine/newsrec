package org.newsrec.service;

import org.newsrec.crawler.*;
import org.newsrec.model.RecommendationResult;
import org.newsrec.reader.ReaderFactory;
import org.newsrec.recommendation.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;

public class OnlineRecommendationService {

    private static final Logger logger = LogManager.getLogger(OnlineRecommendationService.class);

    public List<RecommendationResult>
    recommend(File uploadedFile, Consumer<String> progress) {

        progress.accept("Reading file...\n");
        String content =
                ReaderFactory
                        .getReader(uploadedFile)
                        .read(uploadedFile);

        progress.accept("Extracting keywords...\n");
        List<String> keywords =
                new KeywordExtractor()
                        .extractKeywords(
                                content,
                                5
                        );

        if (keywords.isEmpty()) {
            progress.accept("  - No keywords extracted, using full content.\n");
            keywords = new KeywordExtractor()
                    .extractKeywords(
                            content.substring(
                                    0, Math.min(content.length(), 1000)
                            ),
                            5
                    );
        }

        if (keywords.isEmpty()) {
            keywords.add("artificial intelligence");
            progress.accept("  - Falling back to default keyword.\n");
        }

        progress.accept("Fetching online articles...\n");
        List<RSSArticle> articles =
                new CrawlerManager()
                        .collectArticles(keywords, progress);

        if (articles.isEmpty()) {
            throw new RuntimeException(
                    "Could not fetch any online articles to compare against. " +
                    "Check your internet connection."
            );
        }

        progress.accept("Computing similarities...\n");

        List<String> corpus =
                new ArrayList<>();

        corpus.add(content);

        for (RSSArticle article : articles) {
            corpus.add(
                    article.getTitle()
                            + " "
                            + article.getDescription()
            );
        }

        progress.accept("  - Building vocabulary...\n");
        TFIDFVectorizer vectorizer =
                new TFIDFVectorizer();

        List<List<String>> tokenizedCorpus =
                new ArrayList<>();

        for (String doc : corpus) {
            tokenizedCorpus.add(
                    vectorizer
                            .preprocess(doc)
            );
        }

        Map<String, Double> idfMap =
                vectorizer.precomputeIDF(
                        tokenizedCorpus
                );

        CosineSimilarity similarity =
                new CosineSimilarity();

        progress.accept("  - Vectorizing base file...\n");
        Map<String,Double> baseVector =
                vectorizer.buildVector(
                        content,
                        idfMap
                );

        List<RecommendationResult> results =
                new ArrayList<>();

        int total = articles.size();
        int i = 0;

        for (RSSArticle article : articles) {

            String articleText =
                    article.getTitle()
                            + article.getDescription();

            Map<String,Double> vector =
                    vectorizer.buildVector(
                            articleText,
                            idfMap
                    );

            double score =
                    similarity.calculate(
                            baseVector,
                            vector
                    );

            results.add(
                    new RecommendationResult(
                            article.getTitle(),
                            article.getSource(),
                            article.getLink(),
                            score
                    )
            );

            i++;
            if (i % 100 == 0) {
                progress.accept(
                        "  - Processed " + i
                                + "/" + total + "\n"
                    );
            }
        }

        results.sort(
                Comparator.comparing(
                        RecommendationResult
                                ::getSimilarity
                ).reversed()
        );

        progress.accept("Done.\n");
        return results;
    }
}