package org.newsrec.recommendation;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SimilarityMatrix {

    private final double[][] matrix;
    private final List<String> labels;

    public SimilarityMatrix(List<Map<String, Double>> vectors, List<String> labels) {
        int n = vectors.size();
        if (labels.size() != n) {
            throw new IllegalArgumentException("labels size (" + labels.size() + ") must match vectors size (" + n + ")");
        }
        this.matrix = new double[n][n];
        this.labels = List.copyOf(labels);

        for (int i = 0; i < n; i++) {
            Map<String, Double> vi = vectors.get(i);
            for (int j = i; j < n; j++) {
                Map<String, Double> vj = vectors.get(j);
                double dot = 0;
                for (Map.Entry<String, Double> e : vi.entrySet()) {
                    dot += e.getValue() * vj.getOrDefault(e.getKey(), 0.0);
                }
                matrix[i][j] = dot;
                matrix[j][i] = dot;
            }
        }
    }

    public int size() { return matrix.length; }

    public double get(int i, int j) { return matrix[i][j]; }

    public double[] getRow(int i) { return matrix[i]; }

    public String getLabel(int i) { return labels.get(i); }

    public List<Integer> topK(int i, int k) {
        return IntStream.range(0, size())
                .filter(j -> j != i)
                .boxed()
                .sorted(Comparator.comparingDouble((Integer j) -> matrix[i][j]).reversed())
                .limit(k)
                .collect(Collectors.toList());
    }

    public List<Map.Entry<Integer, Double>> topKWithScores(int i, int k) {
        return IntStream.range(0, size())
                .filter(j -> j != i)
                .boxed()
                .map(j -> Map.entry(j, matrix[i][j]))
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .limit(k)
                .collect(Collectors.toList());
    }
}
