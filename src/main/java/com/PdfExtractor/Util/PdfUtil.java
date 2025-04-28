package com.PdfExtractor.Util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.image.BufferedImage;
import java.io.IOException;

public class PdfUtil {

    // Render the first page as a BufferedImage at the given DPI
    public static BufferedImage renderFirstPageAsImage(PDDocument document, float dpi) throws IOException {
        PDFRenderer renderer = new PDFRenderer(document);
        return renderer.renderImageWithDPI(0, dpi, ImageType.RGB);
    }

    // Extract full text from the PDF document using PDFBox's PDFTextStripper
    public static String extractText(PDDocument document) throws IOException {
        org.apache.pdfbox.text.PDFTextStripper stripper = new org.apache.pdfbox.text.PDFTextStripper();
        return stripper.getText(document);
    }

    // Stub for title extraction (for demo, we assume the title is the first line)
    public static String extractTitle(String text) {
        String[] lines = text.split("\\r?\\n");
        return lines.length > 0 ? lines[0] : "Unknown Title";
    }

    // Stub for subtitle extraction (assume second line)
    public static String extractSubtitle(String text) {
        String[] lines = text.split("\\r?\\n");
        return lines.length > 1 ? lines[1] : "Unknown Subtitle";
    }

    // Stub for summary extraction (first 200 characters)
    public static String extractSummary(String text) {
        return text.length() > 200 ? text.substring(0, 200) : text;
    }

    // Stub for company name extraction
    public static String extractCompanyName(String text) {
        if (text.contains("Inc"))
            return "Example Inc";
        else if (text.contains("LLC"))
            return "Example LLC";
        else
            return "Unknown Company";
    }

    // Stub for job title extraction
    public static String extractJobTitle(String text) {
        return "Default Job Title";
    }

    // Stub for job level extraction
    public static String extractJobLevel(String text) {
        return "Default Job Level";
    }

    // Derive image domain based on company name (naively)
    public static String extractImageDomain(String companyName) {
        return companyName.toLowerCase().replaceAll("\\s+", "") + ".com";
    }

    // Build favicon URL based on the image domain
    public static String extractFaviconUrl(String imageDomain) {
        return "https://www.google.com/s2/favicons?sz=64&domain=" + imageDomain;
    }

    // Stub for categories extraction
    public static String extractCategories(String companyName) {
        return "Default Category";
    }
}
