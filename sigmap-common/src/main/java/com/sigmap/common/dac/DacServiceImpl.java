package com.sigmap.common.dac;

import com.sigmap.common.dac.exeption.InputFileDocument;
import com.sigmap.common.dac.exeption.LoadDocumentExeption;
import com.sigmap.common.dac.exeption.SaveTempDocument;
import com.sigmap.common.dac.exeption.TemplateFileNameNotFound;
import com.sigmap.common.dac_docs.DacDocs;
import com.sigmap.common.dyna_docs.DynaDocs;
import com.sigmap.common.dyna_docs.DynaDocsRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Slf4j
@Service
@AllArgsConstructor
public class DacServiceImpl implements DacService {

    private final DacRepository dacDocsRepository;
    private final  DynaDocsRepository dynaDocsRepository;

    @Override
    public File replaceBookmarksAndGenerateDOC(Integer dacId, Integer dacVersion ,String basePath, String customDirPath ) {
        log.debug("Dac id is : {} , dac version is : {} , input path is : {} and output path is : {}", dacId, dacVersion, basePath,customDirPath);

        // get template file name from database by dacId and dacVersion
        String templateFileName = getTemplateFileName(dacId, dacVersion);


        if (templateFileName == null || templateFileName.isEmpty()) {
            throw new TemplateFileNameNotFound( dacId , dacVersion);
        }

        log.debug("template file name is : ---- : " + templateFileName);

        File file = new File(basePath + templateFileName);

        FileInputStream resource = inputFileDocument(file);

        XWPFDocument document = loadDocument(resource);

        Map<String, String> bookmarks = createBookmarkMap(dacId, dacVersion);
        replaceBookmarks(document, bookmarks);

        File tempDocxFile = saveTempDocument(document, templateFileName, customDirPath);
        return tempDocxFile; // <-- Return the generated file
    }

    private String getTemplateFileName(Integer dacId, Integer dacVersion) {
        // Récupérer une entrée de dac_docs pour obtenir dyna_docs_id
        List<DacDocs> dacDocsList = dacDocsRepository.findByDacIdAndDacVer(dacId, dacVersion);
        if (dacDocsList.isEmpty()) {
            throw new TemplateFileNameNotFound( dacId, dacVersion);
        }
        // Prendre le premier dyna_docs_id (suppose que tous les dac_docs pour ce dac_id/dac_ver pointent vers le même dyna_docs)
        //Integer dynaDocsId = dacDocsList.get(0).getId().getDinaDocsFields().getId().getDynaDocsId().
       // Integer dynaDocsId = dacDocsList.get(0).getId().getDinaDocsFields().getId().getDynaDocsId().getId().getDynaDocsId();

        // Récupérer le template_file_name depuis dyna_docs
        //Optional<DynaDocs> dynaDocs = dynaDocsRepository.findById(dynaDocsId);
        //return dynaDocs.map(DynaDocs::getTemplateFileName).orElseThrow(() -> new TemplateFileNameNotFound( dacId, dacVersion));
        return "dacDocsList.get(0).getId().getDinaDocsFields().getId().getDynaDocsId().getDynaDocsId().getTemplateFileName()";
    }

    private FileInputStream inputFileDocument (File file) {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new InputFileDocument();
        }
    }

    private XWPFDocument loadDocument(FileInputStream fis) {
        try {
            return new XWPFDocument(fis);
        } catch (IOException e) {
            throw new LoadDocumentExeption();
        }
    }

    private Map<String, String> createBookmarkMap(Integer dacId, Integer dacVersion) {
        Map<String, String> bookmarks = new HashMap<>();
        List<DacDocs> dacDocsList = dacDocsRepository.findByDacIdAndDacVer(dacId, dacVersion);
        /*for (DacDocs dacDoc : dacDocsList) {
            DynaDocsFields dynaDocsFields = dacDoc.getId().getDinaDocsFields();
            if (dynaDocsFields != null && dynaDocsFields.getId() != null && dynaDocsFields.getId().getDynaFieldsId().getDynaFieldId() != null) {
                String bookmarkName = dynaDocsFields.getId().getDynaFieldsId().getName();
                String fieldValue = dacDoc.getFieldValue();
                log.debug("Bookmark: {} -> Value: {}", bookmarkName, fieldValue);
                bookmarks.put(bookmarkName, fieldValue);
            } else {
                log.error("Navigation failed for dacDoc: {}", dacDoc);
            }
        }*/
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

    private File saveTempDocument(XWPFDocument document, String docPath, String customDirPath) {
        // Custom directory
       // String customDirPath = "E:\\word\\temp";
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
        catch (IOException e) {
            throw new SaveTempDocument();
        }

        return tempFile;
    }

}

