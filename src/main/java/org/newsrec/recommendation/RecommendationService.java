package org.newsrec.recommendation;

import org.newsrec.model.RecommendationResult;
import org.newsrec.reader.ReaderFactory;

import java.io.File;
import java.util.*;

public class RecommendationService {

    private final TFIDFVectorizer vectorizer =
            new TFIDFVectorizer();

    private final CosineSimilarity cosine =
            new CosineSimilarity();

    public List<RecommendationResult> compareFiles(

            File baseFile,

            List<File> compareFiles

    ) {

        try {

            String baseText =
                    ReaderFactory
                            .getReader(baseFile)
                            .read(baseFile);

            List<String> corpus =
                    new ArrayList<>();

            corpus.add(baseText);

            for(File f : compareFiles){

                corpus.add(
                        ReaderFactory
                                .getReader(f)
                                .read(f)
                );
            }

            Map<String, Double> baseVector =
                    vectorizer.buildVector(
                            baseText,
                            corpus
                    );

            List<RecommendationResult> results =
                    new ArrayList<>();

            for(File file : compareFiles){

                String text =
                        ReaderFactory
                                .getReader(file)
                                .read(file);

                Map<String, Double> vector =
                        vectorizer.buildVector(
                                text,
                                corpus
                        );

                double similarity =
                        cosine.calculate(
                                baseVector,
                                vector
                        );

                results.add(
                        new RecommendationResult(
                                file.getName(),
                                file.getAbsolutePath(),
                                similarity
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

        } catch (Exception e){

            throw new RuntimeException(e);
        }
    }
}