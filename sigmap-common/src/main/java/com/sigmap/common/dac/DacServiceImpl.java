package com.sigmap.common.dac;

import com.sigmap.common.ModeDePassationSelection.ModeDePassationSelection;
import com.sigmap.common.ModeDePassationSelection.ModeDePassationSelectionRepository;
import com.sigmap.common.com.Com;
import com.sigmap.common.com.ComService;
import com.sigmap.common.com.ComSpecifications;
import com.sigmap.common.config.ModePassationType;
import com.sigmap.common.config.ReunionStatus;
import com.sigmap.common.dac.exeption.NoActiveAAOException;
import com.sigmap.common.ModeDePassationSelection.exception.ModeDePassationSelectionNotFound;
import com.sigmap.common.config.Constants;
import com.sigmap.common.dac.exeption.DacNotFound;
import com.sigmap.common.dac.exeption.DacWithRealisation;
import com.sigmap.common.dac.request.CreateDacRequest;
import com.sigmap.common.dac.request.DacQuery;
import com.sigmap.common.dac_docs.DacDocs;
import com.sigmap.common.dac_docs.DacDocsId;
import com.sigmap.common.dac_docs.DacDocsRepository;
import com.sigmap.common.dac_docs.DacDocsService;
import com.sigmap.common.dac_docs.mappers.DacDocsMapper;
import com.sigmap.common.dac_docs.response.customResponceDTO.DacModelsDTO;
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
import com.sigmap.common.dyna_docs_model.exeptions.DynaDocsModelNotFoundExeption;
import com.sigmap.common.dyna_fields.DynaFieldType;
import com.sigmap.common.dyna_fields.DynaFields;
import com.sigmap.common.dyna_fields.DynaFieldsRepository;
import com.sigmap.common.dyna_fields.mappers.DynaFieldsMapper;
import com.sigmap.common.dyna_fields.response.DynaFieldsDTO;
import com.sigmap.common.dyna_fields_details.DynaFieldsDetails;
import com.sigmap.common.dyna_fields_details.DynaFieldsDetailsRepository;
import com.sigmap.common.entity.ActiveStatus;
import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.entity.ExtraitType;
import com.sigmap.common.entity.ProcessStatus;
import com.sigmap.common.financement.Financement;
import com.sigmap.common.keycloakAdmin.service.CurrentUserService;
import com.sigmap.common.lots.Lot;
import com.sigmap.common.security.Authentication;
import com.sigmap.common.model_dac.ModelDac;
import com.sigmap.common.ppms.Ppm;
import com.sigmap.common.ppms.PpmRepository;
import com.sigmap.common.ppms.PpmSpecifications;
import com.sigmap.common.ppms.exception.PpmIdNotFound;
import com.sigmap.common.process_verbal_reunion_extrait.ProcessVerbalReunionExtrait;
import com.sigmap.common.process_verbal_reunion_extrait.ProcessVerbalReunionExtraitService;
import com.sigmap.common.realisations.exceptions.RealisationNotFoundException;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.RefParamRepository;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import com.sigmap.common.realisations.Realisation;
import com.sigmap.common.realisations.RealisationRepository;
import com.sigmap.common.reunion.Reunion;
import com.sigmap.common.reunion.ReunionRepository;
import com.sigmap.common.reunion.ReunionSpecifications;
import com.sigmap.common.reunion.response.ExtraitResponse;
import com.sigmap.common.reunion.response.PvResponse;
import com.sigmap.common.reunion.response.ReunionArgumentDocuments;
import com.sigmap.common.societe_dac.SocieteDac;
import com.sigmap.common.societe_dac.SocieteDacID;
import com.sigmap.common.societe_dac.SocieteDacRepository;
import com.sigmap.common.utils.SequenceGeneratorService;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.springframework.transaction.annotation.Transactional;
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
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Year;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import com.sigmap.common.dac_docs_details.DacDocsDetails;
import com.sigmap.common.dac_docs_details.DacDocsDetailsRepository;
import com.sigmap.common.dac_docs_details.DacDocsDetailsSpecification;
import com.sigmap.common.docs.Docs;
import com.sigmap.common.docs.Mapper.DocMapper;
import com.sigmap.common.AlfrescoConf.AlfrescoService;
import com.sigmap.common.AlfrescoConf.DocumentResponseDTO;
import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.appel_offre.AppelOffre;
import com.sigmap.common.appel_offre.AppelOffreRepository;
import com.sigmap.common.appel_offre.exception.AppelOffreNotFound;
import com.sigmap.common.dac.exeption.InvalidDacRequestException;
import com.sigmap.common.dac.request.CreateDacFromAoRequest;
import com.sigmap.common.dac.response.DacWithAoReferenceResponse;
import com.sigmap.common.dac.response.ParticipantResponse;
import com.sigmap.common.dac_prix.DacPrixId;
import com.sigmap.common.config.ModePassationEtape;
import com.sigmap.common.monnaie.Monnaie;
import com.sigmap.common.monnaie.MonnaieRepository;
import com.sigmap.common.dac.response.DacWebsiteResponse;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
import com.sigmap.common.societe.Societe;
import com.sigmap.common.societe.SocieteRepository;

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
    private final AppelOffreRepository appelOffreRepository;
    private final SocieteDacRepository societeDacRepository;
    private final DacDocsService dacDocsService;
    private final com.sigmap.common.model_dac.ModelDacRepository modelDacRepository;
    private final ComService comService;
    private final ReunionRepository reunionRepository;
    private final ProcessVerbalReunionExtraitService processVerbalReunionExtraitService;
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
        if (dac.getAppelOffre().getPpm() == null) {
            log.error("Dac must have a Ppm");
            throw new IllegalArgumentException("Dac must have a Ppm");
        }
        log.debug("Dac Ppm: {}", dac.getAppelOffre().getPpm());

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
               /* try {
                    if (doc.getDynaField() != null && doc.getDynaField().getDynaDocs() != null) {
                        order = doc.getDynaField().getDynaDocs().getDynaDocOrder();
                    }
                } catch (Exception ignore) {
                }*/
                // Fallback to a large number to avoid NPEs; ordering will still be stable
                docKeyToOrder.put(key, order != null ? order : Integer.MAX_VALUE);
            }
        }

        // Load the exact DynaDocs versions and sort by their intended order
        List<DynaDocs> dynaDocsList = docKeyPairs.values().stream()
                .map(pair -> dynaDocsRepository
                        .findByIdDynaDocsIdAndDynaDocsVer(pair[0], pair[1])
                        .orElseThrow(() -> new IllegalArgumentException("DynaDocs not found for ID: " + pair[0] + ", Version: " + pair[1])))
                .sorted(Comparator.comparing(dd -> docKeyToOrder.get(dd.getId() + ":" + dd.getVersion())))
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
            log.debug("Processing DynaDocs ID: {}, Version: {}, Template: {}", dynaDoc.getId(), dynaDoc.getVersion(), templateFileName);

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
        if (dac.getAppelOffre().getPpm() == null) {
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

    private Map<String, String> createBookmarkMap(Integer dacId, Integer dacVersion, Integer dynaDocsId, Integer dynaDocsVer) {
        Map<String, String> bookmarks = new HashMap<>();

        // Retrieve DacDocs entries for the given Dac
        List<DacDocs> dacDocsList = dacDocsRepository.findByDacIdAndDacVer(dacId, dacVersion);
        log.debug("Found {} DacDocs entries for Dac ID: {}, Version: {}", dacDocsList.size(), dacId, dacVersion);

        // Process DacDocs entries, filtering by document and sorting by field order
        //eliminated sorting by field order with new generation methode
        List<DacDocs> filteredDacDocs = dacDocsList.stream()
                .filter(dacDoc ->
                        dacDoc.getId().getDynaDocsId().equals(dynaDocsId) &&
                                dacDoc.getId().getDynaDocsVer().equals(dynaDocsVer)

                )
                /*.filter(d -> d.getDynaField() != null && !"__DEFAULT__".equals(d.getDynaField().getName()))
                .sorted((d1, d2) -> {
                    Integer order1 = d1.getDynaField().getDynaFieldsOrder();
                    Integer order2 = d2.getDynaField().getDynaFieldsOrder();
                    return order1.compareTo(order2);
                })*/
                .toList();
        dacDocsList.stream()
                .filter(dacDoc -> {
                    DynaFields field = dacDoc.getDynaField();
                    return field != null && !"__DEFAULT__".equals(field.getName()) &&
                            field.getDynaDocs().getId().equals(dynaDocsId) &&
                            field.getDynaDocs().getVersion().equals(dynaDocsVer);
                })
                /*.sorted((d1, d2) -> {
                    Integer order1 = d1.getDynaField().getDynaFieldsOrder();
                    Integer order2 = d2.getDynaField().getDynaFieldsOrder();
                    return order1.compareTo(order2);
                })*/
                .forEach(dacDoc -> {
                    String bookmarkName = dacDoc.getDynaField().getName();
                    String fieldValue = dacDoc.getFieldValue();

                    // Resolve SYSTEM field values dynamically (e.g. for updated dates)
                    if (dacDoc.getDynaField() != null && dacDoc.getDynaField().getType() == DynaFieldType.SYSTEM) {
                         try {
                              String computed = dacDocsService.resolveSystemFieldValue(dacDoc.getDynaField(), dacDoc.getDac());
                              if (computed != null) {
                                   fieldValue = computed;
                              }
                         } catch (Exception e) {
                              log.error("Error resolving system field value for bookmark {}", bookmarkName, e);
                         }
                    }

                    // Sanitize HTML values for specific fields
                    if (dacDoc.getDynaField() != null) {
                        String source = dacDoc.getDynaField().getSource();
                        String sourceField = dacDoc.getDynaField().getSourceField();
                        
                        boolean needsSanitization = 
                            ("dac".equalsIgnoreCase(source) && ("conditionsParticipation".equalsIgnoreCase(sourceField) || "conditionParticipation".equalsIgnoreCase(sourceField))) ||
                            (("appelOffre".equalsIgnoreCase(source) || "appel_offre".equalsIgnoreCase(source) || "ao".equalsIgnoreCase(source)) && ("description".equalsIgnoreCase(sourceField) || "descriptionAO".equalsIgnoreCase(sourceField)));

                        if (needsSanitization && fieldValue != null) {
                            fieldValue = stripHtml(fieldValue);
                        }
                    }

                    if (bookmarkName != null && fieldValue != null) {
                        log.debug("Mapping bookmark: {} -> Value: {}", bookmarkName, fieldValue);
                        bookmarks.put(bookmarkName, fieldValue);
                    }
                });

        return bookmarks;
    }

    private String stripHtml(String html) {
        if (html == null) return null;
        
        // 1. Pre-process block tags to ensure separation
        // Replace closing tags of block elements or br tags with a newline
        String text = html
                .replaceAll("(?i)<br\\s*/?>", "\n")
                .replaceAll("(?i)</p>", "\n")
                .replaceAll("(?i)</div>", "\n")
                .replaceAll("(?i)</li>", "\n")
                .replaceAll("(?i)</ul>", "\n")
                .replaceAll("(?i)</ol>", "\n")
                .replaceAll("(?i)</tr>", "\n");

        // 2. Unescape HTML entities
        text = text
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("\u00A0", " "); // Unicode non-breaking space
            
        // 3. Remove remaining HTML tags
        text = text.replaceAll("\\<[^>]*>", "");
        
        // 4. Trim and collapse multiple spaces (but keep newlines)
        return text.replaceAll("[ \\t\\f]+", " ").replaceAll("[\\r\\n]+", "\n").trim();
    }

    private void replaceBookmarks(XWPFDocument document, Map<String, String> replacements) {
        // Convertir les remplacements en Map<Object> pour gérer les types complexes
        Map<String, Object> complexReplacements = new HashMap<>();
        replacements.forEach(complexReplacements::put);

        // Avant d'appeler processComplexPatterns
        log.debug("Tentative de remplacement avec les données suivantes:");
        for (Map.Entry<String, Object> entry : complexReplacements.entrySet()) {
            log.debug("Clé: {}, Type: {}, Valeur: {}",
                    entry.getKey(),
                    entry.getValue() != null ? entry.getValue().getClass().getSimpleName() : "null",
                    entry.getValue());
        }

        // Ensuite, appelez processComplexPatterns

        // Remplacer d'abord les motifs complexes (tableaux et listes)
        processComplexPatterns(document, complexReplacements);

        // Ensuite remplacer les valeurs simples
        replaceSimplePatterns(document, replacements);

        // Enfin, utiliser l'ancienne méthode en tant que fallback si nécessaire
        if (!isAnyPatternFound(document)) {
            document.getParagraphs().stream()
                    .filter(paragraph -> paragraph.getCTP().getBookmarkStartList() != null
                            && !paragraph.getCTP().getBookmarkStartList().isEmpty())
                    .forEach(paragraph -> {
                        paragraph.getCTP().getBookmarkStartList().forEach(bookmark -> {
                            String bookmarkName = bookmark.getName();
                            if (replacements.containsKey(bookmarkName)) {
                                XWPFRun run = paragraph.createRun();
                                run.setText(replacements.get(bookmarkName));
                                paragraph.getCTP().getDomNode().insertBefore(run.getCTR().getDomNode(), bookmark.getDomNode());
                            }
                        });
                    });
        }
    }

    private void processComplexPatterns(XWPFDocument document, Map<String, Object> replacements) {
        log.debug("Début du traitement des motifs complexes");

        for (Map.Entry<String, Object> entry : replacements.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            log.debug("Traitement de la clé: {}, Type: {}", key,
                    value != null ? value.getClass().getSimpleName() : "null");

            if (value instanceof List) {
                List<?> listValue = (List<?>) value;
                if (!listValue.isEmpty()) {
                    if (key.startsWith("list:") && listValue.get(0) instanceof String) {
                        // C'est une liste simple
                        String listName = key.substring(5); // Enlève le préfixe "list:"
                        log.debug("Appel de replaceListPattern pour la liste: {}", listName);
                        replaceListPattern(document, listName, (List<String>) listValue);
                    } else if (listValue.get(0) instanceof Map) {
                        // C'est un tableau
                        log.debug("Appel de replaceTablePattern avec {} lignes", listValue.size());
                        replaceTablePattern(document, key, (List<Map<String, String>>) listValue);
                    }
                }
            }
        }
    }

    private void replaceSimplePatterns(XWPFDocument document, Map<String, String> replacements) {
        // Remplacer dans les paragraphes normaux
        for (XWPFParagraph paragraph : document.getParagraphs()) {
            replacePatternsInParagraph(paragraph, replacements);
        }

        // Remplacer dans les tableaux
        for (XWPFTable table : document.getTables()) {
            // Loop through all rows in the table
            for (XWPFTableRow row : table.getRows()) {
                // Loop through all cells in the row
                for (XWPFTableCell cell : row.getTableCells()) {
                    // Loop through all paragraphs in the cell
                    for (XWPFParagraph paragraph : cell.getParagraphs()) {
                        replacePatternsInParagraph(paragraph, replacements);
                    }
                }
            }
        }
    }
   private void replaceTablePattern(XWPFDocument document, String tableName, List<Map<String, String>> tableData) {
        if (tableData == null || tableData.isEmpty()) {
            log.warn("Aucune donnée de tableau fournie pour le tableau: {}", tableName);
            return;
        }

        // Parcourir tous les paragraphes pour trouver le marqueur
        // Utiliser une copie de la liste pour éviter ConcurrentModificationException si
        // on supprime des paragraphes
        List<XWPFParagraph> paragraphs = new ArrayList<>(document.getParagraphs());
        for (XWPFParagraph p : paragraphs) {
            String text = p.getText();
            String marker = "**" + tableName + "**";

            if (text != null && text.contains(marker)) {
                log.debug("Marqueur de tableau trouvé: {}", marker);

                try {
                    // Obtenir la position du curseur au paragraphe actuel
                    XmlCursor cursor = p.getCTP().newCursor();

                    // Insérer le tableau à la position du curseur (juste avant le paragraphe)
                    // Note: insertNewTbl insère le tableau à la position du curseur
                    XWPFTable table = document.insertNewTbl(cursor);

                    // Ajouter les données
                    if (!tableData.isEmpty()) {
                        // Ajouter l'en-tête
                        // XWPFTable vient avec une ligne par défaut
                        XWPFTableRow headerRow = table.getRow(0);
                        int colIndex = 0;

                        // Utiliser les clés de la première ligne comme en-têtes
                        Map<String, String> firstRow = tableData.get(0);
                        for (String header : firstRow.keySet()) {
                            if (colIndex > 0) {
                                headerRow.createCell();
                            }
                            XWPFTableCell cell = headerRow.getCell(colIndex);
                            setCellText(cell, header, true);
                            colIndex++;
                        }

                        // Ajouter les données
                        for (int j = 1; j < tableData.size(); j++) {
                            Map<String, String> rowData = tableData.get(j);
                            XWPFTableRow dataRow = table.createRow();
                            colIndex = 0;
                            for (String value : rowData.values()) {
                                dataRow.getCell(colIndex).setText(value != null ? value : "");
                                colIndex++;
                            }
                        }
                    }

                    log.debug("Tableau '{}' inséré avec succès avec {} lignes", tableName, tableData.size());

                    // Supprimer le paragraphe contenant le marqueur car il a été remplacé
                    int pos = document.getPosOfParagraph(p);
                    if (pos != -1) {
                        document.removeBodyElement(pos);
                    }

                    return;

                } catch (Exception e) {
                    log.error("Erreur lors de l'insertion du tableau '{}': {}", tableName, e.getMessage(), e);
                }
            }
        }
        log.warn("Aucun marqueur de tableau trouvé pour: {}", "**table:" + tableName + "**");
    }


    // ADD THIS HELPER METHOD TO YOUR CLASS
   private void setCellText(XWPFTableCell cell, String text, boolean bold) {
        // Remove all extra paragraphs (keep only first)
        while (cell.getParagraphs().size() > 1) {
            cell.removeParagraph(1);
        }

        XWPFParagraph paragraph = cell.getParagraphs().get(0);
        if (paragraph == null) {
            paragraph = cell.addParagraph();
        }

        // Clear all runs
        while (!paragraph.getRuns().isEmpty()) {
            paragraph.removeRun(0);
        }

        // Create new run with desired text and style
        XWPFRun run = paragraph.createRun();
        run.setText(text != null ? text : "");
        run.setBold(bold);
    }

    /**
     * Remplace le marqueur **nomTable** par un vrai tableau Word inséré à la bonne position
     */
   private void replaceListPattern(XWPFDocument document, String listName, List<String> items) {
        if (items == null || items.isEmpty()) return;
        
        // Parcourir tous les paragraphes du document
        for (int i = 0; i < document.getParagraphs().size(); i++) {
            XWPFParagraph p = document.getParagraphs().get(i);
            String text = p.getText();

            if (text != null && text.contains("**list:" + listName + "**")) {
                log.debug("Marqueur de liste trouvé: {}", text);

                // Créer un curseur à la position du marqueur
                XmlCursor cursor = p.getCTP().newCursor();

                // Insérer chaque élément comme un nouveau paragraphe
                for (String item : items) {
                    if (item != null && !item.trim().isEmpty()) {
                        // Créer un nouveau paragraphe à la position du curseur
                        XWPFParagraph newPara = document.insertNewParagraph(cursor);
                        newPara.setIndentationLeft(720);
                        newPara.createRun().setText("- " + item.trim());
                        
                        // Déplacer le curseur après le paragraphe inséré
                        cursor = newPara.getCTP().newCursor();
                        cursor.toNextSibling();
                    }
                }

                // Supprimer le paragraphe du marqueur
                int pos = document.getPosOfParagraph(p);
                if (pos != -1) {
                    document.removeBodyElement(pos);
                }
                return;
            }
        }
   }

    /**
     * Remplace les marqueurs dans un paragraphe tout en préservant le style.
     * 
     * <p>
     * Cette méthode commence par consolider les runs adjacents identiques pour
     * gérer les cas
     * où un marqueur serait coupé (ex: "**" dans un run et "KEY**" dans le
     * suivant).
     * Ensuite, elle itère sur les runs pour effectuer les remplacements.
     * </p>
     *
     * @param paragraph    Le paragraphe à traiter
     * @param replacements La map des remplacements (Nom du marqueur -> Valeur)
     */
    private void replacePatternsInParagraph(XWPFParagraph paragraph,
                                            Map<String, String> replacements) {

        // 1. Merge split tags to ensure **KEY** is in a single run.
        // This also handles the "Partial Bold" issue by applying bold if any part was bold.
        mergeTagRuns(paragraph);
        
        // 2. Iterate through runs and perform IN-PLACE replacement.
        // This avoids the "Redistribution" logic that caused style bleeding and truncation.
        List<XWPFRun> runs = paragraph.getRuns();
        if (runs == null || runs.isEmpty()) return;

        for (XWPFRun run : runs) {
            String text = run.getText(0);
            if (text == null || !text.contains("**")) continue;

            String newText = text;
            boolean modified = false;

            for (Map.Entry<String, String> entry : replacements.entrySet()) {
                String key = entry.getKey();
                if (key.contains(":")) continue; // Skip complex keys like list: or table:

                String pattern = "**" + key + "**";
                if (newText.contains(pattern)) {
                    String value = entry.getValue() != null ? entry.getValue() : "";
                    newText = newText.replace(pattern, value);
                    modified = true;
                }
            }
            
            // Apply changes to this specific run only
            if (modified) {
                if (newText.contains("\n")) {
                    String[] lines = newText.split("\n", -1); // Keep empty lines
                    
                    // Force alignment to LEFT to prevent justification stretching with soft breaks
                    paragraph.setAlignment(ParagraphAlignment.LEFT);

                    run.setText(lines[0], 0);
                    for (int k = 1; k < lines.length; k++) {
                        run.addBreak();
                        run.setText(lines[k]);
                    }
                } else {
                    run.setText(newText, 0);
                }
            }
        }
        
        // 3. Consolidated cleanup of any residual ** if needed, 
        // though strictly speaking, we should only remove what we replaced.
        // If there are leftover **, it means they didn't match any key.
        // We can optionally leave them or remove them. 
        // The previous logic removed all **, so we maintain that behavior for consistency.
        for (XWPFRun run : runs) {
            String text = run.getText(0);
            if (text != null && text.contains("**")) {
                 run.setText(text.replace("**", ""), 0);
            }
        }
    }

    private void mergeTagRuns(XWPFParagraph paragraph) {
         if (paragraph.getRuns().size() < 2) return;
         
         // Helper to get text from runs
         StringBuilder sb = new StringBuilder();
         List<XWPFRun> runs = paragraph.getRuns();
         List<Integer> runOffsets = new ArrayList<>();
         
         for(XWPFRun r : runs) {
             runOffsets.add(sb.length());
             String t = r.getText(0);
             sb.append(t != null ? t : "");
         }
         String text = sb.toString();
         
         int searchIndex = 0;
         while(true) {
             int startIdx = text.indexOf("**", searchIndex);
             if (startIdx == -1) break;
             int endIdx = text.indexOf("**", startIdx + 2);
             if (endIdx == -1) break;
             
             // Found a tag: text.substring(startIdx, endIdx + 2)
             int tagEnd = endIdx + 2;
             
             // Update search index
             searchIndex = tagEnd;
             
             // Identify runs involved
             int startRunIdx = -1;
             int endRunIdx = -1;
             
             for(int i=0; i<runOffsets.size(); i++) {
                 int rStart = runOffsets.get(i);
                 // Heuristic: check if this run contains the start/end points
                 String rText = runs.get(i).getText(0);
                 int rLen = (rText != null) ? rText.length() : 0;
                 int rEnd = rStart + rLen;
                 
                 if (startRunIdx == -1 && startIdx >= rStart && startIdx < rEnd) {
                     startRunIdx = i;
                 }
                 if (endIdx >= rStart && endIdx < rEnd) { 
                     endRunIdx = i;
                 }
             }
             
             // If valid span found and spans multiple runs
             if (startRunIdx != -1 && endRunIdx != -1 && startRunIdx < endRunIdx) {
                 // Check if any run in between (inclusive) is Bold
                 boolean isBold = false;
                 for(int i=startRunIdx; i<=endRunIdx; i++) {
                     if (runs.get(i).isBold()) {
                         isBold = true;
                         break;
                     }
                 }
                 
                 // Merge into startRunIdx
                 XWPFRun startRun = runs.get(startRunIdx);
                 StringBuilder mergedText = new StringBuilder();
                 
                 for(int i=startRunIdx; i<=endRunIdx; i++) {
                      String t = runs.get(i).getText(0);
                      if (t!=null) mergedText.append(t);
                 }
                 
                 startRun.setText(mergedText.toString(), 0);
                 if (isBold) startRun.setBold(true);
                 
                 // Remove other runs. Iterate backwards to avoid index shift
                 for(int i=endRunIdx; i>startRunIdx; i--) {
                     paragraph.removeRun(i);
                 }
                 
                 // Since we modified runs and indices, restart scan to be safe
                 mergeTagRuns(paragraph); 
                 return;
             }
         }
    }



    /**
     * Tente de fusionner les runs adjacents qui ont des styles identiques.
     * Cela permet de reconstituer des marqueurs qui auraient été coupés par Word.
     */
    private void consolidateRuns(XWPFParagraph paragraph) {
        if (paragraph.getRuns().size() < 2)
            return;

        List<XWPFRun> runs = paragraph.getRuns();
        for (int i = 0; i < runs.size() - 1; i++) {
            XWPFRun run1 = runs.get(i);
            XWPFRun run2 = runs.get(i + 1);

            if (areRunsStyleIdentical(run1, run2)) {
                // Fusionner run2 dans run1
                String text1 = run1.getText(0) != null ? run1.getText(0) : "";
                String text2 = run2.getText(0) != null ? run2.getText(0) : "";

                run1.setText(text1 + text2, 0);

                // Supprimer run2
                paragraph.removeRun(i + 1);

                // Reculer l'index pour vérifier à nouveau ce run fusionné avec le suivant
                i--;
                // Mettre à jour la liste locale car paragraph.getRuns() a changé
                runs = paragraph.getRuns();
            }
        }
    }

    /**
     * Vérifie si deux runs ont le même style visuel (gras, italique, couleur, police, taille).
     */
    private boolean areRunsStyleIdentical(XWPFRun r1, XWPFRun r2) {
        if (r1 == null || r2 == null)
            return false;

        // Comparer les propriétés principales
        return r1.isBold() == r2.isBold() &&
                r1.isItalic() == r2.isItalic() &&
                r1.isStrikeThrough() == r2.isStrikeThrough() &&
                Objects.equals(r1.getColor(), r2.getColor()) &&
                Objects.equals(r1.getFontFamily(), r2.getFontFamily()) &&
                r1.getFontSize() == r2.getFontSize() &&
                r1.getUnderline() == r2.getUnderline();
    }

    private boolean isAnyPatternFound(XWPFDocument document) {
        // Vérifier dans les paragraphes normaux
        for (XWPFParagraph p : document.getParagraphs()) {
            if (p.getText() != null && p.getText().contains("**")) {
                return true;
            }
        }
        // Vérifier dans les tableaux
        for( XWPFTable table :document.getTables())  {
            for (XWPFTableRow row : table.getRows()) {
                for (XWPFTableCell cell : row.getTableCells()) {
                    for (XWPFParagraph p : cell.getParagraphs()) {
                        if (p.getText() != null && p.getText().contains("**")) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
}

    private String saveTempDocument(XWPFDocument document, String templateFileName, String customDirPath, Integer docOrder) {
        File customDir = new File(customDirPath);
        if (!customDir.exists()) {
            customDir.mkdirs();
        }

       // String outputFileName = templateFileName.replace(".docx",  ".docx");
        File tempFile = new File(customDir, templateFileName);

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
    /**
     * Traite les champs de type TABLE et LIST pour les insérer dans le document Word.
     * Les données sont récupérées depuis la base de données et formatées selon leur type.
     * 
     * @param document Le document Word à modifier
     * @param dacId L'identifiant du DAC
     * @param dacVersion La version du DAC
     * @param dynaDocsId L'identifiant du modèle de document
     * @param dynaDocsVer La version du modèle de document
     */
    private void processTableAndListBookmarks(XWPFDocument document, Integer dacId, Integer dacVersion, Integer dynaDocsId, Integer dynaDocsVer) {
        // Map pour stocker tous les remplacements à effectuer
        // Clé : nom du champ avec préfixe (table: ou list:)
        // Valeur : List<String> pour les listes ou List<Map> pour les tableaux
        Map<String, Object> replacements = new HashMap<>();

        // Récupération de tous les champs dynamiques pour le modèle de document
        List<DynaFields> dynaFieldsList = dynaFieldsRepository.findByDynaDocsIdAndDynaDocsVer(dynaDocsId, dynaDocsVer);

        log.debug("dynaFieldsList : " + dynaFieldsList);

        // Parcours de chaque champ dynamique
        for (DynaFields dynaField : dynaFieldsList) {
            // Ignorer le champ par défaut
            if ("__DEFAULT__".equals(dynaField.getName())) {
                continue;
            }
            
            // Récupération du type de champ (TABLE ou LIST)
            DynaFieldType typeCode = dynaField.getType();
            String fieldName = dynaField.getName();

            // Récupération des détails des colonnes pour ce champ
            List<DynaFieldsDetails> columns = dynaFieldsDetailsRepository.findAllByDocAndField(
                dynaDocsId, dynaDocsVer, dynaField.getDynaFieldsId());

            // Traitement différent selon qu'il s'agit d'une liste ou d'un tableau
            if (DynaFieldType.TABLE.equals(typeCode) || DynaFieldType.LIST.equals(typeCode)) {
                
                // CAS 1: LISTE (une seule colonne)
                if (columns.size() < 2) {
                    log.info(" column liste ......... {}",typeCode);
                    // Récupération des données de la liste depuis la base de données
                    List<String> rows = dacDocsRepository.findListRowsForField(
                        dacId, dacVersion, dynaDocsId, dynaDocsVer, dynaField.getDynaFieldsId());

                    log.info(" rows liste ......... {}",rows);
                    // Nettoyage des données : suppression des valeurs nulles, espaces superflus et lignes vides
                    List<String> values = rows.stream()
                        .filter(Objects::nonNull)           // Supprime les valeurs nulles
                        .map(String::trim)                  // Supprime les espaces au début et à la fin
                        .filter(s -> !s.isEmpty())          // Supprime les chaînes vides
                        .collect(Collectors.toList());      // Collecte les résultats dans une nouvelle liste

                    log.info(" column liste values ......... {}",values);
                    // Si on a des valeurs, on les ajoute à la map des remplacements avec le préfixe "list:"
                    if (!values.isEmpty()) {
                        replacements.put("list:" + fieldName, values);
                    }
                } 
                
                // CAS 2: TABLEAU (plusieurs colonnes)
                else {
                    // Récupération des données du tableau depuis la base de données
                    List<Map<String, String>> rows = dacDocsRepository.findTableRowsForField(
                        dacId, dacVersion, dynaDocsId, dynaDocsVer, dynaField.getDynaFieldsId());
                    
                    // Filtrage des lignes nulles ou vides
                    List<Map<String, String>> filteredRows = rows.stream()
                        .filter(row -> row != null && !row.isEmpty())
                        .collect(Collectors.toList());
                    
                    // Si on a des données valides
                    if (!filteredRows.isEmpty()) {
                        // Création d'une nouvelle liste pour stocker les lignes formatées
                        List<Map<String, String>> formattedRows = new ArrayList<>();
                        
                        // Création de la ligne d'en-tête avec les noms de colonnes
                        Map<String, String> headerRow = new LinkedHashMap<>(); // LinkedHashMap pour conserver l'ordre
                        for (DynaFieldsDetails column : columns) {
                            // Utilisation de getColumnName() pour le nom de la colonne
                            headerRow.put(column.getColumnName(), column.getColumnName());
                        }
                        formattedRows.add(headerRow); // Ajout de l'en-tête en première ligne
                        
                        // Ajout de toutes les lignes de données
                        formattedRows.addAll(filteredRows);
                        
                        // Ajout du tableau à la map des remplacements avec le préfixe "table:"
                        replacements.put("table:" + fieldName, formattedRows);
                    }
                }
            }
        }
        
        // Une fois tous les remplacements préparés, on les applique au document
        if (!replacements.isEmpty()) {
            processComplexPatterns(document, replacements);
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
        if (inputStreams == null || inputStreams.isEmpty()) {
            throw new IllegalArgumentException("Input streams list cannot be null or empty");
        }

        PDFMergerUtility merger = new PDFMergerUtility();

        // Add each InputStream wrapped in a RandomAccessReadBuffer (required for PDFBox 3.x)
        for (InputStream is : inputStreams) {
            // Fully read the InputStream into a byte array
            byte[] pdfBytes = is.readAllBytes();

            // Create a random-access readable buffer from the bytes
            RandomAccessRead buffer = new RandomAccessReadBuffer(pdfBytes);

            // Add the source
            merger.addSource(buffer);
        }

        // Set the destination to a ByteArrayOutputStream
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        merger.setDestinationStream(result);

        // Use memory-only caching (no temporary files on disk)
        merger.mergeDocuments(IOUtils.createMemoryOnlyStreamCache());

        return result;
    }

    @Override
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
        dac.setReference(generateDacRef(sequenceNumber, nom_ac,dac.getAppelOffre().getPpm()));
        dac.setStatus(ProcessStatus.BROUILLON);
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
    public String generateDacRef(Integer sequenceNumber, String nom_ac,Ppm ppm) {
        String year = ppm.getAnnee();
        return String.format("%s/%d/%s", year, sequenceNumber, nom_ac);
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

    public Page<Dac> findAllLatest(Pageable pageable) {
        return dacRepository.findLatestPerDac(pageable);
    }

    @Override
    public Page<Dac> findAllLatestWithQuery(DacQuery query, Pageable pageable) {
        boolean isAdmin = Authentication.hasRole("ADMIN_CENTRAL") || Authentication.hasRole("SUPER_ADMIN");
        if (isAdmin) {
        if (query.getAppelOffreId() != null) {
            return dacRepository.findLatestDacsByAppelOffre(query.getAppelOffreId(), pageable);
        }
        return dacRepository.findLatestPerDac(pageable);
    }

        List<Integer> userAcs = currentUserService.getCurrentUserAcIds();
        List<Integer> userComs = currentUserService.getCurrentUserComIds();

        if ((userAcs == null || userAcs.isEmpty()) && (userComs == null || userComs.isEmpty())) {
            return Page.empty(pageable);
        }

        if (query.getAppelOffreId() != null) {
            return dacRepository.findLatestDacsByAppelOffreAndUser(query.getAppelOffreId(), userComs, userAcs,
                    pageable);
        }

        return dacRepository.findLatestDacsForUser(userComs, userAcs, pageable);
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
        LinkedHashMap<String, int[]> docPairs = new LinkedHashMap<>();
        Map<String, Integer> orderByKey = new HashMap<>();
        if (noSnapshot) {
            // New DAC without snapshot: return latest docs for its typeMarche, modePassation and modelId (do not persist)
            Integer typeMarcheIdForDac = dac.getAppelOffre().getRealisation().getTypeMarcher().getId();
            Integer modePassationIdForDac = dac.getAppelOffre().getRealisation().getModePassation() != null ? dac.getAppelOffre().getRealisation().getModePassation().getId() : null;
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
        if (dac.isShortListing()) {
            List<SocieteDac> shortlists = societeDacRepository.findLatestByDacId(dacId);
            if (!shortlists.isEmpty()) {
                List<DacWithAoReferenceResponse.ShortlistMemberData> members = shortlists.stream()
                        .map(sl -> {
                            Societe s = sl.getId().getSociete();
                            return new DacWithAoReferenceResponse.ShortlistMemberData(
                                    s.getId(),
                                    s.getNom(),
                                    s.getNif()
                            );
                        })
                        .collect(Collectors.toList());
                dacResponse.setDacShortlist(members);
            }
        }
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
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(dd -> orderByKey.get(dd.getId() + ":" + dd.getVersion())))
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
                                    String resolved = dacDocsService.resolveSystemFieldValue(field, dac);
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

        // Populate dacModels
        List<ModelDac> models = modelDacRepository.findByDacIdAndDacVer(dacId, dacVer);
        DacModelsDTO modelsDTO = new DacModelsDTO();

        for (ModelDac m : models) {
             if (Boolean.TRUE.equals(m.getIsAao())) {
                 if (m.getDoc() != null) {
                     modelsDTO.setAaoModel(m.getDoc().getAlfresco_id());
                 }
             } else {
                 if (m.getDoc() != null) {
                     modelsDTO.setDacModel(m.getDoc().getAlfresco_id());
                 }
             }
        }
        dacResponse.setDacModels(Collections.singletonList(modelsDTO));
        return dacResponse;
    }


    public static Dac cloneWithNewVersion(Dac base, int nextVer) {
        Dac newDac = new Dac();
        newDac.setDacId(base.getDacId());
        newDac.setDacVer(nextVer);
        // Use AppelOffre for navigation instead of direct PPM/Realisation
        newDac.setAppelOffre(base.getAppelOffre());
        newDac.setDateLimiteSoumission(base.getDateLimiteSoumission());
        newDac.setDateLimiteRecptionDemande(base.getDateLimiteRecptionDemande());
        newDac.setPayant(base.isPayant());
        newDac.setShortListing(base.isShortListing());
        newDac.setDynaDocsModel(base.getDynaDocsModel());
        newDac.setResponsableDacId(base.getResponsableDacId());
        newDac.setConditionsParticipation(base.getConditionsParticipation());
        newDac.setEtapeDac(base.getEtapeDac());
        newDac.setStatus(base.getStatus());
        newDac.setReference(base.getReference());
        newDac.setProcessInstanceId(base.getProcessInstanceId());
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

        int dacIdInut = dacId;

        int dacVerInput = dacVer;

        int nextVer = latest.getDacVer() + 1;
        // Create new DAC version using mapper
        Dac newDac = cloneWithNewVersion(base, nextVer);

        newDac = dacRepository.save(newDac);

        // update dac version for prixDac with nextVer for all dacPrix by dacId and dacVer
        List<DacPrix> prixDac = dacPrixRepository.findByDac(dacIdInut, dacVerInput);
        for (DacPrix dacPrix : prixDac) {
            DacPrixId newId = new DacPrixId();
            newId.setDac(newDac);
            newId.setMonnaie(dacPrix.getId().getMonnaie());
            DacPrix copy = new DacPrix();
            copy.setId(newId);
            copy.setPrix(dacPrix.getPrix());
            dacPrixRepository.save(copy);
        }
        // Clone DacShortlist if shortListing is true
       if (newDac.isShortListing()) {
             List<SocieteDac> existingShortlist = societeDacRepository.findByDacIdAndDacVer(base.getDacId(), base.getDacVer());
             for (SocieteDac source : existingShortlist) {
                 SocieteDacID societeDacID = new  SocieteDacID();
                 societeDacID.setDac(newDac);
                 societeDacID.setSociete(source.getId().getSociete());
                 SocieteDac copy = new SocieteDac();
                 copy.setId(societeDacID);
                 copy.setMethodePaiement(source.getMethodePaiement());
                 copy.setStatusPaiement(source.getStatusPaiement());
                 copy.setJustifPaiementId(source.getJustifPaiementId());
                 societeDacRepository.save(copy);
             }
       }

        // Load latest active DynaDocs for DAC with typeMarche, modePassation and modelId
        Integer typeMarcheIdForNew = newDac.getAppelOffre().getRealisation().getTypeMarcher().getId();
        Integer modePassationIdForNew = newDac.getAppelOffre().getRealisation().getModePassation() != null ? newDac.getAppelOffre().getRealisation().getModePassation().getId() : null;
        Integer modelIdForNew = newDac.getDynaDocsModel().getDynaDocsModelId();
        if(typeMarcheIdForNew==null || modePassationIdForNew==null || modelIdForNew==null){
            throw new RuntimeException("could not load latest active DynaDocs for DAC with typeMarche, modePassation and modelId ");
        }
//        List<DynaDocs> latestDocs = dynaDocsRepository.findAllLastVersionByTypeMarcheIdAndModePassationId(typeMarcheIdForNew, modePassationIdForNew,modelIdForNew);

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
                        value = dacDocsService.resolveSystemFieldValue(f, newDac);
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
                            log.info("7777777777777777777777");
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


    @Override
    @Transactional
    public Dac updateStatus(Integer dacId, Integer dacVer, ProcessStatus status) throws DacNotFound {
        Dac dac = dacRepository.findDacByDacIdAndDacVer(dacId,dacVer)
                .orElseThrow(() -> new DacNotFound("DAC not found", dacId, dacVer));


        dac.setStatus(status);

        dac.setUpdatedAt(Calendar.getInstance());

        if (status == ProcessStatus.PUBLIE ) {
            dac.setPublishedAt(Calendar.getInstance());
        }
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
    @Transactional
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
            Map<String, String> bookmarks = new HashMap<>();
            
            // Collect unique DynaDocs identifiers
            Set<String> processedDocs = new HashSet<>();
            for (DacDocs dd : dacDocs) {
                if (dd.getDynaField() != null && dd.getDynaField().getDynaDocs() != null) {
                    Integer docId = dd.getDynaField().getDynaDocs().getId();
                    Integer docVer = dd.getDynaField().getDynaDocs().getVersion();
                    String key = docId + ":" + docVer;
                    if (processedDocs.add(key)) {
                         Map<String, String> docBookmarks = createBookmarkMap(dacId, dacVersion, docId, docVer);
                         for (Map.Entry<String, String> entry : docBookmarks.entrySet()) {
                             String k = entry.getKey();
                             String v = entry.getValue();
                             // If the value is "real" (not a placeholder matching the key), overwrite
                             // Or if we don't have this key yet, add it (even if it is a placeholder)
                             if (!v.equals(k) || !bookmarks.containsKey(k)) {
                                 bookmarks.put(k, v);
                             }
                         }
                    }
                }
            }
            
            // Apply AAO specific parameters (overriding if necessary)
            bookmarks.putAll(createBookmarkMapForAAO(dacId, dacVersion, signataire, fonction));
            
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

    @Override
    @Transactional
    public Dac createFromAppelOffre(CreateDacFromAoRequest request) 
            throws AppelOffreNotFound, InvalidDacRequestException {
        log.info("Creating DAC(s) from Appel d'Offre with ID: {}", request.getAppelOffreId());
        
        // 1. Validate request
        validateDacFromAoRequest(request);
        
        // 2. Load AppelOffre and validate it exists
        AppelOffre appelOffre = appelOffreRepository.findById(request.getAppelOffreId())
                .orElseThrow(() -> new AppelOffreNotFound(request.getAppelOffreId()));
        
        // 3. Get PPM and Realisation from AO
        Ppm ppm = appelOffre.getPpm();
        if (ppm == null) {
            throw new InvalidDacRequestException("Tout Appel d'Offres doit obligatoirement être associé à un PPM");
        }
        
        Realisation realisation = appelOffre.getRealisation();
        if (realisation == null) {
            throw new InvalidDacRequestException("Tout Appel d'Offres doit obligatoirement être associé à une Réalisation.");
        }
        
        // Get ModeDePassationSelection from Realisation to check etapes
        ModeDePassationSelection modePassation = realisation.getModePassation();
        log.info("mode passation: "+modePassation);
        if (modePassation == null) {
            throw new InvalidDacRequestException("Realisation must have a Mode de Passation");
        }
        validateActiveAAOExists(realisation.getTypeMarcher().getId(), modePassation.getId());

        // 4. Check existing DACs for this AO to determine what can be created
        List<Dac> existingDacs = dacRepository.findAll().stream()
                .filter(d -> d.getAppelOffre() != null 
                        && d.getAppelOffre().getId().equals(appelOffre.getId()))
                .collect(Collectors.toList());
        
        // Determine number of DACs to create based on ModePassationEtape
        int numberOfDacsToCreate;
        String etapeForDac;

        if (modePassation.getEtapes() == ModePassationEtape.ETAPE_UNIQUE) {
            if (!existingDacs.isEmpty()) {
                throw new InvalidDacRequestException(
                        "Impossible de créer un DAC supplémentaire. Le mode de passation est ÉTAPE_UNIQUE et un DAC existe déjà pour cet Appel d'Offres.");
            }
            numberOfDacsToCreate = 1;
            etapeForDac = "1";
            log.info("Creating DAC for ETAPE_UNIQUE mode");

        } else if (modePassation.getEtapes() == ModePassationEtape.DEUX_ETAPES) {
            boolean etape1Exists = existingDacs.stream().anyMatch(d -> "1".equals(d.getEtapeDac()));
            boolean etape2Exists = existingDacs.stream().anyMatch(d -> "2".equals(d.getEtapeDac()));

            if (!etape1Exists) {
                numberOfDacsToCreate = 1;
                etapeForDac = "1";
                log.info("Creating first DAC (etape 1) for DEUX_ETAPES mode");
            } else if (!etape2Exists) {
                numberOfDacsToCreate = 1;
                etapeForDac = "2";
                log.info("Creating second DAC (etape 2) for DEUX_ETAPES mode");
            } else {
                throw new InvalidDacRequestException(
                        "Impossible de créer un DAC supplémentaire. Le mode de passation DEUX_ETAPES autorise un maximum de 2 DAC et ils existent déjà.");
            }
        } else {
            throw new InvalidDacRequestException(
                    "Mode de Passation Etape invalide: " + modePassation.getEtapes());
        }

        List<Dac> createdDacs = new ArrayList<>();

        // 5. Create DAC(s)
        Dac dac = null;
        Dac savedDac = null;
        for (int i = 0; i < numberOfDacsToCreate; i++) {
            dac = new Dac();

            // Generate new DAC ID and set version
            Integer newDacId = sequenceGeneratorService.getNextId(Constants.DAC_ID_SEQ);
            dac.setDacId(newDacId);
            dac.setDacVer(1);
            dac.setReference(generateDacRef(newDacId, ppm.getAc().getNom(),ppm));

            // Set reference to source Appel d'Offre (PPM and Realisation accessed through this)
            dac.setAppelOffre(appelOffre);

            // Configure DAC defaults
            dac.setEtapeDac(etapeForDac);
            dac.setStatus(ProcessStatus.BROUILLON);
            dac.setPayant(false);
            boolean isRestraintMode = modePassation.isRestraint();
            boolean isSecondStepDac = modePassation.getEtapes() == ModePassationEtape.DEUX_ETAPES && "2".equals(etapeForDac);
            dac.setShortListing(isRestraintMode || isSecondStepDac);

            // Set process-related fields

            // Set dynamic docs model if provided
            if (request.getDynaDocsModelId() != null) {
                DynaDocsModel dynaDocsModel = dynaDocsModelRepository.findById(request.getDynaDocsModelId())
                        .orElseThrow(() -> new DynaDocsModelNotFoundExeption(request.getDynaDocsModelId()));
                dac.setDynaDocsModel(dynaDocsModel);
            }

            // Save DAC
            savedDac = dacRepository.save(dac);
            Integer typeMarcheId = realisation.getTypeMarcher().getId();
            Integer modePassationId = realisation.getModePassation() != null ? realisation.getModePassation().getId() : null;
            List<DynaDocs> latestDocsFiltred =  getAllDynaDocsWithTypeMarchRealisationModelIds(typeMarcheId, modePassationId, dac.getDynaDocsModel().getDynaDocsModelId());
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
                    id.setDacId(savedDac.getDacId());
                    id.setDacVer(savedDac.getDacVer());
                    id.setDynaDocsId(dd.getId());
                    id.setDynaDocsVer(dd.getVersion());
                    id.setDynaFieldsId(f.getDynaFieldsId());

                    // Avoid duplicates if any pre-existing rows
                    Optional<DacDocs> exists = dacDocsRepository.findDacDocsBYPKs(id.getDacId(), id.getDacVer(), id.getDynaDocsId(), id.getDynaDocsVer(), id.getDynaFieldsId());
                    if (exists.isPresent()) continue;

                    DacDocs row = new DacDocs();
                    row.setId(id);
                    row.setDac(savedDac);
                    row.setDynaField(f);
                    row.setSourceId("");
                    row.setFieldValue(null);
                        dacDocsRepository.save(row);
                }
            }
            createdDacs.add(savedDac);
        }

        return savedDac;
    }
    
    private void validateDacFromAoRequest(CreateDacFromAoRequest request) {
        if (request.getAppelOffreId() == null) {
            throw new InvalidDacRequestException("Échec de la validation : l'ID de l'Appel d'Offres est requis.");
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public DacWithAoReferenceResponse getDacWithAoReference(Integer dacId, Integer dacVer) throws DacNotFound {
        log.debug("Getting DAC with AO reference for ID: {}, Version: {}", dacId, dacVer);
        
        // Load DAC
        Dac dac = dacRepository.findById(new DacId(dacId, dacVer))
                .orElseThrow(() -> new DacNotFound("DAC non trouvé avec l'ID : {} et la Version : {}." ,dacId, dacVer));
        
        // Build response
        DacWithAoReferenceResponse response = new DacWithAoReferenceResponse();
        
        // Set DAC fields
        response.setDacId(dac.getDacId());
        response.setDacVer(dac.getDacVer());
        response.setReference(dac.getReference());
        response.setStatus(dac.getStatus() != null ? dac.getStatus().name() : null);
        response.setDateLimiteSoumission(dac.getDateLimiteSoumission());
        response.setDateLimiteRecptionDemande(dac.getDateLimiteRecptionDemande());
        response.setPayant(dac.isPayant());
        
        // Get montant from DacPrix if exists
        // Note: DacPrixId uses Dac object, need to query differently
        Optional<DacPrix> dacPrixOpt = dacPrixRepository.findAll().stream()
                .filter(dp -> dp.getId().getDac().getDacId().equals(dacId) 
                        && dp.getId().getDac().getDacVer().equals(dacVer))
                .findFirst();
        dacPrixOpt.ifPresent(prix -> response.setMontantPayant(prix.getPrix()));
        
        response.setResponsableDacId(dac.getResponsableDacId());
        response.setConditionsParticipation(dac.getConditionsParticipation());
        response.setEtapeDac(dac.getEtapeDac());
        response.setShortListing(dac.isShortListing());
        // Set shortlist members if enabled
        if (Boolean.TRUE.equals(dac.isShortListing())) {
            List<SocieteDac> shortlists = societeDacRepository.findByDacIdAndDacVer(dacId, dacVer);
            if (!shortlists.isEmpty()) {
                List<DacWithAoReferenceResponse.ShortlistMemberData> members = shortlists.stream()
                        .map(sl -> {
                            Societe s = sl.getId().getSociete();
                            return new DacWithAoReferenceResponse.ShortlistMemberData(
                                    s.getId(),
                                    s.getNom(),
                                    s.getNif()
                            );
                        })
                        .collect(Collectors.toList());
                response.setShortlistMembers(members);
            }
        }
        
        // Set AO reference data
        if (dac.getAppelOffre() != null) {
            AppelOffre ao = dac.getAppelOffre();
            DacWithAoReferenceResponse.AoReferenceData aoData = new DacWithAoReferenceResponse.AoReferenceData();
            aoData.setAoId(ao.getId());
            aoData.setAoReference(ao.getReference());
            aoData.setAoNom(ao.getNom());
            aoData.setAoDescription(ao.getDescription());
            aoData.setEtape(ao.getEtape());
            aoData.setLotSepares(ao.getLotSepares());
            response.setAoReference(aoData);
        }
        
        // Set PPM reference data (accessed through AppelOffre)
        if (dac.getAppelOffre() != null && dac.getAppelOffre().getPpm() != null) {
            Ppm ppm = dac.getAppelOffre().getPpm();
            DacWithAoReferenceResponse.PpmReferenceData ppmData = new DacWithAoReferenceResponse.PpmReferenceData();
            ppmData.setPpmId(ppm.getId());
            ppmData.setPpmVer(ppm.getVersion());
            ppmData.setPpmRef(ppm.getRef());
            ppmData.setPpmNom(ppm.getNom());
            ppmData.setAnnee(ppm.getAnnee());
            
            if (ppm.getAc() != null) {
                ppmData.setAcNom(ppm.getAc().getNom());
                ppmData.setAcId(ppm.getAc().getId());
            }
            
            // Set publication date from created date (convert Calendar to LocalDate)
            if (ppm.getCreatedAt() != null) {
                ppmData.setDatePublication(ppm.getCreatedAt().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate());
            }
            
            response.setPpmReference(ppmData);
        }
        
        // Set Realisation reference data (accessed through AppelOffre)
        if (dac.getAppelOffre() != null && dac.getAppelOffre().getRealisation() != null) {
            Realisation realisation = dac.getAppelOffre().getRealisation();
            DacWithAoReferenceResponse.RealisationReferenceData realisationData = 
                    new DacWithAoReferenceResponse.RealisationReferenceData();
            
            realisationData.setRealisationId(realisation.getId());
            realisationData.setRealisationVer(realisation.getVersion());
            realisationData.setRealisationEnvisagees(realisation.getRealisationEnvisagees());

            if (realisation.getTypeMarcher() != null) {
                realisationData.setTypeMarcher(realisation.getTypeMarcher().getCode());
                realisationData.setTypeMarcherLibelle(realisation.getTypeMarcher().getDescription());
            }

            // Mode de passation
            if (realisation.getModePassation() != null) {
                realisationData.setModePassation(realisation.getModePassation().getLibelle());
                realisationData.setModePassationLibelle(realisation.getModePassation().getLibelle());
                realisationData.setIsInternational(
                        realisation.getModePassation().getType() == ModePassationType.INTERNATIONAL
                );}
            
            // Financing sources
            if (realisation.getFinancements() != null && !realisation.getFinancements().isEmpty()) {
                List<DacWithAoReferenceResponse.FinancementData> financementList = new ArrayList<>();
                for (Financement fin : realisation.getFinancements()) {
                    DacWithAoReferenceResponse.FinancementData finData = 
                            new DacWithAoReferenceResponse.FinancementData();
                    finData.setBudgetEstimer(fin.getBudgetEstimer());
                    finData.setTauxApplicable(fin.getTauxApplicable());
                    
                    if (fin.getSourceFinancement() != null) {
                        finData.setSourceFinancement(fin.getSourceFinancement().getCode());
                    }
                    
                    if (fin.getMonnaie() != null) {
                        finData.setMonnaie(fin.getMonnaie().getCode());
                    }
                    
                    financementList.add(finData);
                }
                realisationData.setSourcesFinancement(financementList);
            }
            
            // Lots
            if (realisation.getLots() != null && !realisation.getLots().isEmpty()) {
                List<DacWithAoReferenceResponse.LotData> lotList = new ArrayList<>();
                for (Lot lot : realisation.getLots()) {
                    DacWithAoReferenceResponse.LotData lotData = new DacWithAoReferenceResponse.LotData();
                    lotData.setLotId(lot.getId());
                    lotData.setLotVer(lot.getVersion());
                    lotData.setDescription(lot.getDescription());
                    lotData.setBudget(lot.getBudget());
                    lotList.add(lotData);
                }
                realisationData.setLots(lotList);
            }
            
            response.setRealisationReference(realisationData);
        }
        
        return response;
    }



    @Override
    public ReunionArgumentDocuments listDocDac(List<Integer> dacComIds, Integer dacId, Integer dacVersion) {
        ReunionArgumentDocuments dto = new ReunionArgumentDocuments();

        List<Integer> comIds=currentUserService.getCurrentUserComIds();
        if(dacComIds.stream().noneMatch(comIds::contains)){
            return dto;
        }
        Set<String> userComTypes = comService.findAll(
                        Specification.where(ComSpecifications.deleted(false))
                                .and(ComSpecifications.idIn(comIds))
                ).stream()
                .map(c -> c.getComType() != null ? c.getComType().getCode() : null)
                .collect(Collectors.toSet());
        if (dacId == null || dacVersion == null) throw new DacNotFound(null);

        // 2) Load reunions once
        Specification<Reunion> spec = Specification
                .where(ReunionSpecifications.deleted(false))
                .and(ReunionSpecifications.status(ReunionStatus.CLOTURER))
                .and(ReunionSpecifications.hasDacId(dacId))
                .and(ReunionSpecifications.hasDacVer(dacVersion));

        List<Reunion> reunions = reunionRepository.findAll(spec);
        log.info("listDocDac reunions fetched: {}", reunions.size());

        // 3) Process reunions
        for (Reunion r : reunions) {

            String reunionComType =
                    r.getCom() != null && r.getCom().getComType() != null && r.getCom().getComType().getCode() != null
                            ? r.getCom().getComType().getCode()
                            : null;

            if (reunionComType == null || reunionComType.isEmpty()) {
                log.warn("Skipping reunion {}: reunionComType is null/empty", r.getId());
                continue;
            }

            Set<String> alfrescoIds = r.getReunionDacs().stream()
                    .filter(Objects::nonNull)
                    // if you have rd.getReunionDacDocs()
                    .flatMap(rd -> rd.getReunionDacDocs().stream())
                    .filter(Objects::nonNull)
                    .map(dacDoc ->
                            dacId.equals(dacDoc.getDacId())
                                    && dacVersion.equals(dacDoc.getDacVer())
                                    && dacDoc.getDocs() != null
                                    ? dacDoc.getDocs().getAlfresco_id()
                                    : null
                    )
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            if (alfrescoIds.isEmpty()) {
                log.debug("Reunion {} type={} skipped: no docs", r.getId(), reunionComType);
                continue;
            }

            log.debug("Reunion {} type={} docsCount={}", r.getId(), reunionComType, alfrescoIds.size());

            if ("CPMP".equals(reunionComType)) {
                dto.getDocumentCPMP().addAll(alfrescoIds);
            }
            if ("CNCMP-S".equals(reunionComType) && (userComTypes.contains("CNCMP-S") || userComTypes.contains("CNCMP-P"))) {
                dto.getDocumentCNCMPSpecialise().addAll(alfrescoIds);
            }
            if ("CNCMP-P".equals(reunionComType)) {
                dto.getDocumentCNCMPPermanente().addAll(alfrescoIds);
            }
        }

        log.info("listDocDac END | cpmp={} perm={} spec={}",
                dto.getDocumentCPMP().size(),
                dto.getDocumentCNCMPPermanente().size(),
                dto.getDocumentCNCMPSpecialise().size());

        return dto;
    }
    @Override
    public PvResponse listPvComByDac(List<Integer> comIds, Integer dacId, Integer dacVersion) {

        PvResponse dto = new PvResponse();
        Set<String> pvCpmp = new HashSet<>();
        Set<String> pvCncmpSpecialise = new HashSet<>();
        Set<String>pvCpcmpPermanant=new HashSet<>();

        Set<String> userComTypes = comService.findAll(
                        Specification.where(ComSpecifications.deleted(false))
                                .and(ComSpecifications.idIn(comIds))
                ).stream()
                .map(c -> c.getComType() != null ? c.getComType().getCode() : null)
                .collect(Collectors.toSet());

        log.warn("START listPvComByDac: comIds={}, dacId={}, ver={}", comIds, dacId, dacVersion);

            Specification<Reunion> spec = Specification
                    .where(ReunionSpecifications.deleted(false))
                    .and(ReunionSpecifications.status(ReunionStatus.CLOTURER))
                    .and(ReunionSpecifications.hasDacId(dacId))
                    .and(ReunionSpecifications.hasDacVer(dacVersion));

            List<Reunion> reunions = reunionRepository.findAll(spec);


            for (Reunion r : reunions) {

                String pvAlfrescoId = r.getProcessVerbal()
                        .getDocuments()
                        .stream()
                        .filter(doc -> doc.isPv())
                        .findFirst()
                        .map(doc -> doc.getDoc().getAlfresco_id())
                        .orElse(null);

                log.warn("Reunion {} PV={}", r.getId(), pvAlfrescoId);

                if (pvAlfrescoId == null) continue;

                String reunionComType = r.getCom().getComType().getCode().trim();


                for (String userType : userComTypes) {


                    switch (userType) {

                        case "CPMP":
                            if ("CPMP".equals(reunionComType)&&pvCpmp.add(pvAlfrescoId)) {
                                dto.getPvCPMP().add(pvAlfrescoId);
                            }
                            if ("CNCMP-P".equals(reunionComType)&&pvCpcmpPermanant.add(pvAlfrescoId)) {
                                dto.getPvCNCMPPermanente().add(pvAlfrescoId);
                            }
                            break;

                        case "CNCMP-P":
                            if ("CNCMP-P".equals(reunionComType)&&pvCpcmpPermanant.add(pvAlfrescoId)) {
                                dto.getPvCNCMPPermanente().add(pvAlfrescoId);
                            }
                            if ("CNCMP-S".equals(reunionComType)) {
                                dto.getPvCNCMPSpecialise().add(pvAlfrescoId);
                            }
                            if ("CPMP".equals(reunionComType)&&pvCpmp.add(pvAlfrescoId)) {
                                dto.getPvCPMP().add(pvAlfrescoId);
                            }
                            break;

                        case "CNCMP-S":
                            // Your original condition is redundant inside this case, but keep it if you want:
                            if ("CNCMP-S".equals(reunionComType)&&pvCncmpSpecialise.add(pvAlfrescoId)
                                    && (userComTypes.contains("CNCMP-S") || userComTypes.contains("CNCMP-P"))) {
                                dto.getPvCNCMPSpecialise().add(pvAlfrescoId);
                            }
                            if ("CNCMP-P".equals(reunionComType)&&pvCpcmpPermanant.add(pvAlfrescoId)) {
                                dto.getPvCNCMPPermanente().add(pvAlfrescoId);
                            }
                            break;

                        default:
                            break;
                    }

                }
            }

                log.warn("FINAL PV RESPONSE: cpmp={}, perm={}, spec={}",
                dto.getPvCPMP(), dto.getPvCNCMPPermanente(), dto.getPvCNCMPSpecialise());

        return dto;
    }

    @Override
    public ExtraitResponse findAllExtraits(List<Integer> acIds, Integer dacId, Integer dacVer) {
        ExtraitResponse dto = new ExtraitResponse();
        Set<String> seenCpmp = new HashSet<>();
        Set<String> seenPermanent = new HashSet<>();
        List<ProcessVerbalReunionExtrait> extraits = processVerbalReunionExtraitService
                .findExtraitsByDacAndAcs(acIds, dacId, dacVer);

        for (ProcessVerbalReunionExtrait ex : extraits) {

            Optional<Reunion> reunionOpt = reunionRepository.findByProcessVerbalId(ex.getPvId());

            if (reunionOpt.isEmpty())
                continue;

            Reunion reunion = reunionOpt.get();

            String typeCom = reunion.getCom().getComType().getCode();
            log.info("EX: id={} type={} doc={} alfresco={} acId {}",
                    ex.getExtraitId(),
                    ex.getExtraitType(),
                    ex.getDoc()!=null,
                    ex.getDoc()!=null ? ex.getDoc().getAlfresco_id() : null,
                    ex.getAcId()
            );

            if ("CPMP".equals(typeCom)){

                String alfrescoId = ExtraitType.PAR_AC.equals(ex.getExtraitType())&&ex.getDoc() != null ? ex.getDoc().getAlfresco_id() : null;
                if (alfrescoId != null&&seenCpmp.add(alfrescoId)) {
                    dto.getExtraitCPMP().add(alfrescoId);
                }
            }
            else if ("CNCMP-P".equals(typeCom)){
                String alfrescoId = ExtraitType.PAR_DOSSIER.equals(ex.getExtraitType())&&ex.getDoc() != null ? ex.getDoc().getAlfresco_id() : null;
                if (alfrescoId != null&&seenPermanent.add(alfrescoId)) {
                    dto.getExtraitCNCMPP().add(alfrescoId);
                }
            }
        }

        return dto;
    }

}

