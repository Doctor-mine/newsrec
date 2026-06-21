package org.newsrec.recommendation;

import java.util.*;
import java.util.stream.Collectors;

public class TextPreprocessor {

    private static final Set<String> STOP_WORDS = Set.of(
            "a", "an", "and", "are", "as", "at", "be", "been", "being", "by",
            "for", "from", "has", "have", "had", "he", "her", "his", "in", "is",
            "it", "its", "of", "on", "or", "that", "the", "their", "them", "there",
            "they", "this", "to", "was", "were", "will", "with", "you", "your"
    );

    public List<String> tokenize(String text) {
        return Arrays.stream(
                        text.toLowerCase()
                                .replaceAll("[^a-zA-Z ]", " ")
                                .split("\\s+")
                )
                .filter(word -> word.length() > 2)
                .filter(word -> !STOP_WORDS.contains(word))
                .collect(Collectors.toList());
    }
}
