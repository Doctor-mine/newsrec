package org.newsrec.reader;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;

public class PDFReader implements DocumentReader {

    @Override
    public String read(File file) {
        try (PDDocument document = Loader.loadPDF(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (IOException e) {
            throw new RuntimeException("The PDF file '" + file.getName() + "' appears to be damaged or incomplete. Please try a different file.", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read PDF file '" + file.getName() + "': " + e.getMessage(), e);
        }
    }
}
