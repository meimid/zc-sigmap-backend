package com.sigmap.common.dac;

import com.sigmap.common.dac.exeption.InputFileDocument;
import com.sigmap.common.dac.exeption.LoadDocumentExeption;
import com.sigmap.common.dac.exeption.SaveTempDocument;
import com.sigmap.common.dac.exeption.TemplateFileNameNotFound;
import com.sigmap.common.dac_docs.DacDocs;
import com.sigmap.common.dac_docs.DacDocsRepository;
import com.sigmap.common.dyna_docs.DynaDocs;
import com.sigmap.common.dyna_docs.DynaDocsRepository;
import com.sigmap.common.dyna_fields.DynaFields;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBookmark;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class DacListServiceImpl {

    private final DacDocsRepository dacDocsRepository;
    private final DynaDocsRepository dynaDocsRepository;
    private final DacRepository dacRepository;

    public List<String> replaceBookmarksAndGenerateListDOC(Integer dacId, Integer dacVersion, String basePath, String customDirPath) {
        log.debug("Processing Dac id: {}, dac version: {}, input path: {}, output path: {} ", dacId, dacVersion, basePath, customDirPath);

        // Étape 1 : Valider les paramètres
        validateParams(dacId, dacVersion, basePath, customDirPath);

        // Vérifier l'existence du Dac
        Dac dac = dacRepository.findById(new DacId(dacId, dacVersion))
                .orElseThrow(() -> new IllegalArgumentException("Dac not found for ID: " + dacId + ", Version: " + dacVersion));

        // Vérifier l'existence du Dac.ppm
        if (dac.getPpm()==null ){

            log.info("dac.getPpm().getId(): {}, dac.getPpm().getVersion(): {} ",
                    dac.getPpm().getId(), dac.getPpm().getVersion());

            log.info ("**********************************");
            throw new IllegalArgumentException("Dac must have ppm");
        }


        // Étape 2 : Récupérer les documents actifs associés
        List<DynaDocs> dynaDocsList = getActiveDynaDocsForDac(dac.getCategory().getId());
        if (dynaDocsList.isEmpty()) {
            log.warn("No active documents found for Dac ID: {}, Version: {}", dacId, dacVersion);
            throw new TemplateFileNameNotFound(dacId, dacVersion);
        }

        List<String> generatedFilePaths = new ArrayList<>();

        // Étape 3 : Traiter chaque document
        for (DynaDocs dynaDoc : dynaDocsList) {
            String templateFileName = dynaDoc.getTemplateFileName();
            log.debug("Processing template file: {} for DynaDocs ID: {}, Version: {}",
                    templateFileName, dynaDoc.getId().getDynaDocsId(), dynaDoc.getId().getDynaDocsVer());

            File file = new File(basePath, templateFileName);
            if (!file.exists()) {
                log.error("Template file not found: {}", file.getAbsolutePath());
                continue;
            }

            try (FileInputStream resource = inputFileDocument(file);
                 XWPFDocument document = loadDocument(resource)) {

                // Étape 4 : Créer la carte des signets pour ce document
                Map<String, String> bookmarks = createBookmarkMap(
                        dacId, dacVersion,
                        dynaDoc.getId().getDynaDocsId(),
                        dynaDoc.getId().getDynaDocsVer());

                // Étape 5 : Remplacer les signets
                replaceBookmarks(document, bookmarks);

                // Étape 6 : Enregistrer le document
                String outputFilePath = saveTempDocument(document, templateFileName, customDirPath);
                generatedFilePaths.add(outputFilePath);
                log.info("Generated document: {}", outputFilePath);

            } catch (IOException e) {
                log.error("Error processing document {}: {}", templateFileName, e.getMessage());
            }
        }

        if (generatedFilePaths.isEmpty()) {
            throw new RuntimeException("No documents were generated for Dac ID: " + dacId + ", Version: " + dacVersion);
        }

        return generatedFilePaths;

    }

    private void validateParams(Integer dacId, Integer dacVersion, String basePath, String customDirPath) {

        if (dacId == null || dacVersion == null) {
            throw new IllegalArgumentException("Dac ID and version must not be null");
        }
        if (basePath == null || basePath.isEmpty() || customDirPath == null || customDirPath.isEmpty()) {
            throw new IllegalArgumentException("Base path and custom directory path must not be null or empty");
        }
    }
    private List<DynaDocs> getActiveDynaDocsForDac(Integer categorieId){
        // recupere tous les documents actif pour le dac donné
        List<DynaDocs> dynaDocsList = dynaDocsRepository.findAllActifForCategorie(categorieId);
       // trier par dyna_doc_order
        return dynaDocsList.stream()
                .sorted(Comparator.comparing(DynaDocs::getDynaDocOrder))
                .collect(Collectors.toList());
    }
    private FileInputStream inputFileDocument(File file) {
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

    private Map<String, String> createBookmarkMap(Integer dacId, Integer dacVersion, Integer dynaDocsId, Integer dynaDocsVer) {
        Map<String, String> bookmarks = new HashMap<>();

        // Get DacDocs entries for the given DAC
        List<DacDocs> dacDocsList = dacDocsRepository.findByDacIdAndDacVer(dacId, dacVersion);

        // Process each DacDocs entry
        for (DacDocs dacDoc : dacDocsList) {
            DynaFields dynaField = dacDoc.getId().getDynaField();
            if (dynaField != null &&
                dynaField.getDynaDocs().getId().getDynaDocsId().equals(dynaDocsId) &&
                dynaField.getDynaDocs().getId().getDynaDocsVer().equals(dynaDocsVer)) {

                String bookmarkName = dynaField.getName();
                String fieldValue = dacDoc.getFieldValue();

                if (bookmarkName != null && fieldValue != null) {
                    log.info("Processing bookmark: {} -> Value: {}", bookmarkName, fieldValue);
                    bookmarks.put(bookmarkName, fieldValue);
                }
            }
        }

        return bookmarks;
    }

    private void replaceBookmarks(XWPFDocument document, Map<String, String> bookmarks) {
        document.getParagraphs().stream()
                .filter(paragraph -> paragraph.getCTP().getBookmarkStartList() != null
                        && !paragraph.getCTP().getBookmarkStartList().isEmpty())
                .forEach(paragraph -> {
                    List<CTBookmark> bookmarksToRemove = new ArrayList<>();
                    paragraph.getCTP().getBookmarkStartList().forEach(bookmark -> {
                        String bookmarkName = bookmark.getName();
                        if (bookmarks.containsKey(bookmarkName)) {
                            // Créer un nouveau run pour le texte de remplacement
                            XWPFRun run = paragraph.createRun();
                            run.setText(bookmarks.get(bookmarkName));
                            paragraph.getCTP().getDomNode().insertBefore(
                                    run.getCTR().getDomNode(), bookmark.getDomNode());
                            bookmarksToRemove.add(bookmark);
                        }
                    });
                    // Supprimer les signets après remplacement
                    bookmarksToRemove.forEach(bookmark -> {
                        paragraph.getCTP().removeBookmarkStart(bookmark.getId().intValue());
                        paragraph.getCTP().removeBookmarkEnd(bookmark.getId().intValue());
                    });
                });
    }

    private String saveTempDocument(XWPFDocument document, String templateFileName, String customDirPath) {
        File customDir = new File(customDirPath);
        if (!customDir.exists()) {
            customDir.mkdirs();
        }

        // Générer un nom de fichier unique
        String outputFileName = templateFileName.replace(".docx",
                "_" + System.currentTimeMillis() + ".docx");
        File tempFile = new File(customDir, outputFileName);

        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            document.write(fos);
        } catch (IOException e) {
            throw new SaveTempDocument();
        }

        return tempFile.getAbsolutePath();
    }

}
