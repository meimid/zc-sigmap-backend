package com.sigmap.common.modified_dyna_docs_for_dac;

import com.sigmap.common.dac.Dac;
import com.sigmap.common.dac.DacRepository;
import com.sigmap.common.dac.DacService;
import com.sigmap.common.entity.ActiveStatus;
import com.sigmap.common.modified_dyna_docs_for_dac.request.ModifiedDynaDocsRequest;
import com.sigmap.common.security.Authentication;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;
import java.nio.file.*;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
public class ModifiedDynaDocsServiceImpl implements ModifiedDynaDocsService {

    private final ModifiedDynaDocsRepository modifiedDynaDocsRepository;
    private final DacService dacService;

    public ModifiedDynaDocsServiceImpl(ModifiedDynaDocsRepository modifiedDynaDocsRepository, DacService dacService) {
        this.modifiedDynaDocsRepository = modifiedDynaDocsRepository;
        this.dacService = dacService;
    }

    @Value("${templateDac.dac.inputModifiedDocPath}")
    private String basePath;

    @Override
    @Transactional
    public void uploadModifiedFiles(ModifiedDynaDocsRequest request, MultipartFile AAOfile, List<MultipartFile> files) {

        List<ModifiedDynaDocs> modifiedDynaDocsList = modifiedDynaDocsRepository
                .findByDacIdAndVersion(request.getDacId(), request.getDacVer());

        int maxVersion = modifiedDynaDocsList.stream()
                .map(ModifiedDynaDocs::getVersion)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0);

        int nextVersion = maxVersion + 1;

        try {
            Optional<Dac> dac = dacService.findById(request.getDacId(), request.getDacVer());
            if (dac.isEmpty()) {
                throw new RuntimeException("Dac not found");
            }
            uploadAAO(AAOfile, nextVersion, dac.get());
            uploadFiles(files, nextVersion, dac.get());
            saveModifiedDynaDocs(modifiedDynaDocsList);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public void uploadAAO(MultipartFile AAOfile, int nextVersion, Dac dac) {

        try {
            log.info("Uploading AAO file: {}", AAOfile.getOriginalFilename());
            Path templatePath = Paths.get(basePath, nextVersion +"_"+AAOfile.getOriginalFilename());
            OutputStream outputStream = Files.newOutputStream(templatePath, StandardOpenOption.CREATE);
            IOUtils.copy(AAOfile.getInputStream(), outputStream);

            ModifiedDynaDocs modifiedDynaDocs = createModifiedDynaDocs(AAOfile, nextVersion, dac, true);

            modifiedDynaDocsRepository.save(modifiedDynaDocs);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public void uploadFiles(List<MultipartFile> files, int nextVersion, Dac dac) {

        try {
            for (MultipartFile file : files) {
                log.info("Uploading file: {}", file.getOriginalFilename());
                Path templatePath = Paths.get(basePath, nextVersion +"_"+file.getOriginalFilename());
                OutputStream outputStream = Files.newOutputStream(templatePath, StandardOpenOption.CREATE);
                IOUtils.copy(file.getInputStream(), outputStream);

                ModifiedDynaDocs modifiedDynaDocs = createModifiedDynaDocs(file, nextVersion, dac, false);
                modifiedDynaDocsRepository.save(modifiedDynaDocs);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private ModifiedDynaDocs createModifiedDynaDocs(MultipartFile file, int version, Dac dac, boolean isAAO) {

        String fileName = FilenameUtils.getBaseName(file.getOriginalFilename());

        ModifiedDynaDocs modifiedDynaDocs = new ModifiedDynaDocs();
        modifiedDynaDocs.setIsAAO(isAAO);
        modifiedDynaDocs.setVersion(version);
        modifiedDynaDocs.setName(fileName);
        modifiedDynaDocs.setTemplateFileName(file.getOriginalFilename());
        modifiedDynaDocs.setStatus(ActiveStatus.ACTIF);
        modifiedDynaDocs.setDac(dac);
        return modifiedDynaDocs;
    }

    void saveModifiedDynaDocs(List<ModifiedDynaDocs> modifiedDynaDocsList) {
        modifiedDynaDocsList.forEach(modifiedDynaDocs -> {
            modifiedDynaDocs.setStatus(ActiveStatus.NON_ACTIF);
            modifiedDynaDocs.setUpdatedAt(Calendar.getInstance());
            modifiedDynaDocs.setUpdatedBy(Authentication.getCurrentUserId().orElse("System"));
        });
        modifiedDynaDocsRepository.saveAll(modifiedDynaDocsList);
    }

}
