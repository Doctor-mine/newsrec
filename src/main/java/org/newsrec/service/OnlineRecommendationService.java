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
    private List<RSSArticle> lastArticles;
    private String lastFileContent;
    private Map<String, Double> lastIdfMap;

    public List<RecommendationResult> recommend(File uploadedFile, Consumer<String> progress) {
        return recommend(uploadedFile, progress, Integer.MAX_VALUE);
    }

    public List<RSSArticle> getLastArticles() { return lastArticles; }

    public String getLastFileContent() { return lastFileContent; }

    public Map<String, Double> getLastIdfMap() { return lastIdfMap; }

    public List<RecommendationResult> recommend(File uploadedFile, Consumer<String> progress, int topK) {
        progress.accept("Reading file...\n");
        String content = ReaderFactory.getReader(uploadedFile).read(uploadedFile);

        progress.accept("Extracting keywords...\n");
        List<String> keywords = new KeywordExtractor().extractKeywords(content, 5);

        if (keywords.isEmpty()) {
            progress.accept("  - No keywords extracted, using full content.\n");
            keywords = new KeywordExtractor().extractKeywords(
                    content.substring(0, Math.min(content.length(), 1000)), 5
            );
        }

        if (keywords.isEmpty()) {
            keywords.add("artificial intelligence");
            progress.accept("  - Falling back to default keyword.\n");
        }

        progress.accept("Fetching online articles...\n");
        List<RSSArticle> articles = new CrawlerManager().collectArticles(keywords, progress);
        this.lastArticles = articles;

        if (articles.isEmpty()) {
            throw new RuntimeException(
                    "Could not fetch any online articles to compare against. Check your internet connection."
            );
        }

        progress.accept("Computing similarities...\n");
        List<String> corpus = new ArrayList<>();
        corpus.add(content);

        for (RSSArticle article : articles) {
            corpus.add(article.getTitle() + " " + article.getDescription());
        }

        progress.accept("  - Building vocabulary...\n");
        TFIDFVectorizer vectorizer = new TFIDFVectorizer();
        List<List<String>> tokenizedCorpus = new ArrayList<>();

        for (String doc : corpus) {
            tokenizedCorpus.add(vectorizer.preprocess(doc));
        }

        Map<String, Double> idfMap = vectorizer.precomputeIDF(tokenizedCorpus);
        this.lastFileContent = content;
        this.lastIdfMap = idfMap;
        progress.accept("  - Vectorizing documents...\n");

        List<Map<String, Double>> allVectors = new ArrayList<>();
        List<String> allLabels = new ArrayList<>();
        allVectors.add(vectorizer.normalize(vectorizer.buildVector(content, idfMap)));
        allLabels.add("Base file");

        int total = articles.size();

        for (int i = 0; i < total; i++) {
            RSSArticle article = articles.get(i);
            String articleText = article.getTitle() + article.getDescription();
            allVectors.add(vectorizer.normalize(vectorizer.buildVector(articleText, idfMap)));
            allLabels.add(article.getTitle());

            if (i % 100 == 0 && i > 0) {
                progress.accept("  - Processed " + i + "/" + total + "\n");
            }
        }

        progress.accept("  - Building similarity matrix...\n");
        SimilarityMatrix simMatrix = new SimilarityMatrix(allVectors, allLabels);
        List<Map.Entry<Integer, Double>> top = simMatrix.topKWithScores(0, topK);
        List<RecommendationResult> results = new ArrayList<>();

        for (Map.Entry<Integer, Double> entry : top) {
            int idx = entry.getKey();
            RSSArticle article = articles.get(idx - 1);
            results.add(new RecommendationResult(article.getTitle(), "", entry.getValue(), article.getSource(), article.getLink(), article.getDescription()));
        }

        progress.accept("Done.\n");
        return results;
    }
}
