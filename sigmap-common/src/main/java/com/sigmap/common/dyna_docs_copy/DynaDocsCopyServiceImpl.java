package com.sigmap.common.dyna_docs_copy;

import com.sigmap.common.dac.Dac;
import com.sigmap.common.dac.DacService;
import com.sigmap.common.dyna_docs.DynaDocs;
import com.sigmap.common.dyna_docs.DynaDocsService;
import com.sigmap.common.entity.ActiveStatus;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.*;
import java.util.List;

import static java.util.UUID.randomUUID;
import static org.apache.poi.xdgf.util.Util.sanitizeFilename;

@Slf4j
@Service

public class DynaDocsCopyServiceImpl implements DynaDocsCopyService {
    private final DynaDocsCopyRepository dynaDocsCopyRepository;
    private final DacService dacService;
    private final DynaDocsService dynaDocsService;

    public DynaDocsCopyServiceImpl(DynaDocsCopyRepository dynaDocsCopyRepository, @Lazy DacService dacService, DynaDocsService dynaDocsService) {
        this.dynaDocsCopyRepository = dynaDocsCopyRepository;
        this.dacService = dacService;
        this.dynaDocsService = dynaDocsService;
    }

    @Value("${templateDac.dac.inputDocPath}")
    private String basePath;
    @Value("${templateDac.dac.inputModifiedDocPath}")
    private String inputModifiedDocPath;

    @Override
    public DynaDocsCopy save(DynaDocsCopy dynaDocsCopy) {
        return dynaDocsCopyRepository.save(dynaDocsCopy);

    }

    @Override
    public List<DynaDocsCopy> findActiveLastVersion(Integer dacId, Integer dacVer) {
        List<DynaDocsCopy> dynaDocsCopies= dynaDocsCopyRepository.findActiveLastVersionByDac(dacId, dacVer);
        log.debug("dynaDocsCopies: --------------------- {}", dynaDocsCopies.size());
        return dynaDocsCopies;
    }

    @Override
    public DynaDocsCopy findById(Integer id) {
        return dynaDocsCopyRepository.findById(id).orElseThrow(() -> new RuntimeException("DynaDocsCopy not found"));
    }

    @Override
    public byte[] downloadModifiedFile(Integer id) {
        DynaDocsCopy current = findById(id);
        String templateName = current.getTemplateFileName();
        if (templateName == null || templateName.isBlank()) {
            throw new RuntimeException("No template file name associated with this copy.");
        }
        Path filePath = safeResolve(inputModifiedDocPath, templateName);
        if (!Files.exists(filePath)) {
            throw new RuntimeException("Modified file not found on server.");
        }
        try {
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + e.getMessage(), e);
        }
    }

    @Transactional
    @Override
    public void uploadModifiedFiles(Integer id , MultipartFile file) {
        if (id == null) {
            throw new IllegalArgumentException("ID is required");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }

        DynaDocsCopy current = dynaDocsCopyRepository.findOneById(id)
                .orElseThrow(() -> new RuntimeException("DynaDocsCopy not found"));

        int nextVersion = (current.getVersion() == null ? 0 : current.getVersion()) + 1;

        // 1) Archive old file (if exists)
        archiveOldFileIfPresent(current);


        // 2) Update existing row (overwrite update)
        Dac dac = dacService.findById(current.getDac().getDacId(), current.getDac().getDacVer())
                .orElseThrow(() -> new RuntimeException("DAC not found"));

        DynaDocs dynaDocs = dynaDocsService.findById(current.getDynaDocs().getId(), current.getDynaDocs().getVersion())
                .orElseThrow(() -> new RuntimeException("DynaDocs not found"));

        // 3) Save new file
        String storedFileName = storeNewFile(file);
        SaveInformationNewFile(current, file, storedFileName, nextVersion, dac, dynaDocs);

        // 4) Update existing row (overwrite update)
        current.setStatus(ActiveStatus.NON_ACTIF);

        dynaDocsCopyRepository.save(current);

    }

    private void archiveOldFileIfPresent(DynaDocsCopy current) {
        String oldStoredName = current.getTemplateFileName();
        Integer oldVersion = current.getVersion();
        String uuid = randomUUID().toString();

        if (oldStoredName == null || oldStoredName.isBlank() || oldVersion == null) return;

        Path oldPath = safeResolve(inputModifiedDocPath, oldStoredName);
        if (!Files.exists(oldPath)) return;

        // If oldStoredName already has a prefix, you might want to avoid double-prefixing.
        // Here we always archive as "{oldVersion}_{oldStoredName}"
        String archivedName = oldVersion + "_" + uuid + "_" + oldStoredName;
        Path archivedPath = safeResolve(inputModifiedDocPath, archivedName);

        try {
            try {
                Files.move(oldPath, archivedPath, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(oldPath, archivedPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to archive old file: " + oldStoredName, e);
        }
    }

    private String storeNewFile(MultipartFile file) {
        String original = file.getOriginalFilename();
        String safeOriginal = sanitizeFilename(original != null ? original : "file.docx");

        Path target = safeResolve(inputModifiedDocPath, safeOriginal);

        try (InputStream in = file.getInputStream();
             OutputStream out = Files.newOutputStream(target,
                     StandardOpenOption.CREATE,
                     StandardOpenOption.TRUNCATE_EXISTING,
                     StandardOpenOption.WRITE)) {

            IOUtils.copy(in, out);
            return safeOriginal;

        } catch (IOException e) {
            throw new RuntimeException("Error uploading file " + safeOriginal, e);
        }
    }

    private void SaveInformationNewFile(DynaDocsCopy currentCopy,MultipartFile file, String storedFileName, int version, Dac dac, DynaDocs dynaDocs) {

        DynaDocsCopy entity = new DynaDocsCopy();
        entity.setIsAAO(currentCopy.getIsAAO());
        entity.setVersion(version);
        entity.setOrdre(currentCopy.getOrdre());
        entity.setName(FilenameUtils.getBaseName(file.getOriginalFilename()));
        entity.setTemplateFileName(storedFileName);
        entity.setStatus(ActiveStatus.ACTIF);
        entity.setDac(dac);
        entity.setIsStatic(currentCopy.getIsStatic());
        entity.setDynaDocs(dynaDocs);

        dynaDocsCopyRepository.save(entity);

    }

    // Prevent path traversal + keep under basePath
    private Path safeResolve(String basePath, String fileName) {
        Path base = Paths.get(basePath).toAbsolutePath().normalize();
        Path resolved = base.resolve(fileName).toAbsolutePath().normalize();
        if (!resolved.startsWith(base)) {
            throw new SecurityException("Invalid file path");
        }
        return resolved;
    }


    public void cloneDynaDocsIntoDynaDocsCopie(Dac savedDac, List<DynaDocs> docs) {

        if (savedDac == null) throw new IllegalArgumentException("savedDac is required");
        if (docs == null) throw new IllegalArgumentException("docs is required");

        if (basePath == null || basePath.isBlank()) {
            throw new IllegalStateException("basePath is not configured");
        }
        if (inputModifiedDocPath == null || inputModifiedDocPath.isBlank()) {
            throw new IllegalStateException("inputModifiedDocPath is not configured");
        }

        Path baseDir = Paths.get(basePath).toAbsolutePath().normalize();
        Path modifiedDir = Paths.get(inputModifiedDocPath).toAbsolutePath().normalize();

        for (DynaDocs dd : docs) {
            String templateName = dd.getTemplateFileName();

            if (templateName != null && !templateName.isBlank()) {
                // Prevent path traversal if templateName comes from DB/user
                String safeName = templateName.replace("\\", "/");
                if (safeName.contains("..") || safeName.startsWith("/")) {
                    log.error("nom de fichier non sécurisé: {}", templateName);
                } else {
                    Path source = baseDir.resolve(safeName).normalize();
                    Path destination = modifiedDir.resolve(safeName).normalize();

                    // Ensure still inside base directories
                    if (!source.startsWith(baseDir) || !destination.startsWith(modifiedDir)) {
                        log.error("Path traversal detected for template: {}", templateName);
                    } else {
                        try {
                            if (Files.exists(source)) {
                                Files.createDirectories(destination.getParent());
                                Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
                                log.info("Cloned template file: {}", templateName);
                            } else {
                                log.warn("Template file does not exist: {}", source);
                            }
                        } catch (IOException e) {
                            log.error("Failed to copy template file during cloning: {}", templateName, e);
                        }
                    }
                }
            }

            DynaDocsCopy copy = new DynaDocsCopy();
            copy.setVersion(1);
            copy.setName(dd.getName());
            copy.setTemplateFileName(dd.getTemplateFileName());
            copy.setStatus(dd.getStatus());
            copy.setIsAAO(dd.getIsAAO());
            copy.setOrdre(dd.getDynaDocOrder());
            copy.setDac(savedDac);
            copy.setIsStatic(dd.getIsStatic());
            copy.setDynaDocs(dd);

            dynaDocsCopyRepository.save(copy);
        }
    }
}
