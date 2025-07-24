package com.sigmap.common.AlfrescoConf;

import com.sigmap.common.agpm.Agpm;
import com.sigmap.common.com.Com;
import com.sigmap.common.com.ComService;
import com.sigmap.common.config.Constants;
import com.sigmap.common.docs.Docs;
import com.sigmap.common.docs.DocsRepository;
import com.sigmap.common.docs.Request.DocumentHolder;
import com.sigmap.common.paas.Paa;
import com.sigmap.common.ppms.Ppm;
import com.sigmap.common.process_verbal.ProcessVerbal;
import com.sigmap.common.pv_docs.PvDocs;
import com.sigmap.common.pv_docs.PvDocsRepository;
import com.sigmap.common.pv_docs.request.PvDocRequest;
import com.sigmap.common.realisations.Realisation;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.JsonNode;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import javax.print.Doc;
import java.io.IOException;
import java.util.*;

@Slf4j
@Service
public class DocumentCommuneService {
    @Autowired
    private AlfrescoService alfrescoService;
    private final DocsRepository docsRepository;
    @Autowired
    private PvDocsRepository pvDocsRepository;


    public DocumentCommuneService(DocsRepository docsRepository) {
        this.docsRepository = docsRepository;
    }

    /**
     * Detects the file extension based on the MIME type provided.
     * The method uses a map of MIME types and their corresponding file extensions.
     *
     * @param mimeType The MIME type to detect the extension for.
     * @return The file extension corresponding to the MIME type, or an empty string if not found.
     */
    public String detectExtensionFromMimeType(String mimeType) {
        Map<String, String> mimeToExtension = new HashMap<>();

        // Documents
        mimeToExtension.put("application/msword", "doc");
        mimeToExtension.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx");
        mimeToExtension.put("application/vnd.oasis.opendocument.text", "odt");
        mimeToExtension.put("application/pdf", "pdf");
        mimeToExtension.put("application/rtf", "rtf");

        // Spreadsheets
        mimeToExtension.put("application/vnd.ms-excel", "xls");
        mimeToExtension.put("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx");
        mimeToExtension.put("application/vnd.oasis.opendocument.spreadsheet", "ods");
        mimeToExtension.put("application/vnd.ms-excel.sheet.macroenabled.12", "xlsm");

        // Presentations
        mimeToExtension.put("application/vnd.ms-powerpoint", "ppt");
        mimeToExtension.put("application/vnd.openxmlformats-officedocument.presentationml.presentation", "pptx");
        mimeToExtension.put("application/vnd.oasis.opendocument.presentation", "odp");
        mimeToExtension.put("application/vnd.ms-powerpoint.presentation.macroenabled.12", "pptm");

        // Images
        mimeToExtension.put("image/jpeg", "jpg");
        mimeToExtension.put("image/png", "png");
        mimeToExtension.put("image/gif", "gif");
        mimeToExtension.put("image/bmp", "bmp");
        mimeToExtension.put("image/webp", "webp");
        mimeToExtension.put("image/svg+xml", "svg");
        mimeToExtension.put("image/tiff", "tiff");

        // Audio
        mimeToExtension.put("audio/mpeg", "mp3");
        mimeToExtension.put("audio/ogg", "ogg");
        mimeToExtension.put("audio/wav", "wav");
        mimeToExtension.put("audio/aac", "aac");

        // Video
        mimeToExtension.put("video/mp4", "mp4");
        mimeToExtension.put("video/x-msvideo", "avi");
        mimeToExtension.put("video/x-ms-wmv", "wmv");
        mimeToExtension.put("video/ogg", "ogv");
        mimeToExtension.put("video/webm", "webm");

        // Compressed files
        mimeToExtension.put("application/zip", "zip");
        mimeToExtension.put("application/x-rar-compressed", "rar");
        mimeToExtension.put("application/x-7z-compressed", "7z");
        mimeToExtension.put("application/gzip", "gz");
        mimeToExtension.put("application/x-tar", "tar");

        return mimeToExtension.getOrDefault(mimeType, "");
    }

    /**
     * Converts a file size in bytes to a human-readable format (KB, MB, GB).
     *
     * @param sizeInBytes The file size in bytes.
     * @return A formatted string representing the file size with the appropriate unit.
     */
    public String formatSize(long sizeInBytes) {
        double size = sizeInBytes;
        String unit = "bytes";
        if (size >= 1024) {
            size /= 1024;
            unit = "KB";
        }
        if (size >= 1024) {
            size /= 1024;
            unit = "MB";
        }
        if (size >= 1024) {
            size /= 1024;
            unit = "GB";
        }
        return String.format("%.2f %s", size, unit);
    }

    /**
     * Extracts the original file name without its extension.
     *
     * @param file The uploaded file.
     * @return The original file name without its extension.
     */
    public String getOriginalFileNameWithoutExtension(MultipartFile file) {
        String originalFileName = file.getOriginalFilename();

        if (originalFileName == null || originalFileName.lastIndexOf(".") == -1) {
            return originalFileName; // Return the name as it is if no extension is found
        }

        return originalFileName.substring(0, originalFileName.lastIndexOf("."));
    }

    // Convertir la taille de la chaîne en octets
    /**
     * Parses a size string (e.g., "10 MB", "200 KB", "1 GB") and converts it to bytes.
     *
     * @param size The size string to parse.
     * @return The size in bytes.
     */
    public long parseSizeToBytes(String size) {
        if (size.toLowerCase().endsWith("mb")) {
            return Long.parseLong(size.replace("MB", "").trim()) * 1024 * 1024;
        } else if (size.toLowerCase().endsWith("kb")) {
            return Long.parseLong(size.replace("KB", "").trim()) * 1024;
        } else if (size.toLowerCase().endsWith("gb")) {
            return Long.parseLong(size.replace("GB", "").trim()) * 1024 * 1024 * 1024;
        } else {
            return Long.parseLong(size);
        }
    }
    /**
     * Récupère un document à partir de son ID dans le service Alfresco et le convertit en un objet ViewFileAlf.
     *
     * @param id L'ID du document à récupérer.
     * @return L'objet ViewFileAlf correspondant au document.
     * @throws Exception Si une erreur se produit lors de la récupération du document.
     */
    public ViewFileAlf getFilebyId(String id) throws Exception {
        DocumentResponseDTO doc = alfrescoService.getDocumentById(id);
        return docToAttachment(doc);
    }

    /**
     * Convertit un document Alfresco en un objet ViewFileAlf.
     * Cette méthode extrait le contenu du document, y compris son nom, son type MIME, et les données du fichier.
     *
     * @param doc Le document Alfresco à convertir.
     * @return L'objet ViewFileAlf contenant les informations du document.
     * @throws IOException Si une erreur survient lors de la lecture du flux de contenu du document.
     */
    public ViewFileAlf docToAttachment(DocumentResponseDTO doc) throws IOException {
        ViewFileAlf aa = new ViewFileAlf();

        // Check for errors in DocumentResponseDTO
        if (doc.isErreur()) {
            throw new IOException("Error fetching document content: " + doc.getMsg());
        }

        // Fetch metadata using getDocumentInformation
        String metadataJson = alfrescoService.getDocumentInformation(doc.getDocumentId());
        JsonNode metadata = new ObjectMapper().readTree(metadataJson);

        // Extract file name and MIME type from metadata
        String fileName = metadata.path("entry").path("name").asText();
        String mimeType = metadata.path("entry").path("content").path("mimeType").asText();

        // Populate ViewFileAlf
        aa.setName(fileName); // Set the file name
        aa.setType(mimeType); // Set the MIME type
        aa.setData(doc.getFile()); // Set the content directly from byte[]

        return aa;
    }
    public <T extends DocumentHolder> void processAndSaveDocuments(List<MultipartFile> files, T entity, String path) throws IOException {

        String entityTypeName = entity.getClass().getSimpleName().toLowerCase();
        String baseFolderPath = String.format("%s/%d/%d",path  , entity.getId(), entity.getVersion());

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                log.warn("Skipping null or empty file in the list.");
                continue; // Skip to the next file
            }

            try {
                //helper methode for mapping document
                Docs document = mapperDocument(file,entityTypeName,baseFolderPath);

                if (entity instanceof Realisation realisation) {
                    if(realisation.getDocs() == null) {
                        realisation.setDocs(new ArrayList<>());
                    }
                    realisation.getDocs().add(document);
                } else if (entity instanceof Paa paa) {
                    if(paa.getDocs() == null) {
                        paa.setDocs(new ArrayList<>());
                    }
                    paa.getDocs().add(document);
                }
                else if(entity instanceof Agpm agpm) {
                    if(agpm.getDocs() == null) {
                        agpm.setDocs(new ArrayList<>());
                    }
                    agpm.getDocs().add(document);
                }
                else {
                    Ppm ppm= (Ppm) entity;
                    if(ppm.getDocs() == null) {
                        ppm.setDocs(new ArrayList<>());
                    }
                    ppm.getDocs().add(document);
                }
                // Save the document (Docs entity)
                docsRepository.save(document);

            } catch (Exception e) {
                log.error("Failed to process file: OriginalName={}, Error={}", file.getOriginalFilename(), e.getMessage());
                // Re-throw as IOException for upstream handling
                throw new IOException("Failed to process or upload document: " + e.getMessage(), e);
            }
        }

    }
    public Docs mapperDocument(MultipartFile file, String entityTypeName, String baseFolderPath) throws Exception {
        //1-extract file data

        UUID uuid = UUID.randomUUID();
        Tika tika = new Tika();
        String detectedMimeType = tika.detect(file.getInputStream());
        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "document";

        String fileExtension = detectExtensionFromMimeType(detectedMimeType);
        if (fileExtension.isEmpty()) {
            // Fallback to original filename extension if MIME type detection fails
            fileExtension = detectExtensionFromMimeType(originalFilename);
        }
        String[] parts = originalFilename.split("\\.");
        String baseName = parts[0];
        String extension = parts.length > 1 ? "." + parts[parts.length - 1] : "";
        String uniqueFileName = entityTypeName +"_"+ uuid + "_" + extension;
        MultipartFile renamedFile = new RenamedMultipartFile(file, uniqueFileName);
        long fileSize = file.getSize();
        String formattedSize = formatSize(fileSize);

        String alfrescoId = alfrescoService.uploadDocument(renamedFile, baseFolderPath);
        log.info("Uploaded document to Alfresco: AlfrescoID={}, FileName={}, Path={}", alfrescoId, uniqueFileName, baseFolderPath);

        //2-remplissage mte3 document with data
        Docs document = new Docs();
        document.setName(baseName);
        document.setAlfresco_id(alfrescoId);
        document.setPathFolder(baseFolderPath);
        document.setFileSize(formattedSize);
        document.setContentType(detectedMimeType);
        document.setFileExtension(fileExtension.isEmpty() ? extension.replace(".", "") : fileExtension);
        document.setAlfrescoName(uniqueFileName);
        return document;

    }
    public void processAndSaveDocumentsForPv(List<PvDocRequest> docRequests, String path, ProcessVerbal processVerbal) throws IOException {

        String baseFolderPath = String.format(path  +"/"+ processVerbal.getPpmId()+"/"+processVerbal.getPpmVersion()+Constants.PATH_ENTREPOT_GED_PV);

        for (PvDocRequest docRequest : docRequests) {
            MultipartFile file = docRequest.getDocument();
            boolean isPv = docRequest.isPv();
            if (file == null || file.isEmpty()) {
                log.warn("Skipping null or empty file in the list.");
                continue; // Skip to the next file
            }
            try {
                if(processVerbal.getDocuments() == null) {
                    processVerbal.setDocuments(new HashSet<>());
                }
                Docs document = mapperDocument(file,"ppm_pv",baseFolderPath);
                Docs doc= docsRepository.save(document);
                PvDocs pvDocs= new PvDocs();
                pvDocs.setDocId(doc.getId());
                pvDocs.setPv(isPv);
                pvDocs.setProcessVerbalId(processVerbal.getProcessVerbalId());
                pvDocs.setPpmId(processVerbal.getPpmId());
                pvDocs.setPpmVersion(processVerbal.getPpmVersion());
                pvDocs.setDoc(document);
                pvDocsRepository.save(pvDocs);
                processVerbal.getDocuments().add(pvDocs);



            } catch (Exception e) {
                log.error("Failed to process file: OriginalName={}, Error={}", file.getOriginalFilename(), e.getMessage());
                // Re-throw as IOException for upstream handling
                throw new IOException("Failed to process or upload document: " + e.getMessage(), e);
            }
        }

    }

    public  void processAndSaveDocumentsForCom(List<MultipartFile> files, Com entity, String path) throws IOException {

        String entityTypeName = entity.getClass().getSimpleName().toLowerCase();
        String baseFolderPath = String.format("%s/%d",path  , entity.getId());

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                log.warn("Skipping null or empty file in the list.");
                continue; // Skip to the next file
            }

            try {
                //helper methode for mapping document
                Docs document = mapperDocument(file,entityTypeName,baseFolderPath);


                    if(entity.getDocs() == null) {
                        entity.setDocs(new ArrayList<>());
                }
                entity.getDocs().add(document);
                // Save the document (Docs entity)
                docsRepository.save(document);



            } catch (Exception e) {
                log.error("Failed to process file: OriginalName={}, Error={}", file.getOriginalFilename(), e.getMessage());
                // Re-throw as IOException for upstream handling
                throw new IOException("Failed to process or upload document: " + e.getMessage(), e);
            }
        }
        log.info("///////////////////////////////////{}",entity.getDocs().size());
    }
}

