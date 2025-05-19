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

        String basePath = "E:\\word\\templates\\";
        File file = new File(basePath + docPath);
        FileInputStream resource = new FileInputStream(file);

        // FileInputStream fis = new FileInputStream(resource.getFile());
        XWPFDocument document = loadDocument(resource);

        Map<String, String> bookmarks = createBookmarkMap(dacId);
        replaceBookmarks(document, bookmarks);

        File tempDocxFile = saveTempDocument(document, docPath);

        log.info("Generated PDF file: " + tempDocxFile);

        return tempDocxFile; // <-- Return the generated PDF file
    }
    // Placeholder method for PDF conversion
    private XWPFDocument loadDocument(FileInputStream fis) throws Exception {
        return new XWPFDocument(fis);
    }

    private Map<String, String> createBookmarkMap(Integer dacId) {
        return Map.of(
                "EMP_Matags","18556",
                "sysdate", "May 19, 2025",
                "EMP_NUMREF", "000145",
                "EMP_NomPrenom", "Mhamdi_Wassim",
                "EMP_Position", "Mhamdi_Wassim"

        );
    }

    /*private void replaceBookmarks(XWPFDocument document, Map<String, String> bookmarks) {
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
    */

    private void replaceBookmarks(XWPFDocument document, Map<String, String> bookmarks) {
        // Ne traiter que les paragraphes qui contiennent potentiellement des signets
        document.getParagraphs().stream()
                .filter(paragraph -> paragraph.getCTP().getBookmarkStartList() != null
                        && !paragraph.getCTP().getBookmarkStartList().isEmpty())
                .forEach(paragraph -> {
                    // Pour chaque signet trouvé dans le paragraphe
                    paragraph.getCTP().getBookmarkStartList().forEach(bookmark -> {
                        String bookmarkName = bookmark.getName();

                        // Si le nom du signet est dans notre Map
                        if (bookmarks.containsKey(bookmarkName)) {
                            // Remplacer le texte dans le premier run du paragraphe
                            if (!paragraph.getRuns().isEmpty()) {
                                paragraph.getRuns().get(0).setText(bookmarks.get(bookmarkName), 0);
                            }
                        }
                    });
                });
    }
    private File saveTempDocument(XWPFDocument document, String docPath) throws Exception {
        // Custom directory
        String customDirPath = "E:\\word\\temp";
        File customDir = new File(customDirPath);

        // Create the directory if it doesn't exist
        if (!customDir.exists()) {
            customDir.mkdirs();
        }

        // Create a unique file name (you can change the logic if you want)
        File tempFile = new File(customDir, docPath);
        //File tempFile = new File(customDir, "tempDoc_" + System.currentTimeMillis() + ".docx");
        // Write the document to the file
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            document.write(fos);
        }

        return tempFile;
    }

}

