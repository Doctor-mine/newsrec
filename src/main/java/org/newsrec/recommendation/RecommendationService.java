package org.newsrec.recommendation;

import org.newsrec.model.RecommendationResult;
import org.newsrec.reader.ReaderFactory;

import java.io.File;
import java.util.*;

public class RecommendationService {

    private final TFIDFVectorizer vectorizer = new TFIDFVectorizer();

    public List<RecommendationResult> compareFiles(File baseFile, List<File> compareFiles) {
        return compareFiles(baseFile, compareFiles, Integer.MAX_VALUE);
    }

    public List<RecommendationResult> compareFiles(File baseFile, List<File> compareFiles, int topK) {
        try {
            String baseText = ReaderFactory.getReader(baseFile).read(baseFile);
            Map<File, String> fileTexts = new HashMap<>();
            List<String> corpus = new ArrayList<>();
            corpus.add(baseText);

            for (File f : compareFiles) {
                String text = ReaderFactory.getReader(f).read(f);
                fileTexts.put(f, text);
                corpus.add(text);
            }

            Map<String, Double> baseVector = vectorizer.normalize(vectorizer.buildVector(baseText, corpus));
            List<Map<String, Double>> allVectors = new ArrayList<>();
            allVectors.add(baseVector);
            List<String> allLabels = new ArrayList<>();
            allLabels.add(baseFile.getName());

            for (File file : compareFiles) {
                String text = fileTexts.get(file);
                allVectors.add(vectorizer.normalize(vectorizer.buildVector(text, corpus)));
                allLabels.add(file.getName());
            }

            SimilarityMatrix simMatrix = new SimilarityMatrix(allVectors, allLabels);
            List<Map.Entry<Integer, Double>> top = simMatrix.topKWithScores(0, topK);
            List<RecommendationResult> results = new ArrayList<>();

            for (Map.Entry<Integer, Double> entry : top) {
                int idx = entry.getKey();
                File file = compareFiles.get(idx - 1);
                results.add(new RecommendationResult(file.getName(), file.getAbsolutePath(), entry.getValue()));
            }

            return results;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
