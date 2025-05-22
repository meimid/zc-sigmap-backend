package com.sigmap.common.dac;

import com.sigmap.common.dac_docs.DacDocs;
import com.sigmap.common.dyna_docs.DynaDocs;
import com.sigmap.common.dyna_docs.DynaDocsRepository;
import com.sigmap.common.dyna_docs_fields.DynaDocsFields;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Slf4j
@Service
public class DacServiceImpl implements DacService {

    @Autowired
    DacRepository dacDocsRepository;

    @Autowired
    private DynaDocsRepository dynaDocsRepository;


    @Override
    public File replaceBookmarksAndGeneratePDF(Integer dacId, Integer dacVersion) throws Exception {


        String templateFileName = getTemplateFileName(dacId, dacVersion);
        if (templateFileName == null) {
            throw new IllegalArgumentException("No document found for dacId: " + dacId + ", dacVersion: " + dacVersion);
        }

        log.info("template file name is : ---- : " + templateFileName);


        String basePath = "E:\\word\\templates\\";
        File file = new File(basePath + templateFileName);
        FileInputStream resource = new FileInputStream(file);

        // FileInputStream fis = new FileInputStream(resource.getFile());
        XWPFDocument document = loadDocument(resource);

        Map<String, String> bookmarks = createBookmarkMap(dacId, dacVersion);
        replaceBookmarks(document, bookmarks);

        File tempDocxFile = saveTempDocument(document, templateFileName);

        log.info("Generated PDF file: " + tempDocxFile);

        return tempDocxFile; // <-- Return the generated PDF file
    }

    private String getTemplateFileName(Integer dacId, Integer dacVersion) {
        // Récupérer une entrée de dac_docs pour obtenir dyna_docs_id
        List<DacDocs> dacDocsList = dacDocsRepository.findByDacIdAndDacVer(dacId, dacVersion);
        if (dacDocsList.isEmpty()) {
            return null;
        }
        // Prendre le premier dyna_docs_id (suppose que tous les dac_docs pour ce dac_id/dac_ver pointent vers le même dyna_docs)
        Integer dynaDocsId = dacDocsList.get(0).getId().getDinaDocsFields().getId().getDynaDocsId().getDynaDocsId();

        // Récupérer le template_file_name depuis dyna_docs
        Optional<DynaDocs> dynaDocs = dynaDocsRepository.findById(dynaDocsId);
        return dynaDocs.map(DynaDocs::getTemplateFileName).orElse(null);
    }

    private XWPFDocument loadDocument(FileInputStream fis) throws Exception {
        return new XWPFDocument(fis);
    }

    private Map<String, String> createBookmarkMap(Integer dacId, Integer dacVersion) {
        Map<String, String> bookmarks = new HashMap<>();
        List<DacDocs> dacDocsList = dacDocsRepository.findByDacIdAndDacVer(dacId, dacVersion);
        for (DacDocs dacDoc : dacDocsList) {
            DynaDocsFields dynaDocsFields = dacDoc.getId().getDinaDocsFields();
            if (dynaDocsFields != null && dynaDocsFields.getId() != null && dynaDocsFields.getId().getDynaFieldsId().getDynaFieldId() != null) {
                String bookmarkName = dynaDocsFields.getId().getDynaFieldsId().getName();
                String fieldValue = dacDoc.getFieldValue();
                log.debug("Bookmark: {} -> Value: {}", bookmarkName, fieldValue);
                bookmarks.put(bookmarkName, fieldValue);
            } else {
                log.error("Navigation failed for dacDoc: {}", dacDoc);
            }
        }
        return bookmarks;
    }



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
                            XWPFRun run = paragraph.createRun();
                            run.setText(bookmarks.get(bookmarkName));
                            paragraph.getCTP().getDomNode().insertBefore(run.getCTR().getDomNode(), bookmark.getDomNode());
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

