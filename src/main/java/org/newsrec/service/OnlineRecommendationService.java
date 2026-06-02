package org.newsrec.service;

import org.newsrec.crawler.*;
import org.newsrec.model.RecommendationResult;
import org.newsrec.reader.ReaderFactory;
import org.newsrec.recommendation.*;

import java.io.File;
import java.util.*;

public class OnlineRecommendationService {

    public List<RecommendationResult>
    recommend(File uploadedFile) {

        String content =
                ReaderFactory
                        .getReader(uploadedFile)
                        .read(uploadedFile);

        List<String> keywords =
                new KeywordExtractor()
                        .extractKeywords(
                                content,
                                5
                        );

        if (keywords.isEmpty()) {
            keywords.add("");
        }

        List<RSSArticle> articles =
                new CrawlerManager()
                        .collectArticles(keywords);

        List<String> corpus =
                new ArrayList<>();

        corpus.add(content);

        for(RSSArticle article :
                articles){

            corpus.add(
                    article.getTitle()
                            +
                            " "
                            +
                            article.getDescription()
            );
        }

        TFIDFVectorizer vectorizer =
                new TFIDFVectorizer();

        CosineSimilarity similarity =
                new CosineSimilarity();

        Map<String,Double> baseVector =
                vectorizer.buildVector(
                        content,
                        corpus
                );

        List<RecommendationResult> results =
                new ArrayList<>();

        for(RSSArticle article :
                articles){

            String articleText =
                    article.getTitle()
                            +
                            article.getDescription();

            Map<String,Double> vector =
                    vectorizer.buildVector(
                            articleText,
                            corpus
                    );

            double score =
                    similarity.calculate(
                            baseVector,
                            vector
                    );

            results.add(
                    new RecommendationResult(
                            article.getTitle(),
                            score
                    )
            );
        }

        results.sort(
                Comparator.comparing(
                        RecommendationResult
                                ::getSimilarity
                ).reversed()
        );

        return results;
    }
}