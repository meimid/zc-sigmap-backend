package com.sigmap.common.dac;

import com.sigmap.common.ModeDePassationSelection.ModeDePassationSelection;
import com.sigmap.common.ModeDePassationSelection.ModeDePassationSelectionRepository;
import com.sigmap.common.dac.exception.NoActiveAAOException;
import com.sigmap.common.ModeDePassationSelection.exception.ModeDePassationSelectionNotFound;
import com.sigmap.common.config.Constants;
import com.sigmap.common.dac.exeption.DacNotFound;
import com.sigmap.common.dac.exeption.DacWithRealisation;
import com.sigmap.common.dac.request.CreateDacRequest;
import com.sigmap.common.dac_docs.DacDocs;
import com.sigmap.common.dac_docs.DacDocsId;
import com.sigmap.common.dac_docs.DacDocsRepository;
import com.sigmap.common.dac_docs.mappers.DacDocsMapper;
import com.sigmap.common.dac_docs.response.customResponceDTO.DacResponseDTO;
import com.sigmap.common.dac_docs.response.customResponceDTO.DynaDocsResponseDTO;
import com.sigmap.common.dac_prix.DacPrix;
import com.sigmap.common.dac_prix.DacPrixRepository;
import com.sigmap.common.dac_prix.response.DacPrixResponce;
import com.sigmap.common.dyna_docs.DynaDocs;
import com.sigmap.common.dyna_docs.DynaDocsRepository;
import com.sigmap.common.dyna_docs.mappers.DynaDocsMapper;
import com.sigmap.common.dyna_docs_model.DynaDocsModel;
import com.sigmap.common.dyna_docs_model.DynaDocsModelRepository;
import com.sigmap.common.dyna_fields.DynaFieldType;
import com.sigmap.common.dyna_fields.DynaFields;
import com.sigmap.common.dyna_fields.DynaFieldsRepository;
import com.sigmap.common.dyna_fields.mappers.DynaFieldsMapper;
import com.sigmap.common.dyna_fields.response.DynaFieldsDTO;
import com.sigmap.common.dyna_fields_details.DynaFieldsDetails;
import com.sigmap.common.dyna_fields_details.DynaFieldsDetailsRepository;
import com.sigmap.common.entity.ActiveStatus;
import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.entity.ProcessStatus;
import com.sigmap.common.financement.Financement;
import com.sigmap.common.lots.Lot;
import com.sigmap.common.ppms.Ppm;
import com.sigmap.common.ppms.PpmRepository;
import com.sigmap.common.ppms.exception.PpmIdNotFound;
import com.sigmap.common.realisations.exceptions.RealisationNotFoundException;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.RefParamRepository;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import com.sigmap.common.realisations.Realisation;
import com.sigmap.common.realisations.RealisationRepository;
import com.sigmap.common.utils.SequenceGeneratorService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.xmlbeans.XmlCursor;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.io.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import com.sigmap.common.dac_docs_details.DacDocsDetails;
import com.sigmap.common.dac_docs_details.DacDocsDetailsRepository;
import com.sigmap.common.dac_docs_details.DacDocsDetailsSpecification;
import com.sigmap.common.docs.Docs;
import com.sigmap.common.docs.Mapper.DocMapper;
import com.sigmap.common.AlfrescoConf.AlfrescoService;
import com.sigmap.common.AlfrescoConf.DocumentResponseDTO;
import com.sigmap.common.keycloakAdmin.service.CurrentUserService;

@Slf4j
@Service
@AllArgsConstructor
public class DacServiceImpl implements DacService {

    private final SequenceGeneratorService sequenceGeneratorService;
    private final DacDocsRepository dacDocsRepository;
    private final DynaDocsRepository dynaDocsRepository;
    private final DacRepository dacRepository;
    private final DynaFieldsRepository dynaFieldsRepository;
    private final DynaFieldsDetailsRepository dynaFieldsDetailsRepository;
    private final PpmRepository ppmRepository;
    private final RefParamRepository refParamRepository;
    private final RealisationRepository realisationRepository;
    private final DynaDocsMapper dynaDocsMapper;
    private final DynaFieldsMapper dynaFieldsMapper;
    private final DacDocsMapper dacDocsMapper;
    private final DacDocsDetailsRepository dacDocsDetailsRepository;
    private final AlfrescoService alfrescoService;
    private final ModeDePassationSelectionRepository modeDePassationSelectionRepository;
    private final DacPrixRepository dacPrixRepository;
    private final DynaDocsModelRepository dynaDocsModelRepository;
    private final CurrentUserService currentUserService;
    @Override
    public List<String> generateListDocumentsWithBookmarks(Integer dacId, Integer dacVersion, String basePath, String customDirPath) {

        log.debug("Processing Dac ID: {}, Version: {}, Base Path: {}, Output Path: {}", dacId, dacVersion, basePath, customDirPath);

        // Step 1: Validate parameters
        validateParams(dacId, dacVersion, basePath, customDirPath);

        // Step 1: Retrieve Dac by ID and version
        DacId dacIdObj = new DacId(dacId, dacVersion);
        Dac dac = dacRepository.findById(dacIdObj)
                .orElseThrow(() -> new IllegalArgumentException("Dac not found for ID: " + dacId + ", Version: " + dacVersion));
        log.debug("Retrieved Dac: {}", dac);

        // Check if Dac has a Ppm
        if (dac.getPpm() == null) {
            log.error("Dac must have a Ppm");
            throw new IllegalArgumentException("Dac must have a Ppm");
        }
        log.debug("Dac Ppm: {}", dac.getPpm());

        // Step 2: Determine the exact snapshot of DynaDocs (id + version) used for this DAC version from dac_docs
        List<DacDocs> dacDocsSnapshot = dacDocsRepository.findByDacIdAndDacVer(dacId, dacVersion);
        if (dacDocsSnapshot.isEmpty()) {
            log.warn("No dac_docs data found for Dac ID: {}, Version: {}", dacId, dacVersion);
            throw new RuntimeException("No dac_docs data found for Dac ID: " + dacId + ", Version: " + dacVersion);
        }

        // Group by (dynaDocsId, dynaDocsVer) and compute the order using the related DynaDocs.dynaDocOrder
        Map<String, Integer> docKeyToOrder = new HashMap<>();
        LinkedHashMap<String, int[]> docKeyPairs = new LinkedHashMap<>();
        for (DacDocs doc : dacDocsSnapshot) {
            Integer ddId = doc.getId().getDynaDocsId();
            Integer ddVer = doc.getId().getDynaDocsVer();
            String key = ddId + ":" + ddVer;
            if (!docKeyPairs.containsKey(key)) {
                docKeyPairs.put(key, new int[]{ddId, ddVer});
                // Use the relation via field -> dynaDocs to get order when available
                Integer order = null;
                try {
                    if (doc.getDynaField() != null && doc.getDynaField().getDynaDocs() != null) {
                        order = doc.getDynaField().getDynaDocs().getDynaDocOrder();
                    }
                } catch (Exception ignore) {}
                // Fallback to a large number to avoid NPEs; ordering will still be stable
                docKeyToOrder.put(key, order != null ? order : Integer.MAX_VALUE);
            }
        }

        // Load the exact DynaDocs versions and sort by their intended order
        List<DynaDocs> dynaDocsList = docKeyPairs.values().stream()
                .map(pair -> dynaDocsRepository
                        .findByIdDynaDocsIdAndDynaDocsVer(pair[0], pair[1])
                        .orElseThrow(() -> new IllegalArgumentException("DynaDocs not found for ID: " + pair[0] + ", Version: " + pair[1])))
                .sorted(java.util.Comparator.comparing(dd -> docKeyToOrder.get(dd.getId() + ":" + dd.getVersion())))
                .toList();

        List<String> generatedFilePaths = new ArrayList<>();

        int docOreder = 0;
        // Step 3: Process each snapshot document in order
        for (DynaDocs dynaDoc : dynaDocsList) {
            docOreder = docOreder + 1;
            // If static doc: fetch linked Docs and integrate as-is (pdf/image)
            if (Boolean.TRUE.equals(dynaDoc.getIsStatic())) {
                try {
                    Optional<DacDocs> linked = dacDocsSnapshot.stream()
                            .filter(dd -> Objects.equals(dd.getId().getDynaDocsId(), dynaDoc.getId())
                                    && Objects.equals(dd.getId().getDynaDocsVer(), dynaDoc.getVersion())
                                    && dd.getDocs() != null)
                            .findFirst();
                    if (linked.isPresent()) {
                        Docs staticDoc = linked.get().getDocs();
                        if (staticDoc.getAlfresco_id() != null && !staticDoc.getAlfresco_id().isBlank()) {
                            DocumentResponseDTO content = alfrescoService.getDocumentById(staticDoc.getAlfresco_id());
                            if (content != null && !Boolean.TRUE.equals(content
                                    .isErreur()) && content.getFile() != null) {
                                String ext = safeExtension(staticDoc.getFileExtension());
                                String baseName = safeFileName(dynaDoc.getName() != null ? dynaDoc.getName() : "static_doc");
                                String outputFilePath = saveBinaryDocument(content.getFile(), baseName + "_" + docOreder + (ext.isEmpty() ? "" : "." + ext), customDirPath);
                                generatedFilePaths.add(outputFilePath);
                                continue; // next doc
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("Failed to integrate static doc for DynaDocs {} v{}: {}", dynaDoc.getId(), dynaDoc.getVersion(), e.getMessage());
                    // fallback: skip static doc on error
                }
            }

            String templateFileName = dynaDoc.getTemplateFileName();
            log.debug("Processing DynaDocs ID: {}, Version: {}, Template: {}",
                    dynaDoc.getId(), dynaDoc.getVersion(), templateFileName);

            File templateFile = new File(basePath, templateFileName);
            if (!templateFile.exists()) {
                log.error("Template file not found: {}", templateFile.getAbsolutePath());
                continue;
            }

            try (FileInputStream fis = new FileInputStream(templateFile);
                 XWPFDocument document = new XWPFDocument(fis)) {

                // Create bookmark map with values from DacDocs, respecting field order and using the snapshot version
                Map<String, String> bookmarks = createBookmarkMap(dacId, dacVersion, dynaDoc.getId(), dynaDoc.getVersion());

                // Handle multi-value fields (table and list bookmarks) using the snapshot version
                processTableAndListBookmarks(document, dacId, dacVersion, dynaDoc.getId(), dynaDoc.getVersion());

                // Replace bookmarks in the document
                replaceBookmarks(document, bookmarks);

                // Save the generated document
                String outputFilePath = saveTempDocument(document, templateFileName, customDirPath, docOreder);
                generatedFilePaths.add(outputFilePath);

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

    @Override
    public String generateDocumentWithBookmarks(Integer dacId, Integer dacVersion, Integer dynaDocsId, Integer dynaDocsVer, String basePath, String customDirPath) {
        log.debug("Processing Dac ID: {}, Version: {}, DynaDocsId: {}, DynaDocsVer: {}, Base Path: {}, Output Path: {}", dacId, dacVersion, dynaDocsId, dynaDocsVer, basePath, customDirPath);
        validateParams(dacId, dacVersion, basePath, customDirPath);
        DacId dacIdObj = new DacId(dacId, dacVersion);
        Dac dac = dacRepository.findById(dacIdObj)
                .orElseThrow(() -> new IllegalArgumentException("Dac not found for ID: " + dacId + ", Version: " + dacVersion));
        if (dac.getPpm() == null) {
            log.error("Dac must have a Ppm");
            throw new IllegalArgumentException("Dac must have a Ppm");
        }
        DynaDocs dynaDoc = dynaDocsRepository.findByIdDynaDocsIdAndDynaDocsVer(dynaDocsId, dynaDocsVer)
                .orElseThrow(() -> new IllegalArgumentException("DynaDocs not found for ID: " + dynaDocsId + ", Version: " + dynaDocsVer));

        // Handle static doc directly
        if (Boolean.TRUE.equals(dynaDoc.getIsStatic())) {
            List<DacDocs> dacDocsSnapshot = dacDocsRepository.findByDacIdAndDacVer(dacId, dacVersion);
            Optional<DacDocs> linked = dacDocsSnapshot.stream()
                    .filter(dd -> Objects.equals(dd.getId().getDynaDocsId(), dynaDoc.getId())
                            && Objects.equals(dd.getId().getDynaDocsVer(), dynaDoc.getVersion())
                            && dd.getDocs() != null)
                    .findFirst();
            if (linked.isPresent()) {
                Docs staticDoc = linked.get().getDocs();
                if (staticDoc.getAlfresco_id() != null && !staticDoc.getAlfresco_id().isBlank()) {
                    DocumentResponseDTO content = alfrescoService.getDocumentById(staticDoc.getAlfresco_id());
                    if (content != null && !Boolean.TRUE.equals(content.isErreur()) && content.getFile() != null) {
                        String ext = safeExtension(staticDoc.getFileExtension());
                        String baseName = safeFileName(dynaDoc.getName() != null ? dynaDoc.getName() : "static_doc");
                        return saveBinaryDocument(content.getFile(), baseName + "_1" + (ext.isEmpty() ? "" : "." + ext), customDirPath);
                    }
                }
            }
            throw new IllegalArgumentException("Static DynaDoc has no linked document");
        }

        String templateFileName = dynaDoc.getTemplateFileName();
        File templateFile = new File(basePath, templateFileName);
        if (!templateFile.exists()) {
            log.error("Template file not found: {}", templateFile.getAbsolutePath());
            throw new IllegalArgumentException("Template file not found: " + templateFile.getAbsolutePath());
        }
        try (
                FileInputStream fis = new FileInputStream(templateFile);
                XWPFDocument document = new XWPFDocument(fis)) {
            Map<String, String> bookmarks = createBookmarkMap(dacId, dacVersion, dynaDocsId, dynaDocsVer);
            processTableAndListBookmarks(document, dacId, dacVersion, dynaDocsId, dynaDocsVer);
            replaceBookmarks(document, bookmarks);
            String outputFilePath = saveTempDocument(document, templateFileName, customDirPath, 1);
            log.debug("Generated document: {}", outputFilePath);
            return outputFilePath;
        } catch (IOException e) {
            log.error("Error processing document {}: {}", templateFileName, e.getMessage());
            throw new RuntimeException("Error processing document: " + e.getMessage());
        }
    }

    private String safeExtension(String ext) {
        if (ext == null) return "";
        String cleaned = ext.trim().replaceAll("[^A-Za-z0-9]", "").toLowerCase();
        return cleaned;
    }

    private String safeFileName(String name) {
        if (name == null) return "doc";
        return name.replaceAll("[^A-Za-z0-9-_]", "_");
    }

    private String saveBinaryDocument(byte[] content, String fileName, String customDirPath) {
        File customDir = new File(customDirPath);
        if (!customDir.exists()) {
            customDir.mkdirs();
        }
        File out = new File(customDir, fileName);
        try (FileOutputStream fos = new FileOutputStream(out)) {
            fos.write(content);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save static document: " + e.getMessage());
        }
        return out.getAbsolutePath();
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
        return dynaDocsRepository.findAllActifForTypeMarche(categoryId)
                .stream()
                .sorted(Comparator.comparing(DynaDocs::getDynaDocOrder))
                .collect(Collectors.toList());
    }

    private Map<String, String> createBookmarkMap(Integer dacId, Integer dacVersion, Integer dynaDocsId, Integer dynaDocsVer) {
        Map<String, String> bookmarks = new HashMap<>();

        // Retrieve DacDocs entries for the given Dac
        List<DacDocs> dacDocsList = dacDocsRepository.findByDacIdAndDacVer(dacId, dacVersion);
        log.debug("Found {} DacDocs entries for Dac ID: {}, Version: {}", dacDocsList.size(), dacId, dacVersion);

        // Process DacDocs entries, filtering by document and sorting by field order
        List<DacDocs> filteredDacDocs = dacDocsList.stream()
                .filter(dacDoc ->
                        dacDoc.getId().getDynaDocsId().equals(dynaDocsId) &&
                                dacDoc.getId().getDynaDocsVer().equals(dynaDocsVer)

                )
                .filter(d -> d.getDynaField() != null && !"__DEFAULT__".equals(d.getDynaField().getName()))
                .sorted((d1, d2) -> {
                    Integer order1 = d1.getDynaField().getDynaFieldsOrder();
                    Integer order2 = d2.getDynaField().getDynaFieldsOrder();
                    return order1.compareTo(order2);
                })
                .toList();
        dacDocsList.stream()
                .filter(dacDoc -> {
                    DynaFields field = dacDoc.getDynaField();
                    return field != null && !"__DEFAULT__".equals(field.getName()) &&
                            field.getDynaDocs().getId().equals(dynaDocsId) &&
                            field.getDynaDocs().getVersion().equals(dynaDocsVer);
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
                        log.debug("Mapping bookmark: {} -> Value: {}", bookmarkName, fieldValue);
                        bookmarks.put(bookmarkName, fieldValue);
                    }
                });

        return bookmarks;
    }

    /**
     * Replaces bookmarks in the document with values from the provided map.
     * If a bookmark is not found in paragraphs, attempts to write in table cells as a fallback.
     */
    private void replaceBookmarks(XWPFDocument document, Map<String, String> bookmarks) {
        // Loop through all paragraphs in the document
        document.getParagraphs().stream()
                .filter(paragraph -> paragraph.getCTP().getBookmarkStartList() != null
                        && !paragraph.getCTP().getBookmarkStartList().isEmpty())
                .forEach(paragraph -> {
                    // For each bookmark found in the paragraph
                    paragraph.getCTP().getBookmarkStartList().forEach(bookmark -> {
                        String bookmarkName = bookmark.getName();
                        // If the bookmark name is in our map
                        if (bookmarks.containsKey(bookmarkName)) {
                            // Create a run and set the text to the bookmark value
                            XWPFRun run = paragraph.createRun();
                            run.setText(bookmarks.get(bookmarkName));
                            // Insert the run before the bookmark in the paragraph
                            paragraph.getCTP().getDomNode().insertBefore(run.getCTR().getDomNode(), bookmark.getDomNode());
                        }
                    });
                });
        // Fallback: For any bookmark not found in paragraphs, try to write in table cells
        for (String bookmarkName : bookmarks.keySet()) {
            if (!isBookmarkInParagraphs(document, bookmarkName)) {
                // If not found in paragraphs, write in table cell
                writeSingleRowAtBkInTbl(document, bookmarkName, bookmarks.get(bookmarkName));
            }
        }
    }

    /**
     * Checks if a bookmark exists in any paragraph in the document.
     */
    private boolean isBookmarkInParagraphs(XWPFDocument document, String bookmarkName) {
        // Loop through all paragraphs
        for (XWPFParagraph paragraph : document.getParagraphs()) {
            // Check if the paragraph contains bookmarks
            if (paragraph.getCTP().getBookmarkStartList() != null) {
                for (CTBookmark bookmark : paragraph.getCTP().getBookmarkStartList()) {
                    // If the bookmark name matches, return true
                    if (bookmarkName.equals(bookmark.getName())) {
                        return true;
                    }
                }
            }
        }
        // Not found
        return false;
    }

    /**
     * Fallback: Write a value at a bookmark inside a table cell if not found in paragraphs.
     */
    private void writeSingleRowAtBkInTbl(XWPFDocument document, String bookmarkName, String bookmarkValue) {
        // Loop through all tables in the document
        for (XWPFTable table : document.getTables()) {
            // Loop through all rows in the table
            for (XWPFTableRow row : table.getRows()) {
                // Loop through all cells in the row
                for (XWPFTableCell cell : row.getTableCells()) {
                    // Loop through all paragraphs in the cell
                    for (XWPFParagraph paragraph : cell.getParagraphs()) {
                        // Check if the paragraph contains bookmarks
                        if (paragraph.getCTP().getBookmarkStartList() != null) {
                            for (CTBookmark bookmark : paragraph.getCTP().getBookmarkStartList()) {
                                // If the bookmark name matches
                                if (bookmarkName.equals(bookmark.getName())) {
                                    // Remove the first paragraph (optional, to clear old content)
                                    cell.removeParagraph(0);
                                    // Set the cell text to the bookmark value
                                    cell.setText(bookmarkValue);
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    private String saveTempDocument(XWPFDocument document, String templateFileName, String customDirPath,Integer docOrder) {
        File customDir = new File(customDirPath);
        if (!customDir.exists()) {
            customDir.mkdirs();
        }

        String outputFileName = templateFileName.replace(".docx", "_" + docOrder + ".docx");
        File tempFile = new File(customDir, outputFileName);

        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            document.write(fos);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save document: " + e.getMessage());
        }

        return tempFile.getAbsolutePath();
    }
    /**
     * This method processes both list and table type fields and inserts the corresponding tables into the document.
     * - For list fields: generates a table with 1 column (each value in a row)
     * - For table fields: generates a table with columns as defined in dyna_fields_details and rows from dac_docs_details
     */
    private void processTableAndListBookmarks(XWPFDocument document, Integer dacId, Integer dacVersion, Integer dynaDocsId, Integer dynaDocsVer) {

        // Fetch all DynaFields for the document template and version
        List<DynaFields> dynaFieldsList = dynaFieldsRepository.findByDynaDocsIdAndDynaDocsVer(dynaDocsId, dynaDocsVer);

        for (DynaFields dynaField : dynaFieldsList) {
            if ("__DEFAULT__".equals(dynaField.getName())) {
                continue;
            }
            DynaFieldType typeCode = dynaField.getType();

            // Get the bookmark name for this field
            String bookmarkName = dynaField.getName();

            // Fetch ONLY the columns of the requested snapshot version
            List<DynaFieldsDetails> columns = dynaFieldsDetailsRepository.findAllByDocAndField(dynaDocsId, dynaDocsVer, dynaField.getDynaFieldsId());
            if (DynaFieldType.TABLE.equals(typeCode) || DynaFieldType.LIST.equals(typeCode)) {
                // in liste case when there is only one column
                if (columns.size() < 2) {
                    // Treat as list (single column)
                    List<String> rows = dacDocsRepository.findListRowsForField(dacId, dacVersion, dynaDocsId, dynaDocsVer, dynaField.getDynaFieldsId());
                    List<String> values = new ArrayList<>();
                    if (!rows.isEmpty() && !columns.isEmpty()) {
                        values.addAll(rows.stream()
                                .filter(Objects::nonNull)
                                .map(String::trim)
                                .filter(s -> !s.isEmpty()).toList());

                    }
                    insertSingleColumnTableAtBookmark(document, bookmarkName, columns, values);

                    // in table case when there is more than one column
                } else {
                    List<Map<String, String>> rows = dacDocsRepository.findTableRowsForField(dacId, dacVersion, dynaDocsId, dynaDocsVer, dynaField.getDynaFieldsId());
                    insertMultiColumnTableAtBookmark(document, bookmarkName, columns, rows);
                }
            }
        }
    }


    /**
     * Inserts a single-column table at the location of the given bookmark, with each value in a new row.
     */
    private void insertSingleColumnTableAtBookmark(XWPFDocument document, String bookmarkName,List<DynaFieldsDetails> columns, List<String> values) {
        // Loop through all paragraphs in the document
        for (XWPFParagraph paragraph : document.getParagraphs()) {
            // Check if the paragraph contains bookmarks
            if (paragraph.getCTP().getBookmarkStartList() != null) {
                // Loop through all bookmarks in the paragraph
                for (CTBookmark bookmark : paragraph.getCTP().getBookmarkStartList()) {
                    // If the bookmark name matches
                    if (bookmarkName.equals(bookmark.getName())) {
                        bookmark.setName("");
                        // Get the XML cursor at the end of the paragraph
                        XmlCursor cursor = paragraph.getCTP().newCursor();
                        // Insert a new table at the cursor position
                        XWPFTable table = document.insertNewTbl(cursor);
                        if (table == null) {
                            throw new RuntimeException("Failed to insert table: table is null. Check cursor position.");
                        }
                        // Create the header row and set the column names
                        // For each value, create a new row and set the cell text
                        for (String value : values) {
                            XWPFTableRow row = table.createRow();
                            row.getCell(0).setText(value);
                        }
                        table.removeRow(0);
                        return;
                    }
                }
            }
        }
    }

    /**
     * Inserts a multi-column table at the location of the given bookmark, with columns and rows as provided.
     */
    private void insertMultiColumnTableAtBookmark(XWPFDocument document, String bookmarkName, List<DynaFieldsDetails> columns, List<Map<String, String>> rows) {
        // Loop through all paragraphs in the document
        for (XWPFParagraph paragraph : document.getParagraphs()) {
            // Check if the paragraph contains bookmarks
            if (paragraph.getCTP().getBookmarkStartList() != null) {
                // Loop through all bookmarks in the paragraph
                for (CTBookmark bookmark : paragraph.getCTP().getBookmarkStartList()) {
                    log.debug("Loop through all bookmarks in the paragraph {} ", bookmark.getName());
                    // If the bookmark name matches
                    if (bookmarkName.equals(bookmark.getName())) {
                        log.debug("in condition of bookmark name matches  {} ", bookmark.getName());
                        bookmark.setName("");
                        // Get the XML cursor at the end of the paragraph
                        XmlCursor cursor = paragraph.getCTP().newCursor();
                        // Insert a new table at the cursor position (should now be valid)
                        XWPFTable table = document.insertNewTbl(cursor);
                        if (table == null) {
                            throw new RuntimeException("Failed to insert table: table is null. Check cursor position.");
                        }
                        // Create the header row and set the column names
                        XWPFTableRow headerRow = table.createRow();
                        // Sort columns by order ascending before creating the header
                        columns = columns.stream().sorted(Comparator.comparing(DynaFieldsDetails::getColumnPos)).collect(Collectors.toList());
                        for (int i = 0; i < columns.size(); i++) {

                            XWPFTableCell cell;
                            if (i == 0) {
                                // The first cell is already created by createRow()
                                cell = headerRow.getCell(0);
                            } else {
                                cell = headerRow.createCell();
                            }
                            cell.setText(columns.get(i).getColumnName());
                        }

                        // For each row of data, create a new table row and set cell values
                        for (Map<String, String> rowData : rows) {
                            XWPFTableRow row = table.createRow();
                            for (int i = 0; i < columns.size(); i++) {
                                XWPFTableCell cell;
                                if (i == 0) {
                                    cell = row.getCell(0);
                                } else {
                                    cell = row.createCell();
                                }
                                cell.setText(rowData.getOrDefault(columns.get(i).getColumnName(), ""));
                            }
                        }
                        table.removeRow(0);
                        return;
                    }
                }
            }
        }
    }


    /**
     * Merges a list of PDF input streams into a single PDF output stream using Apache PDFBox.
     *
     * @param inputStreams List of InputStreams of PDF files to merge.
     * @return OutputStream containing the merged PDF.
     * @throws IOException If an error occurs while merging PDFs.
     */
    public static ByteArrayOutputStream merge(List<InputStream> inputStreams) throws IOException {

        ByteArrayOutputStream mergedOutputStream = new ByteArrayOutputStream();

        PDFMergerUtility merger = new PDFMergerUtility();
        merger.setDestinationStream(mergedOutputStream);

        merger.addSources(inputStreams);

        merger.mergeDocuments(null);

        return mergedOutputStream;
    }


    @Override
    @Transactional(rollbackOn = Throwable.class)
    public Dac create(CreateDacRequest request) throws RefParamNotFound, RealisationNotFoundException, ModeDePassationSelectionNotFound, NoActiveAAOException {
        if ( request.getPpmId() == null || request.getPpmVer() == null || request.getRealisationId() == null || request.getRealisationVer() == null) {
            throw new IllegalArgumentException("All fields (ppmId, ppmVer, realisationId, realisationVer) are required");
        }
        // Fetch related Ppm
        CompositeId ppmKey = new CompositeId();
        ppmKey.setId(request.getPpmId());
        ppmKey.setVersion(request.getPpmVer());
        Ppm ppm = ppmRepository.findById(ppmKey).orElseThrow(() -> new PpmIdNotFound( request.getPpmId(), request.getPpmVer()));

        // Fetch related Realisation
        CompositeId realisationKey = new CompositeId();
        realisationKey.setId(request.getRealisationId());
        realisationKey.setVersion(request.getRealisationVer());
        

        // Enforce: a realisation can be linked to at most 2 DACs (by distinct dacId)
        Integer dacCountForReal = dacRepository.countDistinctDacByRealisation(request.getRealisationId(), request.getRealisationVer());
        log.info("Dac count for realisation: {} ------------------------ ", dacCountForReal);
        if (dacCountForReal != null && dacCountForReal >= 2) {
            throw new RuntimeException("Maximum number of DACs (2) already created for this realisation");
        }

        // Ensure if a DAC with this realisation exists -->  it must be published
        List<Dac> dacsForRealisation = dacRepository.findByRealisationIdAndVersion(request.getRealisationId(), request.getRealisationVer());
        //get the last DAC by its id and max version
        Optional<Dac> lastDacVersion = dacsForRealisation.stream().max(Comparator.comparing(Dac::getDacId).thenComparing(Dac::getDacVer));
        if (lastDacVersion.isPresent()) {
            Dac firstDac = lastDacVersion.get();
            if (firstDac.getStatus() != ProcessStatus.PUBLIE) {
                throw new DacWithRealisation(request.getRealisationId() , request.getRealisationVer());
            }
        }

        Realisation realisation = realisationRepository.findById(realisationKey).orElseThrow(() -> new RealisationNotFoundException( request.getRealisationId(), request.getRealisationVer()));

        // get type marche from realisation
        Integer typeMarcheId = realisation.getTypeMarcher().getId();
        Integer modePassationId = realisation.getModePassation() != null ? realisation.getModePassation().getId() : null;
        // Fetch related RefParam (category)
        RefParam typeMarche = refParamRepository.findById(typeMarcheId).orElseThrow(() -> new RefParamNotFound("",List.of(typeMarcheId)));
        if (typeMarche == null) {
            throw new IllegalArgumentException("Type marche not found");
        }
        ModeDePassationSelection modePassation;
        if (modePassationId != null) {
            modePassation = modeDePassationSelectionRepository.findById(modePassationId)
                    .orElseThrow(()-> new ModeDePassationSelectionNotFound("",modePassationId));
        } else {
            modePassation = null;
        }

        // AAO validation: ensure at least one active AAO DynaDocs exists for this typeMarche and modePassation
        validateActiveAAOExists(typeMarcheId, modePassationId);
        // Generate DAC reference
        String nom_ac = ppm.getAc().getNom();
        Integer sequenceNumber = dacRepository.findMaxDacNumber() + 1;

        // get DynaDocModel by dynaDocsModelId from request
        Optional <DynaDocsModel> dynaDocModel = dynaDocsModelRepository.findById(request.getDynaDocsModelId());
        if(dynaDocModel.isEmpty()) {
            throw new IllegalArgumentException("Dyna Doc Model is required");
        }

        // Create DAC
        Dac dac = new Dac();
        dac.setDacId(sequenceGeneratorService.getNextId(Constants.DAC_ID_SEQ));
        dac.setDacVer(1);
        dac.setReference(generateDacRef(sequenceNumber, nom_ac));
        dac.setPpm(ppm);
        dac.setRealisation(realisation);
        dac.setTypeMarche(typeMarche);
        dac.setModePassation( modePassation);
        dac.setStatus(ProcessStatus.BROUILLON);
        dac.setParentDacId(request.getParentDacId());
        dac.setShortListing(request.isShortListing());
        dynaDocModel.ifPresent(dac::setDynaDocsModel);
        Dac savedCdac = dacRepository.save(dac);

        // Seed dac_docs snapshot for version 1 with latest DynaDocs of this typeMarche
       // List<DynaDocs> latestDocs = dynaDocsRepository.findAllLastVersionByTypeMarcheIdAndModePassationId(typeMarche.getId(), modePassation.getId(), dac.getDynaDocsModel().getDynaDocsModelId());

        // get all dyna docs with last version

        List<DynaDocs> latestDocsFiltred =  getAllDynaDocsWithTypeMarchRealisationModelIds(typeMarche.getId(), modePassation.getId(), dac.getDynaDocsModel().getDynaDocsModelId());
        for (DynaDocs dd : latestDocsFiltred) {
            List<DynaFields> fields = dynaFieldsRepository.findByDynaDocsIdAndDynaDocsVer(dd.getId(), dd.getVersion());
            if (fields.isEmpty()) {
                // Create a placeholder DEFAULT field so dac_docs rows can be created without null dyna_field_id
                Integer maxId = dynaFieldsRepository.findMaxFieldIdForDoc(dd.getId(), dd.getVersion());
                DynaFields placeholder = new DynaFields();
                placeholder.setDynaFieldsId(maxId + 1);
                placeholder.setDynaDocsId(dd.getId());
                placeholder.setDynaDocsVer(dd.getVersion());
                placeholder.setName("__DEFAULT__");
                placeholder.setRef("__DEFAULT__");
                placeholder.setLabel("__DEFAULT__");
                placeholder.setType(DynaFieldType.SYSTEM);
                placeholder.setSource(null);
                placeholder.setSourceField(null);
                placeholder.setDynaFieldsOrder(Integer.MAX_VALUE);
                placeholder.setStatus(ActiveStatus.ACTIF);
                placeholder = dynaFieldsRepository.save(placeholder);
                fields = List.of(placeholder);
            }
            for (DynaFields f : fields) {
                DacDocsId id = new DacDocsId();
                id.setDacId(savedCdac.getDacId());
                id.setDacVer(savedCdac.getDacVer());
                id.setDynaDocsId(dd.getId());
                id.setDynaDocsVer(dd.getVersion());
                id.setDynaFieldsId(f.getDynaFieldsId());

                // Avoid duplicates if any pre-existing rows
                Optional<DacDocs> exists = dacDocsRepository.findDacDocsBYPKs(id.getDacId(), id.getDacVer(), id.getDynaDocsId(), id.getDynaDocsVer(), id.getDynaFieldsId());
                if (exists.isPresent()) continue;

                DacDocs row = new DacDocs();
                row.setId(id);
                row.setDac(savedCdac);
                row.setDynaField(f);
                row.setSourceId("");
                row.setFieldValue(null);
                dacDocsRepository.save(row);
            }
        }

        return savedCdac;
    }
    public String generateDacRef(Integer sequenceNumber, String nom_ac) {
        String year = String.valueOf(Year.now().getValue());
        return String.format("%s/%d/%s", year, sequenceNumber, nom_ac);
    }

    @Override
    public List <Dac> findAll() {
        List<Integer> userAcIds = currentUserService.getCurrentUserAcIds();
        Specification<Dac> spec = Specification.where(DacSpecifications.searchByDeleted(false));
        if (userAcIds != null && !userAcIds.isEmpty()) {
            spec = spec.and(DacSpecifications.searchByAcIds(userAcIds));
        }
        return dacRepository.findAll(spec);
    }

    @Override
    public Page<Dac> findAll(Specification<Dac> spec, Pageable pageable) {
        List<Integer> userAcIds = currentUserService.getCurrentUserAcIds();
        Specification<Dac> specification = Specification.where(DacSpecifications.searchByDeleted(false));
        if (spec != null) {
            specification = specification.and(spec);
        }
        if (userAcIds != null && !userAcIds.isEmpty()) {
            specification = specification.and(DacSpecifications.searchByAcIds(userAcIds));
        }
        return dacRepository.findAll(specification, pageable);
    }

    public Page<Dac> findAllLatest(Pageable pageable) {
        List<Integer> userAcIds = currentUserService.getCurrentUserAcIds();
        if (userAcIds == null || userAcIds.isEmpty()) {
            return Page.empty(pageable);
        }
        return dacRepository.findLatestPerDacByAcIds(userAcIds, pageable);
    }

    @Override
    public Optional<Dac> findOne(Specification<Dac> specification) {
        return dacRepository.findOne(specification);
    }

    @Override
    public Optional<Dac> findById(Integer dacId, Integer dacVer) {
        Specification<Dac> specification = DacSpecifications.id(dacId, dacVer)
                .and(DacSpecifications.searchByDeleted(false));
        return findOne(specification);
    }

    @Override
    public Dac save(Dac dac) {
        return dacRepository.save(dac);
    }



    // MAPPER 1 : UN SEUL DacPrix → DacPrixResponce
    public DacPrixResponce mapDacPrixToDacPrixResponce(DacPrix dacPrix) {
        if (dacPrix == null) {
            return null;
        }
        DacPrixResponce response = new DacPrixResponce();
        response.setDacId(dacPrix.getId().getDac().getDacId());
        response.setDacVer(dacPrix.getId().getDac().getDacVer());
        response.setMonnaie(dacPrix.getId().getMonnaie());
        response.setPrix(dacPrix.getPrix());
        return response;
    }

    // MAPPER 2 : LISTE → LISTE (APPELLE LE PREMIER)
    public List<DacPrixResponce> mapDacPrixToDacPrixResponceList(List<DacPrix> dacPrixList) {
        if (dacPrixList == null || dacPrixList.isEmpty()) {
            return new ArrayList<>();
        }
        return dacPrixList.stream()
                .map(this::mapDacPrixToDacPrixResponce) // RÉUTILISE LE 1ER MAPPER
                .collect(Collectors.toList());
    }



    @Override
    @Transactional
    public DacResponseDTO findByDacIdAndVer(Integer dacId, Integer dacVer) {
        Optional<Dac> dacOpt = dacRepository.findById(new DacId(dacId, dacVer));
        if (dacOpt.isEmpty()) {
            throw new RuntimeException("Dac not found with id: " + dacId + " and version: " + dacVer);
        }
        Dac dac = dacOpt.get();

        // Build the snapshot of DynaDocs (id + version) used when this DAC version was created
        List<DacDocs> dacDocsSnapshot = dacDocsRepository.findByDacIdAndDacVer(dacId, dacVer);

        boolean noSnapshot = dacDocsSnapshot.isEmpty();

        // Distinct (dynaDocsId, dynaDocsVer) keys preserving relative order by dynaDocOrder when resolvable
        java.util.LinkedHashMap<String, int[]> docPairs = new java.util.LinkedHashMap<>();
        java.util.Map<String, Integer> orderByKey = new java.util.HashMap<>();
        if (noSnapshot) {
            // New DAC without snapshot: return latest docs for its typeMarche, modePassation and modelId (do not persist)
            Integer typeMarcheIdForDac = dac.getTypeMarche().getId();
            Integer modePassationIdForDac = dac.getModePassation() != null ? dac.getModePassation().getId() : null;
            Integer modelId = dac.getDynaDocsModel().getDynaDocsModelId() != null ? dac.getDynaDocsModel().getDynaDocsModelId() : null;
            if(typeMarcheIdForDac == null || modePassationIdForDac == null || modelId == null) {
                throw new RuntimeException("return latest docs for its typeMarche, modePassation and modelId");
            }
           // List<DynaDocs> latestByCategoryAndModePassationAndModel =  dynaDocsRepository.findAllLastVersionByTypeMarcheIdAndModePassationId(typeMarcheIdForDac, modePassationIdForDac,modelId);

            List<DynaDocs> latestDocsFiltred =  getAllDynaDocsWithTypeMarchRealisationModelIds(typeMarcheIdForDac, modePassationIdForDac,modelId);
            for (DynaDocs dd : latestDocsFiltred) {
                String key = dd.getId() + ":" + dd.getVersion();
                if (!docPairs.containsKey(key)) {
                    docPairs.put(key, new int[]{dd.getId(), dd.getVersion()});
                    orderByKey.put(key, dd.getDynaDocOrder() != null ? dd.getDynaDocOrder() : Integer.MAX_VALUE);
                }
            }
        } else {
            for (DacDocs dd : dacDocsSnapshot) {
                int id = dd.getId().getDynaDocsId();
                int ver = dd.getId().getDynaDocsVer();
                String key = id + ":" + ver;
                if (!docPairs.containsKey(key)) {
                    docPairs.put(key, new int[]{id, ver});
                    Integer ord = null;
                    try {
                        if (dd.getDynaField() != null && dd.getDynaField().getDynaDocs() != null) {
                            ord = dd.getDynaField().getDynaDocs().getDynaDocOrder();
                        }
                    } catch (Exception ignore) {}
                    orderByKey.put(key, ord != null ? ord : Integer.MAX_VALUE);
                }
            }
        }

        DacResponseDTO dacResponse = dacDocsMapper.toDacResponse(dac);

        List<DacPrix> dacPrixList = dacPrixRepository.findByDac(dacId, dacVer);
        if (dacPrixList != null && !dacPrixList.isEmpty()) {
            List<DacPrixResponce> dacPrixResponces = mapDacPrixToDacPrixResponceList(dacPrixList);
            dacResponse.setDacPrix(dacPrixResponces);
        }
        else {
            dacResponse.setDacPrix(new ArrayList<>());
        }

        List<DynaDocsResponseDTO> dynaDocsResponses = docPairs.values().stream()
                .map(pair -> dynaDocsRepository
                        .findByIdDynaDocsIdAndDynaDocsVer(pair[0], pair[1])
                        .orElse(null))
                .filter(java.util.Objects::nonNull)
                .sorted(java.util.Comparator.comparing(dd -> orderByKey.get(dd.getId() + ":" + dd.getVersion())))
                .map(dynaDocs -> {
                    DynaDocsResponseDTO dynaDocsDTO = dynaDocsMapper.toDynaDocsResponse(dynaDocs);
                    List<DynaFields> dynaFieldsList = dynaFieldsRepository.findByDynaDocsIdAndVer(dynaDocs.getId(), dynaDocs.getVersion());
                    List<DynaFieldsDTO> dynaFieldsDTOs = dynaFieldsList.stream()
                            .filter(field -> !"__DEFAULT__".equals(field.getName()))
                            .map(field -> {
                                DynaFieldsDTO fieldDTO = dynaFieldsMapper.toDynaFieldsDTO(field);
                                fieldDTO.setDynaFieldsDetails(dacDocsMapper.dynaFieldsDetailsToDTO(field.getDynaFieldsDetails()));

                                if (!noSnapshot && field.getType() == DynaFieldType.SYSTEM) {
                                    // Resolve and upsert into dac_docs.field_value for this exact snapshot version only when snapshot exists
                                    String resolved = resolveSystemFieldValue(field, dac);
                                    upsertSystemFieldValue(dac, dynaDocs, field, resolved);
                                }

                                return fieldDTO;
                            })
                            .collect(Collectors.toList());
                    dynaDocsDTO.setDynaFields(dynaFieldsDTOs);

                    // Attach Docs of this dynaDoc if any dac_docs row has a linked Docs
                    if (!noSnapshot) {
                        Docs linked = dacDocsSnapshot.stream()
                                .filter(ddr -> Objects.equals(ddr.getId().getDynaDocsId(), dynaDocs.getId())
                                        && Objects.equals(ddr.getId().getDynaDocsVer(), dynaDocs.getVersion())
                                        && ddr.getDocs() != null)
                                .map(DacDocs::getDocs)
                                .findFirst()
                                .orElse(null);
                        if (linked != null) {
                            dynaDocsDTO.setDocs(DocMapper.INSTANCE.mapToDto(linked));
                        }
                    }

                    return dynaDocsDTO;
                })
                .collect(Collectors.toList());

        dacResponse.setDynaDocs(dynaDocsResponses);
        return dacResponse;
    }


    public static Dac cloneWithNewVersion(Dac base, int nextVer) {
        Dac newDac = new Dac();
        newDac.setDacId(base.getDacId());
        newDac.setDacVer(nextVer);
        newDac.setPpm(base.getPpm());
        newDac.setRealisation(base.getRealisation());
        newDac.setTypeMarche(base.getTypeMarche());
        newDac.setDateLimiteSoumission(base.getDateLimiteSoumission());
        newDac.setDateLimiteRecptionDemande(base.getDateLimiteRecptionDemande());
        newDac.setPayant(base.isPayant());
        newDac.setShortListing(base.isShortListing());
        newDac.setParentDacId(base.getParentDacId());
        newDac.setDynaDocsModel(base.getDynaDocsModel());
        // Add other fields as needed...
        return newDac;
    }

    @Override
    @Transactional
    public DacResponseDTO synchronizeToLatestDynaDocs(Integer dacId, Integer dacVer) {
        // Load base DAC
        Dac base = dacRepository.findDacByDacIdAndDacVer(dacId, dacVer)
                .orElseThrow(() -> new RuntimeException("Dac not found with id: " + dacId + " and version: " + dacVer));

        // Compute next version
        Dac latest = dacRepository.findDacWithLatestVersionByDacId(dacId);
        int nextVer = latest.getDacVer() + 1;

        // update dac version for prixDac with nextVer for all dacPrix by dacId and dacVer
        List<DacPrix> prixDac = dacPrixRepository.findByDac(dacId, dacVer);
        for (DacPrix dacPrix : prixDac) {
            dacPrix.getId().getDac().setDacVer(nextVer);
            dacPrixRepository.save(dacPrix);
        }

        // Create new DAC version using mapper
        Dac newDac = cloneWithNewVersion(base, nextVer);

        // Set modePassation from base DAC, or from realisation if base doesn't have it
        if (base.getModePassation() != null) {
            newDac.setModePassation(base.getModePassation());
        } else if (base.getRealisation() != null && base.getRealisation().getModePassation() != null) {
            newDac.setModePassation(base.getRealisation().getModePassation());
        }

        newDac.setStatus(base.getStatus());
        newDac.setReference(base.getReference());
        newDac.setProcessInstanceId(base.getProcessInstanceId());
        newDac = dacRepository.save(newDac);

        // Load latest active DynaDocs for DAC with typeMarche, modePassation and modelId
        Integer typeMarcheIdForNew = newDac.getTypeMarche().getId();
        Integer modePassationIdForNew = newDac.getModePassation() != null ? newDac.getModePassation().getId() : null;
        Integer modelIdForNew = newDac.getDynaDocsModel().getDynaDocsModelId();
        if(typeMarcheIdForNew==null || modePassationIdForNew==null || modelIdForNew==null){
            throw new RuntimeException("could not load latest active DynaDocs for DAC with typeMarche, modePassation and modelId ");
        }
        //List<DynaDocs> latestDocs = dynaDocsRepository.findAllLastVersionByTypeMarcheIdAndModePassationId(typeMarcheIdForNew, modePassationIdForNew,modelIdForNew);

        // get all dyna docs with last version
        List<DynaDocs> latestDocs =  getAllDynaDocsWithTypeMarchRealisationModelIds(typeMarcheIdForNew, modePassationIdForNew,modelIdForNew);

        // Index prior values by dynaFieldsId for quick mapping
        List<DacDocs> priorDocs = dacDocsRepository.findByDacIdAndDacVer(dacId, dacVer);
        Map<Integer, DacDocs> priorByFieldId = priorDocs.stream()
                .collect(Collectors.toMap(d -> d.getId().getDynaFieldsId(), d -> d, (a,b)->a));
        Set<Integer> priorDocIds = priorDocs.stream()
                .map(d -> d.getId().getDynaDocsId())
                .collect(Collectors.toSet());

        // For each latest doc and its fields, create dac_docs rows for new DAC version, copying value if field id matches
        for (DynaDocs dd : latestDocs) {
            boolean isNewDocForDac = !priorDocIds.contains(dd.getId());
            List<DynaFields> fields = dynaFieldsRepository.findByDynaDocsIdAndVer(dd.getId(), dd.getVersion());
            if (fields.isEmpty()) {
                Integer maxId = dynaFieldsRepository.findMaxFieldIdForDoc(dd.getId(), dd.getVersion());
                DynaFields placeholder = new DynaFields();
                placeholder.setDynaFieldsId(maxId + 1);
                placeholder.setDynaDocsId(dd.getId());
                placeholder.setDynaDocsVer(dd.getVersion());
                placeholder.setName("__DEFAULT__");
                placeholder.setRef("__DEFAULT__");
                placeholder.setLabel("__DEFAULT__");
                placeholder.setType(DynaFieldType.SYSTEM);
                placeholder.setSource(null);
                placeholder.setSourceField(null);
                placeholder.setDynaFieldsOrder(Integer.MAX_VALUE);
                placeholder.setStatus(ActiveStatus.ACTIF);
                placeholder = dynaFieldsRepository.save(placeholder);
                fields = List.of(placeholder);
            }
            for (DynaFields f : fields) {
                DacDocsId id = new DacDocsId();
                id.setDacId(newDac.getDacId());
                id.setDacVer(newDac.getDacVer());
                id.setDynaDocsId(dd.getId());
                id.setDynaDocsVer(dd.getVersion());
                id.setDynaFieldsId(f.getDynaFieldsId());

                String value = null;
                String sourceId = "";
                DacDocs prior = null;
                if (isNewDocForDac) {
                    value = null;
                    sourceId = "";
                } else {
                    prior = priorByFieldId.get(f.getDynaFieldsId());
                    // Fallback: if field id changed between versions, match by field name within same dynaDoc
                    if (prior == null) {
                        prior = priorDocs.stream()
                                .filter(p -> Objects.equals(p.getId().getDynaDocsId(), dd.getId()))
                                .filter(p -> p.getDynaField() != null && Objects.equals(p.getDynaField().getName(), f.getName()))
                                .findFirst()
                                .orElse(null);
                    }
                    if (prior != null) {
                        value = prior.getFieldValue();
                        log.info("priooooor {}", value);
                        sourceId = prior.getSourceId() != null ? prior.getSourceId() : "";
                    } else if (f.getType() == DynaFieldType.SYSTEM) {
                        value = resolveSystemFieldValue(f, newDac);
                    }
                }

                DacDocs row = new DacDocs();
                row.setId(id);
                row.setDac(newDac);
                row.setDynaField(f);
                row.setSourceId(sourceId);
                row.setFieldValue(value);
                // Preserve linked document if exists on prior row (carry over docId)
                if (!isNewDocForDac && prior != null && prior.getDocs() != null) {
                    row.setDocs(prior.getDocs());
                }
                dacDocsRepository.save(row);

                // Handle LIST/TABLE snapshot for dac_docs_details
                if (f.getType() == DynaFieldType.LIST || f.getType() == DynaFieldType.TABLE) {
                    // Determine target columns in latest design
                    List<DynaFieldsDetails> targetColumns = dynaFieldsDetailsRepository.findAllByDocAndField(dd.getId(), dd.getVersion(), f.getDynaFieldsId());
                    Map<Integer, DynaFieldsDetails> targetColumnsById = targetColumns.stream().collect(Collectors.toMap(DynaFieldsDetails::getDynaFieldsDetailsId, c -> c, (a,b)->a));

                    // Pull prior details rows if a prior snapshot existed for this field
                    List<DacDocsDetails> priorDetails = Collections.emptyList();
                    List<DynaFieldsDetails> priorColumns = Collections.emptyList();
                    Map<Integer, String> priorColNameById = Collections.emptyMap();
                    DacDocs priorForDetails = prior;
                    if (priorForDetails == null && !isNewDocForDac) {
                        // Fallback by field name within same dynaDoc for details as well
                        priorForDetails = priorDocs.stream()
                                .filter(p -> Objects.equals(p.getId().getDynaDocsId(), dd.getId()))
                                .filter(p -> p.getDynaField() != null && Objects.equals(p.getDynaField().getName(), f.getName()))
                                .findFirst()
                                .orElse(null);
                    }
                    if (priorForDetails != null) {
                        priorDetails = dacDocsDetailsRepository.findAll(
                                Specification.where(DacDocsDetailsSpecification.byDacId(priorForDetails.getId().getDacId()))
                                        .and(DacDocsDetailsSpecification.byDacVer(priorForDetails.getId().getDacVer()))
                                        .and(DacDocsDetailsSpecification.byDynaDocsId(priorForDetails.getId().getDynaDocsId()))
                                        .and(DacDocsDetailsSpecification.byDynaDocsVer(priorForDetails.getId().getDynaDocsVer()))
                                        .and(DacDocsDetailsSpecification.byDynaFieldsId(priorForDetails.getId().getDynaFieldsId()))
                        );
                        // Also load prior column definitions to enable name-based matching when IDs changed
                        priorColumns = dynaFieldsDetailsRepository.findAllByDocAndField(priorForDetails.getId().getDynaDocsId(), priorForDetails.getId().getDynaDocsVer(), priorForDetails.getId().getDynaFieldsId());
                        priorColNameById = priorColumns.stream().collect(Collectors.toMap(DynaFieldsDetails::getDynaFieldsDetailsId, DynaFieldsDetails::getColumnName, (a,b)->a));
                    }

                    // Group prior details by rowId
                    Map<Integer, List<DacDocsDetails>> priorByRow = priorDetails.stream().collect(Collectors.groupingBy(DacDocsDetails::getRowId));

                    // For each prior row (or none if new doc), materialize details for each target column
                    if (!priorByRow.isEmpty()) {
                        for (Map.Entry<Integer, List<DacDocsDetails>> e : priorByRow.entrySet()) {
                            Integer rowId = e.getKey();
                            Map<Integer, DacDocsDetails> priorByCol = e.getValue().stream().collect(Collectors.toMap(DacDocsDetails::getDynaFieldsDetailsId, d -> d, (a,b)->a));

                            for (Map.Entry<Integer, DynaFieldsDetails> targetEntry : targetColumnsById.entrySet()) {
                                Integer targetColId = targetEntry.getKey();
                                DynaFieldsDetails targetCol = targetEntry.getValue();

                                // Prefer exact ID match
                                DacDocsDetails old = priorByCol.get(targetColId);
                                if (old == null && !priorColNameById.isEmpty()) {
                                    // Fallback to name-based matching if IDs changed between versions
                                    String targetName = targetCol.getColumnName();
                                    Integer priorMatchedId = priorColNameById.entrySet().stream()
                                            .filter(p -> Objects.equals(p.getValue(), targetName))
                                            .map(Map.Entry::getKey)
                                            .findFirst()
                                            .orElse(null);
                                    if (priorMatchedId != null) {
                                        old = priorByCol.get(priorMatchedId);
                                    }
                                }

                                DacDocsDetails copy = new DacDocsDetails();
                                copy.setDynaFieldsDetailsId(targetColId);
                                copy.setRowId(rowId);
                                copy.setDacId(newDac.getDacId());
                                copy.setDacVer(newDac.getDacVer());
                                copy.setDynaDocsId(dd.getId());
                                copy.setDynaDocsVer(dd.getVersion());
                                copy.setDynaFieldsId(f.getDynaFieldsId());
                                copy.setSourceId(old != null ? old.getSourceId() : "");
                                copy.setFieldValue(old != null ? old.getFieldValue() : "");
                                copy.setDac(row);
                                dacDocsDetailsRepository.save(copy);
                            }
                        }
                    } else {
                        // No prior rows: do nothing (empty snapshot). Rows will be created when user inputs data.
                    }
                }
            }
        }

        // Return the assembled response for the new version using snapshot assembly
        return findByDacIdAndVer(newDac.getDacId(), newDac.getDacVer());
    }

    private List<DynaDocs> getAllDynaDocsWithTypeMarchRealisationModelIds(Integer typeMarche,Integer ModePassationId, Integer modelId) {

        // get all dyna docs with last version
        List<DynaDocs> latestDocs = dynaDocsRepository.findAllLast();

        // test parametres not null before
        if (typeMarche ==null || ModePassationId == null || modelId == null) {
            throw new IllegalArgumentException("All fields (typeMarcheId, modePassationId, dynaDocsModelId) are required");
        }
        List<DynaDocs> latestDocsFiltred = latestDocs.stream()
                .filter(d -> d.getTypeMarche().getId().equals(typeMarche)
                        && d.getModePassation().getId().equals(ModePassationId) &&
                        d.getDynaDocsModel().getDynaDocsModelId().equals(modelId))
                .toList();
        log.debug("------------{}",latestDocsFiltred);
        return latestDocsFiltred;
    }

    //Update if exists or insert a system field if it doesnt exist
    private void upsertSystemFieldValue(Dac dac, DynaDocs dynaDocs, DynaFields field, String value) {
        DacDocsId id = new DacDocsId();
        id.setDacId(dac.getDacId());
        id.setDacVer(dac.getDacVer());
        id.setDynaDocsId(dynaDocs.getId());
        id.setDynaDocsVer(dynaDocs.getVersion());
        id.setDynaFieldsId(field.getDynaFieldsId());

        // Only upsert for the exact snapshot version (avoid cross-version updates)
        Optional<DacDocs> existing = dacDocsRepository.findDacDocsBYPKs(
                id.getDacId(), id.getDacVer(), id.getDynaDocsId(), id.getDynaDocsVer(), id.getDynaFieldsId());

        if (existing.isPresent()) {
            DacDocs doc = existing.get();
            doc.setFieldValue(value);
            if (doc.getSourceId() == null) doc.setSourceId("");
            dacDocsRepository.save(doc);
        } else {
            DacDocs doc = new DacDocs();
            doc.setId(id);
            doc.setDac(dac);
            doc.setDynaField(field);
            doc.setSourceId("");
            doc.setFieldValue(value);
            dacDocsRepository.save(doc);
        }
    }

    private String resolveSystemFieldValue(DynaFields field, Dac dac) {
        String source = field.getSource();
        String sourceField = field.getSourceField();
        if (source == null || sourceField == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        DateTimeFormatter localDateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        switch (source) {
            case "ppm": {
                if (dac.getPpm() == null) return null;
                return switch (sourceField) {
                    case "nom" -> dac.getPpm().getNom();
                    case "created_at" -> {
                        if (dac.getPpm().getUpdatedAt() != null) {
                            yield sdf.format(new Date(dac.getPpm().getCreatedAt().getTimeInMillis()));
                        }
                        yield null;
                    }
                    default -> null;
                };
            }
            case "ac": {
                if (dac.getPpm() == null || dac.getPpm().getAc() == null) return null;
                return switch (sourceField) {
                    case "nom" -> dac.getPpm().getAc().getNom();
                    case "email" ->dac.getPpm().getAc().getEmail();
                    case "adresse" -> {
                        if (dac.getPpm().getAc().getAdresse() != null) {
                            yield dac.getPpm().getAc().getAdresse().getAdresse() + " " +
                                    dac.getPpm().getAc().getAdresse().getCompAdresse() + " " +
                                    dac.getPpm().getAc().getAdresse().getCodePostal() + " " +
                                    dac.getPpm().getAc().getAdresse().getVille();
                            }
                        yield null;
                    }
                    default -> null;
                };
            }
            case "realisation": {
                if (dac.getRealisation() == null) return null;
                return switch (sourceField) {
                    case "realisationEnvisagees", "realisation_envisagees" -> dac.getRealisation().getRealisationEnvisagees();
                    case "beneficiaire" -> dac.getRealisation().getBeneficiaire();
                    case "sourceFinancement", "source_financement" -> {
                        if (dac.getRealisation().getFinancements() == null || dac.getRealisation().getFinancements().isEmpty()) yield null;
                        StringBuilder sb = new StringBuilder();
                        int i = 1;
                        Set<String> seen = new java.util.LinkedHashSet<>();
                        for (Financement fin : dac.getRealisation().getFinancements()) {
                            // Skip soft-deleted financements
                            if (Boolean.TRUE.equals(fin.getIsDeleted())) continue;
                            String label = getRefParamLabel(fin.getSourceFinancement());
                            if (label == null) continue;
                            String trimmed = label.trim();
                            if (trimmed.isEmpty()) continue;
                            if (!seen.add(trimmed)) continue; // skip duplicates
                            if (sb.length() > 0) sb.append("\n");
                            sb.append(i++).append("- ").append(trimmed);
                        }
                        yield sb.isEmpty() ? null : sb.toString();
                    }

                    case "typeMarcher", "type_marcher" -> dac.getRealisation().getTypeMarcher() != null ? getRefParamLabel(dac.getRealisation().getTypeMarcher()) : null;
                    case "modeSelection", "mode_selection" -> dac.getRealisation().getModeSelection() != null ? getRefParamLabel(dac.getRealisation().getModeSelection()) : null;
                    case "modePassation", "mode_passation" -> dac.getRealisation().getModePassation() != null ? dac.getRealisation().getModePassation().getLibelle() : null;
                    case "nomLot", "nom_lot" -> {
                        if (dac.getRealisation().getLots() == null || dac.getRealisation().getLots().isEmpty()) yield null;
                        StringBuilder sb = new StringBuilder();
                        int i = 1;
                        Set<String> seen = new LinkedHashSet<>();
                        for (Lot lot : dac.getRealisation().getLots()) {
                            if (Boolean.TRUE.equals(lot.getIsDeleted())) continue;
                            String name = lot.getDescription();
                            if (name == null) continue;
                            String trimmed = name.trim();
                            if (trimmed.isEmpty()) continue;
                            if (!seen.add(trimmed)) continue;
                            if (!sb.isEmpty()) sb.append("\n");
                            sb.append(i++).append("- ").append(trimmed);
                        }
                        yield sb.isEmpty() ? null : sb.toString();
                    }
                    case "budgetEstime", "budget_estime" -> toString(dac.getRealisation().getBudgetEstime());
                   // case "date_prev_lps" -> toString(dac.getRealisation().getDatePrevLps(), dateFormatter);
                    //case "date_prev_ac" -> toString(dac.getRealisation().getDatePrevAc(), dateFormatter);
                    // case "date_prev_dp" -> toString(dac.getRealisation().getDatePrevDp(), dateFormatter);
                    //case "date_prev_ap" -> toString(dac.getRealisation().getDatePrevAp(), dateFormatter);
                    case "date_prev_lps" -> {
                        if (dac.getRealisation().getDatePrevLps() != null) {
                            yield dac.getRealisation().getDatePrevLps().format(localDateFormatter);
                        }
                        yield null;
                    }
                    case "date_prev_ac" -> {
                        if (dac.getRealisation().getDatePrevAc() != null) {
                            yield dac.getRealisation().getDatePrevAc().format(localDateFormatter);
                        }
                        yield null;
                    }
                    case "date_prev_dp" -> {
                        if (dac.getRealisation().getDatePrevDp() != null) {
                            yield dac.getRealisation().getDatePrevDp().format(localDateFormatter);
                        }
                        yield null;
                    }
                    case "date_prev_ap" -> {
                        if (dac.getRealisation().getDatePrevAp() != null) {
                            yield dac.getRealisation().getDatePrevAp().format(localDateFormatter);
                        }
                        yield null;
                    }

                    default -> null;
                };
            }
            default:
                return null;
        }
    }

    @Override
    @Transactional
    public Dac updateStatus(Integer dacId, Integer dacVer, ProcessStatus status) throws DacNotFound {
        Dac dac = dacRepository.findDacByDacIdAndDacVer(dacId,dacVer)
                .orElseThrow(() -> new DacNotFound("DAC not found", dacId, dacVer));


        dac.setStatus(status);

        dac.setUpdatedAt(Calendar.getInstance());
        return dacRepository.save(dac);
    }
    private String toString(LocalDate date, DateTimeFormatter fmt) { return date != null ? date.format(fmt) : null; }
    private String toString(BigDecimal number) { return number != null ? number.toPlainString() : null; }
    private String getRefParamLabel(RefParam ref) { return ref == null ? null : ref.getDescription(); }

    /**
     * Validates that at least one active AAO DynaDocs exists for the given typeMarche and modePassation
     */
    private void validateActiveAAOExists(Integer typeMarcheId, Integer modePassationId) {
        if (modePassationId != null && typeMarcheId != null) {
         var hasActiveAAO = dynaDocsRepository.existsActiveAAOByTypeMarcheAndModePassation(typeMarcheId, modePassationId);
            if (hasActiveAAO < 1) {
                throw new NoActiveAAOException(typeMarcheId, modePassationId);
            }
        }
    }

    @Override
    public Integer findLatestVersionById(Integer dacId) {
        return dacRepository.findMaxVersionById(dacId);
    }

    private Map<String, String> createBookmarkMapForAAO(Integer dacId, Integer dacVersion, String signataire, String fonction) {
        Map<String, String> bookmarks = new HashMap<>();

        // retrive DAC for the given DAC ID and DAC Version
        Dac dac = dacRepository.findDacByDacIdAndDacVer(dacId, dacVersion)
                .orElseThrow(() -> new DacNotFound("DAC not found", dacId, dacVersion));

        if (dac != null) {
            log.info("The Dac found with ID : ' {} ' and Version: ' {} ' is ' {} '  ", dacId, dacVersion,  dac);
        }

        if (!signataire.isEmpty() && !fonction.isEmpty()) {
            bookmarks.put("signataire", signataire);
            bookmarks.put("fonction", fonction);
        }
        return bookmarks;

    }

    @Override
    public String generateDAODocumentWithBookmarks(Integer dacId, Integer dacVersion, String signataire, String fonction, String basePath, String customDirPath) {
        log.debug("Processing Dac ID: {}, Version: {}, Base Path: {}, Output Path: {}", dacId, dacVersion, basePath, customDirPath);
        validateParams(dacId, dacVersion, basePath, customDirPath);
        DacId dacIdObj = new DacId(dacId, dacVersion);

        // retrive DAC for the given DAC ID and DAC Version
        Dac dac = dacRepository.findById(dacIdObj)
                .orElseThrow(() -> new IllegalArgumentException("Dac not found for ID: " + dacId + ", Version: " + dacVersion));

        // retrive dacdocs for the given DAC ID and DAC Version
        List<DacDocs> dacDocs = dacDocsRepository.findByDacIdAndDacVer(dacId, dacVersion);

        // fetch the AAO file from the liste of dacDocs that contain mutiple files
        DacDocs dacDocsAAO = dacDocs.stream()
                .filter(dacDoc -> dacDoc.getDynaField().getDynaDocs().getIsAAO())
                .findFirst()
                .orElse(null);

        // get the dyna doc from dynaDoc table
        if (dacDocsAAO == null) {
            log.error("No Dac Docs found for Dac ID: {}, Version: {}", dacId, dacVersion);
            throw new IllegalArgumentException("No AAO found for Dac ID: " + dacId + ", Version: " + dacVersion);
        }

        // get dynamic document AAO from Dac Docs with dac ID and Version
        DynaDocs dynaDoc = dynaDocsRepository.findByIdDynaDocsIdAndDynaDocsVer(dacDocsAAO.getDynaField().getDynaDocs().getId(), dacDocsAAO.getDynaField().getDynaDocs().getVersion()).orElse(null);

        if (dynaDoc == null) {
            log.error("No AAO document found for Dac ID: {}, Version: {}", dacId, dacVersion);
            throw new IllegalArgumentException("No AAO found for Dac ID: " + dacId + ", Version: " + dacVersion);
        }

        // retrive propreties for dyna doc
        Integer dynaDocsId = dynaDoc.getId();
        Integer dynaDocsVer = dynaDoc.getVersion();
        String templateFileName = dynaDoc.getTemplateFileName();

        // create output directory
        File templateFile = new File(basePath, templateFileName);
        if (!templateFile.exists()) {
            log.error("Template file not found: {}", templateFile.getAbsolutePath());
            throw new IllegalArgumentException("Template file not found: " + templateFile.getAbsolutePath());
        }
        try (
                FileInputStream fis = new FileInputStream(templateFile);
                XWPFDocument document = new XWPFDocument(fis)) {
            Map<String, String> bookmarks = createBookmarkMapForAAO(dacId, dacVersion, signataire, fonction);
            processTableAndListBookmarks(document, dacId, dacVersion, dynaDocsId, dynaDocsVer);
            replaceBookmarks(document, bookmarks);
            String outputFilePath = saveTempDocument(document, templateFileName, customDirPath, 1);
            log.debug("Generated document: {}", outputFilePath);
            return outputFilePath;
        } catch (IOException e) {
            log.error("Error processing document {}: {}", templateFileName, e.getMessage());
            throw new RuntimeException("Error processing document: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public List<Dac> findPublishedDacsByRealisationIdAndVersion(Integer realisationId, Integer realisationVer) {
        log.debug("Fetching published DACs for realisation ID: {} and version: {}", realisationId, realisationVer);
        return dacRepository.findPublishedDacsByRealisationIdAndVersion(realisationId, realisationVer);
    }
}
