package org.newsrec.reader;

import java.io.File;

public class ReaderFactory {

    public static DocumentReader getReader(
            File file
    ) {

        String name =
                file.getName().toLowerCase();

        if(name.endsWith(".pdf")) {
            return new PDFReader();
        }

        if(name.endsWith(".docx")) {
            return new DOCXReader();
        }

        throw new IllegalArgumentException(
                "Unsupported file"
        );
    }
}