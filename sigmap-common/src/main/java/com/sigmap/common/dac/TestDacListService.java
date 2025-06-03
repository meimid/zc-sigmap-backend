package com.sigmap.common.dac;


import com.sigmap.common.dac_docs.DacDocs;
import com.sigmap.common.dac_docs.DacDocsRepository;
import com.sigmap.common.dyna_docs.DynaDocs;
import com.sigmap.common.dyna_docs.DynaDocsRepository;
import com.sigmap.common.dyna_fields.DynaFields;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBookmark;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class TestDacListService {


    private final DacDocsRepository dacDocsRepository;
    private final DynaDocsRepository dynaDocsRepository;
    private final DacRepository dacRepository;


    public List<String> generateDocumentsWithBookmarks(Integer dacId, Integer dacVersion, String basePath, String customDirPath) {

        log.info("Processing Dac ID: {}, Version: {}, Base Path: {}, Output Path: {}", dacId, dacVersion, basePath, customDirPath);

        // Step 1: Validate parameters
        validateParams(dacId, dacVersion, basePath, customDirPath);

        // Step 1: Retrieve Dac by ID and version
        DacId dacIdObj = new DacId(dacId, dacVersion);
        Dac dac = dacRepository.findById(dacIdObj)
                .orElseThrow(() -> new IllegalArgumentException("Dac not found for ID: " + dacId + ", Version: " + dacVersion));
        log.info("Retrieved Dac: {}", dac);

        // Check if Dac has a Ppm
        if (dac.getPpm() == null) {
            log.error("Dac must have a Ppm");
            throw new IllegalArgumentException("Dac must have a Ppm");
        }
        log.info("Dac Ppm: {}", dac.getPpm());

        // Step 2: Get all active documents by Dac categoryId, sorted by dynaDocOrder
        List<DynaDocs> dynaDocsList = getActiveDynaDocsForDac(dac.getCategory().getId());
        if (dynaDocsList.isEmpty()) {
            log.warn("No active documents found for Dac ID: {}, Version: {}", dacId, dacVersion);
            throw new RuntimeException("No active documents found for Dac ID: " + dacId + ", Version: " + dacVersion);
        }
        log.info("Active DynaDocs count: {}", dynaDocsList.size());

        List<String> generatedFilePaths = new ArrayList<>();

        // Step 3: Process each document in order
        for (DynaDocs dynaDoc : dynaDocsList) {
            String templateFileName = dynaDoc.getTemplateFileName();
            log.info("Processing DynaDocs ID: {}, Version: {}, Template: {}",
                    dynaDoc.getDynaDocsId(), dynaDoc.getDynaDocsVer(), templateFileName);

            File templateFile = new File(basePath, templateFileName);
            if (!templateFile.exists()) {
                log.error("Template file not found: {}", templateFile.getAbsolutePath());
                continue;
            }

            try (FileInputStream fis = new FileInputStream(templateFile);
                 XWPFDocument document = new XWPFDocument(fis)) {

                // Create bookmark map with values from DacDocs, respecting field order
                Map<String, String> bookmarks = createBookmarkMap(
                        dacId, dacVersion, dynaDoc.getDynaDocsId(), dynaDoc.getDynaDocsVer());
                log.info("//////////////: {}",bookmarks);

                // Replace bookmarks in the document
                replaceBookmarks(document, bookmarks);
                log.info("okkkkk ----------");


                // Save the generated document
                String outputFilePath = saveTempDocument(document, templateFileName, customDirPath);
                generatedFilePaths.add(outputFilePath);
                log.info("Generated document: {}", outputFilePath);

            } catch (IOException e) {
                log.error("Error processing document {}: {}", templateFileName, e.getMessage());
            }
        }

        if (generatedFilePaths.isEmpty()) {
            log.error("No documents generated for Dac ID: {}, Version: {}", dacId, dacVersion);
            throw new RuntimeException("No documents generated for Dac ID: " + dacId + ", Version: " + dacVersion);
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

    private List<DynaDocs> getActiveDynaDocsForDac(Integer categoryId) {
        return dynaDocsRepository.findAllActifForCategorie(categoryId)
                .stream()
                .sorted(Comparator.comparing(DynaDocs::getDynaDocOrder))
                .collect(Collectors.toList());
    }

    private Map<String, String> createBookmarkMap(Integer dacId, Integer dacVersion, Integer dynaDocsId, Integer dynaDocsVer) {
        Map<String, String> bookmarks = new HashMap<>();

        // Retrieve DacDocs entries for the given Dac
        List<DacDocs> dacDocsList = dacDocsRepository.findByDacIdAndDacVer(dacId, dacVersion);
        log.info("Found {} DacDocs entries for Dac ID: {}, Version: {}", dacDocsList.size(), dacId, dacVersion);

        // Process DacDocs entries, filtering by document and sorting by field order
        List<DacDocs> filteredDacDocs = dacDocsList.stream()
                .filter(dacDoc ->
                     dacDoc.getId().getDynaDocsId().equals(dynaDocsId)&&
                            dacDoc.getId().getDynaDocsVer().equals(dynaDocsVer)

                )
                .sorted((d1, d2) -> {
                    Integer order1 = d1.getDynaField().getDynaFieldsOrder();
                    Integer order2 = d2.getDynaField().getDynaFieldsOrder();
                    return order1.compareTo(order2);
                })
                .toList();
        log.info("************************************** {} ", filteredDacDocs);
        dacDocsList.stream()
                .filter(dacDoc -> {
                    DynaFields field = dacDoc.getDynaField();
                    return field != null &&
                            field.getDynaDocs().getDynaDocsId().equals(dynaDocsId) &&
                            field.getDynaDocs().getDynaDocsVer().equals(dynaDocsVer);
                })
                .sorted((d1, d2) -> {
                    Integer order1 = d1.getDynaField().getDynaFieldsOrder();
                    Integer order2 = d2.getDynaField().getDynaFieldsOrder();
                    return order1.compareTo(order2);
                })
                .forEach(dacDoc -> {
                    String bookmarkName = dacDoc.getDynaField().getName();
                    String fieldValue = dacDoc.getFieldValue();
                    if (bookmarkName != null && fieldValue != null) {
                        log.info("Mapping bookmark: {} -> Value: {}", bookmarkName, fieldValue);
                        bookmarks.put(bookmarkName, fieldValue);
                    }
                });

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

    private String saveTempDocument(XWPFDocument document, String templateFileName, String customDirPath) {
        File customDir = new File(customDirPath);
        if (!customDir.exists()) {
            customDir.mkdirs();
        }

        String outputFileName = templateFileName.replace(".docx", "_" + System.currentTimeMillis() + ".docx");
        File tempFile = new File(customDir, outputFileName);

        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            document.write(fos);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save document: " + e.getMessage());
        }

        return tempFile.getAbsolutePath();
    }
}
