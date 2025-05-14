package com.sigmap.common.dac;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBookmark;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Map;



@Slf4j
@Service
public class DacServiceImpl implements DacService {

   // private static final String INPUT_TEMPLATE = "src/main/resources/templates/187.docx"; // Input document path

    @Override
    public File replaceBookmarksAndGeneratePDF(String docPath, Integer dacId, Integer dacVersion) throws Exception {
        ClassPathResource resource = new ClassPathResource("templates/" + docPath);
        FileInputStream fis = new FileInputStream(resource.getFile());
        XWPFDocument document = loadDocument(fis);

        Map<String, String> bookmarks = createBookmarkMap(dacId);
        replaceBookmarks(document, bookmarks);

        File tempFile = saveTempDocument(document);

        //*-**-*--*-
        File pdfTempFile = File.createTempFile("output", ".pdf");

        File pdfFile = new File(tempFile.getAbsolutePath().replace(".docx", ".pdf"));

        log.info("Generated PDF file: " + pdfFile);
        convertToPDF(tempFile, pdfTempFile);

        cleanup(fis, document, tempFile);

        return pdfTempFile; // <-- Return the generated PDF file
    }
    // Placeholder method for PDF conversion
    private XWPFDocument loadDocument(FileInputStream fis) throws Exception {
        return new XWPFDocument(fis);
    }

    private Map<String, String> createBookmarkMap(Integer dacId) {
        return Map.of(
                "EMP_Matags","181181818",
                "date", "May 13, 2025",
                "fonctions", "Software Engineer",
                "Affectation", "Development Team"
        );
    }

    /**
     * Replace bookmarks in the given XWPFDocument with the given Map of values.
     * This method iterates through all paragraphs in the document, and for each
     * paragraph, it checks if the paragraph has a bookmark start. If it does,
     * it checks if the bookmark name is in the given Map of values. If it is,
     * it replaces the text in the first run of the paragraph with the value
     * associated with the bookmark name.
     *
     * @param document the XWPFDocument to process
     * @param bookmarks the Map of values to replace bookmarks with
     */
    private void replaceBookmarks(XWPFDocument document, Map<String, String> bookmarks) {
        for (XWPFParagraph paragraph : document.getParagraphs()) {
            // Check if the paragraph has a bookmark start
            if (paragraph.getCTP().getBookmarkStartList() != null) {
                for (CTBookmark bookmarkStart : paragraph.getCTP().getBookmarkStartList()) {
                    String bookmarkName = bookmarkStart.getName();
                    // Check if the bookmark name is in the given Map of values
                    if (bookmarks.containsKey(bookmarkName)) {
                        // Replace text in the first run of the paragraph
                        for (XWPFRun run : paragraph.getRuns()) {
                            run.setText(bookmarks.get(bookmarkName), 0);
                            break; // Replace text in the first run; adjust if needed
                        }
                    }
                }
            }
        }
    }

    private File saveTempDocument(XWPFDocument document) throws Exception {
        File tempFile = File.createTempFile("tempDoc", ".docx");
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            document.write(fos);
        }
        return tempFile;
    }

    private void convertToPDF(File docxFile, File pdfFile) throws Exception {
        // Placeholder for PDF conversion logic (e.g., using docx4j or Apache PDFBox)
        System.out.println("PDF conversion would happen here: " + pdfFile.getAbsolutePath());
    }

    private void cleanup(FileInputStream fis, XWPFDocument document, File tempFile) throws Exception {
        tempFile.deleteOnExit();
        fis.close();
        document.close();
    }


}

