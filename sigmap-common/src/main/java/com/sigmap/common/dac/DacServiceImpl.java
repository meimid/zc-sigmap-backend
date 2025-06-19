package com.sigmap.common.dac;

import com.sigmap.common.dac.request.CreateDacRequest;
import com.sigmap.common.dac_docs.DacDocs;
import com.sigmap.common.dac_docs.DacDocsRepository;
import com.sigmap.common.dyna_docs.DynaDocs;
import com.sigmap.common.dyna_docs.DynaDocsRepository;
import com.sigmap.common.dyna_fields.DynaFieldType;
import com.sigmap.common.dyna_fields.DynaFields;
import java.io.*;

import java.util.*;
import java.util.stream.Collectors;

import com.sigmap.common.dyna_fields.DynaFieldsRepository;
import com.sigmap.common.dyna_fields_details.DynaFieldsDetails;
import com.sigmap.common.dyna_fields_details.DynaFieldsDetailsRepository;
import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.entity.ProcessStatus;
import com.sigmap.common.ppms.Ppm;
import com.sigmap.common.ppms.PpmRepository;
import com.sigmap.common.ppms.exception.PpmIdNotFound;
import com.sigmap.common.realisations.exceptions.RealisationNotFoundException;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.RefParamRepository;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import com.sigmap.common.realisations.Realisation;
import com.sigmap.common.realisations.RealisationRepository;
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

@Slf4j
@Service
@AllArgsConstructor
public class DacServiceImpl implements DacService {


    private final DacDocsRepository dacDocsRepository;
    private final DynaDocsRepository dynaDocsRepository;
    private final DacRepository dacRepository;
    private final DynaFieldsRepository dynaFieldsRepository;
    private final DynaFieldsDetailsRepository dynaFieldsDetailsRepository;
    private final PpmRepository ppmRepository;
    private final RefParamRepository refParamRepository;
    private final RealisationRepository realisationRepository;


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

        // Step 2: Get all active documents by Dac categoryId, sorted by dynaDocOrder
        List<DynaDocs> dynaDocsList = getActiveDynaDocsForDac(dac.getCategory().getId());
        if (dynaDocsList.isEmpty()) {
            log.warn("No active documents found for Dac ID: {}, Version: {}", dacId, dacVersion);
            throw new RuntimeException("No active documents found for Dac ID: " + dacId + ", Version: " + dacVersion);
        }

        List<String> generatedFilePaths = new ArrayList<>();

        int docOreder = 0;
        // Step 3: Process each document in order
        for (DynaDocs dynaDoc : dynaDocsList) {
            docOreder = docOreder + 1;
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

                // Create bookmark map with values from DacDocs, respecting field order
                Map<String, String> bookmarks = createBookmarkMap(dacId, dacVersion, dynaDoc.getId(), dynaDoc.getVersion());

                // Handle multi-value fields (table and list bookmarks)
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
        log.debug("Found {} DacDocs entries for Dac ID: {}, Version: {}", dacDocsList.size(), dacId, dacVersion);

        // Process DacDocs entries, filtering by document and sorting by field order
        List<DacDocs> filteredDacDocs = dacDocsList.stream()
                .filter(dacDoc ->
                        dacDoc.getId().getDynaDocsId().equals(dynaDocsId) &&
                                dacDoc.getId().getDynaDocsVer().equals(dynaDocsVer)

                )
                .sorted((d1, d2) -> {
                    Integer order1 = d1.getDynaField().getDynaFieldsOrder();
                    Integer order2 = d2.getDynaField().getDynaFieldsOrder();
                    return order1.compareTo(order2);
                })
                .toList();
        dacDocsList.stream()
                .filter(dacDoc -> {
                    DynaFields field = dacDoc.getDynaField();
                    return field != null &&
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
            DynaFieldType typeCode = dynaField.getType();

            // Get the bookmark name for this field
            String bookmarkName = dynaField.getName();

            List<DynaFieldsDetails> columns = dynaFieldsDetailsRepository.findByDynaFieldsId(dynaField.getDynaFieldsId());
            if (DynaFieldType.TABLE.equals(typeCode)) {
                // in liste case when there is only one column
                if (columns.size() < 2) {
                    // Treat as list (single column)
                    List<Map<String, String>> rows = dacDocsRepository.findTableRowsForField(dacId, dacVersion, dynaDocsId, dynaDocsVer, dynaField.getDynaFieldsId());
                    List<String> values = new ArrayList<>();
                    String colName = columns.get(0).getColumnName();
                    for (Map<String, String> row : rows) {
                        values.add(row.getOrDefault(colName, ""));
                    }
                    insertSingleColumnTableAtBookmark(document, bookmarkName,columns, values);

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
    public Dac save(Dac dac) {
        return dacRepository.save(dac);
    }

    @Override
    public Dac create(CreateDacRequest request) throws RefParamNotFound, RealisationNotFoundException {
        if ( request.getPpmId() == null || request.getPpmVer() == null || request.getCategoryId() == null ) {
            throw new IllegalArgumentException("All fields (ppmId, ppmVer, categoryId) are required");
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
        Realisation realisation = realisationRepository.findById(realisationKey).orElseThrow(() -> new RealisationNotFoundException( request.getRealisationId(), request.getRealisationVer()));

        // Fetch related RefParam (category)
        RefParam category = refParamRepository.findById(request.getCategoryId()).orElseThrow(() -> new RefParamNotFound("",List.of(request.getCategoryId())));
        if (category == null) {
            throw new IllegalArgumentException("Category not found");
        }
        // Create DAC
        Dac dac = new Dac();
        dac.setDacVer(1);
        dac.setPpm(ppm);
        dac.setRealisation(realisation);
        dac.setCategory(category);
        dac.setStatus(ProcessStatus.BROUILLON);
        Dac savedCdac = dacRepository.save(dac);

        return savedCdac;
    }

    @Override
    public List <Dac> findAll() {
        Specification<Dac> spec = DacSpecifications.searchByDeleted(false);
        return dacRepository.findAll(spec);
    }

    @Override
    public Page<Dac> findAll(Specification<Dac> spec, Pageable pageable) {
        Specification<Dac> specification = Specification.where(DacSpecifications.searchByDeleted(false));
        return dacRepository.findAll(specification, pageable);
    }

}
