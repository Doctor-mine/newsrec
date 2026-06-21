package org.newsrec.recommendation;

import java.util.*;
import java.util.stream.Collectors;

public class TFIDFVectorizer {

    private final TextPreprocessor preprocessor =
            new TextPreprocessor();

    public Map<String, Double> buildVector(
            String document,
            List<String> corpus
    ) {

        List<List<String>> tokenizedCorpus =
                corpus.stream()
                        .map(preprocessor::tokenize)
                        .collect(Collectors.toList());

        Map<String, Double> idfMap =
                precomputeIDF(tokenizedCorpus);

        return buildVector(document, idfMap);
    }

    public Map<String, Double> buildVector(
            String document,
            Map<String, Double> idfMap
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
                    idfMap.getOrDefault(
                            term,
                            0.0
                    );

            vector.put(
                    term,
                    tf * idf
            );
        }

        return vector;
    }

    public Map<String, Double> precomputeIDF(
            List<List<String>> tokenizedCorpus
    ) {

        Map<String, Long> docCount =
                new HashMap<>();

        for (List<String> docTokens : tokenizedCorpus) {
            Set<String> unique =
                    new HashSet<>(docTokens);
            for (String term : unique) {
                docCount.merge(term, 1L, Long::sum);
            }
        }

        int totalDocs = tokenizedCorpus.size();

        Map<String, Double> idfMap =
                new HashMap<>();

        for (Map.Entry<String, Long> entry :
                docCount.entrySet()) {

            idfMap.put(
                    entry.getKey(),
                    Math.log(
                            (double) totalDocs
                                    /
                                    (1 + entry.getValue())
                    )
            );
        }

        return idfMap;
    }

    public List<String> preprocess(String text) {
        return preprocessor.tokenize(text);
    }

    public Map<String, Double> normalize(
            Map<String, Double> vector
    ) {
        double norm = 0;
        for (double value : vector.values()) {
            norm += value * value;
        }
        norm = Math.sqrt(norm);
        if (norm == 0) return vector;
        Map<String, Double> normalized = new HashMap<>();
        for (Map.Entry<String, Double> entry : vector.entrySet()) {
            normalized.put(entry.getKey(), entry.getValue() / norm);
        }
        return normalized;
    }

    private double tf(
            String term,
            List<String> terms
    ){
        Map<String, Long> freq = new HashMap<>();
        for (String t : terms) {
            freq.merge(t, 1L, Long::sum);
        }
        long count = freq.getOrDefault(term, 0L);
        return (double) count / terms.size();
    }
}