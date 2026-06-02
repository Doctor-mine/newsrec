package org.newsrec.recommendation;

import java.util.*;

public class TFIDFVectorizer {

    private final TextPreprocessor preprocessor =
            new TextPreprocessor();

    public Map<String, Double> buildVector(
            String document,
            List<String> corpus
    ) {

        List<String> terms =
                preprocessor.tokenize(document);

        Map<String, Double> vector =
                new HashMap<>();

        Set<String> uniqueTerms =
                new HashSet<>(terms);

        for(String term : uniqueTerms){

            double tf =
                    tf(term, terms);

            double idf =
                    idf(term, corpus);

            vector.put(
                    term,
                    tf * idf
            );
        }

        return vector;
    }

    private double tf(
            String term,
            List<String> terms
    ){

        long count =
                terms.stream()
                        .filter(t -> t.equals(term))
                        .count();

        return (double) count /
                terms.size();
    }

    private double idf(
            String term,
            List<String> corpus
    ) {

        long count = corpus.stream()
                .filter(doc ->
                        preprocessor
                                .tokenize(doc)
                                .contains(term))
                .count();

        return Math.log(
                (double) corpus.size()
                        /
                        (1 + count)
        );
    }
}