package org.newsrec.service;

import org.newsrec.recommendation.TextPreprocessor;

import java.util.*;
import java.util.stream.Collectors;

public class KeywordExtractor {

    private final TextPreprocessor processor = new TextPreprocessor();

    public List<String> extractKeywords(String text, int topN) {
        List<String> words = processor.tokenize(text);

        Map<String, Integer> frequency = new HashMap<>();

        for (String word : words) {
            frequency.put(word, frequency.getOrDefault(word, 0) + 1);
        }

        return frequency.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(topN)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
