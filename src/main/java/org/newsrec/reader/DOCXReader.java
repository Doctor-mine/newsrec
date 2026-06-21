package org.newsrec.reader;

import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.File;
import java.io.FileInputStream;

public class DOCXReader implements DocumentReader {

    @Override
    public String read(File file) {

        try (
                FileInputStream fis =
                        new FileInputStream(file);

                XWPFDocument document =
                        new XWPFDocument(fis)
        ) {

            return document.getParagraphs()
                    .stream()
                    .map(p -> p.getText())
                    .reduce("", (a, b) -> a + "\n" + b);

        } catch (Exception e) {

            throw new RuntimeException(e);
        }
    }
}