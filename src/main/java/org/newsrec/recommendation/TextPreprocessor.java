package org.newsrec.recommendation;

import java.util.*;
import java.util.stream.Collectors;

public class TextPreprocessor {

    private static final Set<String> STOP_WORDS = Set.of(
            "a", "about", "above", "after", "again", "against", "all", "am", "an",
            "and", "any", "are", "as", "at", "be", "because", "been", "before",
            "being", "below", "between", "both", "but", "by", "can", "could",
            "did", "do", "does", "done", "each", "few", "for", "from", "further",
            "get", "had", "has", "have", "having", "he", "her", "here", "hers",
            "herself", "him", "himself", "his", "how", "i", "if", "in", "into",
            "is", "it", "its", "itself", "just", "me", "more", "most", "my",
            "myself", "no", "nor", "not", "now", "of", "off", "on", "once",
            "only", "or", "other", "our", "ours", "ourselves", "out", "over",
            "own", "per", "s", "said", "same", "she", "should", "so", "some",
            "such", "t", "than", "that", "the", "their", "them", "themselves",
            "then", "there", "these", "they", "this", "those", "through", "to",
            "too", "under", "until", "up", "upon", "use", "used", "uses", "very",
            "was", "we", "were", "what", "when", "where", "which", "while",
            "who", "whom", "why", "will", "with", "within", "without", "would",
            "you", "your", "yours", "yourself", "yourselves"
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
