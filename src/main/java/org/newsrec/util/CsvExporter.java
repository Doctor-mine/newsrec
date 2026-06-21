package org.newsrec.util;

import org.newsrec.model.RecommendationResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class CsvExporter {

    private static final Logger logger = LogManager.getLogger(CsvExporter.class);

    public static void export(
            List<RecommendationResult> results,
            File outputFile
    ) {

        try (PrintWriter pw =
                     new PrintWriter(
                             outputFile,
                             StandardCharsets.UTF_8
                     )
        ) {

            pw.println("File Name,Source,Similarity,Link,File Path");

            for (RecommendationResult r : results) {

                pw.printf("\"%s\",\"%s\",%.4f,\"%s\",\"%s\"%n",
                        escapeCsv(r.getFileName()),
                        escapeCsv(r.getSource()),
                        r.getSimilarity(),
                        escapeCsv(r.getLink()),
                        escapeCsv(r.getFilePath())
                );
            }

        } catch (Exception e) {

            logger.error("Failed to export CSV to {}", outputFile, e);
        }
    }

    private static String escapeCsv(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }
}
