package com.sigmap.common.AlfrescoConf;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.JsonNode;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DocumentCommuneService {
    @Autowired
    private AlfrescoService alfrescoService;
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

}
