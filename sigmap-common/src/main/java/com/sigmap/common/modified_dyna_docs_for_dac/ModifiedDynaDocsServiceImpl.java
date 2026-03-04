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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.*;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

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
    public ModifiedDynaDocsRequest buildModifiedDynaDocsRequest( Integer dacId, Integer dacVer, MultipartFile aaoFile, List<MultipartFile> files, List<Integer> orders) {

        if (files.size() != orders.size()) {
            throw new IllegalArgumentException("Files and orders size must be equal");
        }

        List<ModifiedDynaDocsRequest.RestFiles> restFiles =
                IntStream.range(0, files.size())
                        .mapToObj(i -> new ModifiedDynaDocsRequest.RestFiles( files.get(i), orders.get(i) ))
                        .toList();

        return ModifiedDynaDocsRequest.builder()
                .dacId(dacId)
                .dacVer(dacVer)
                .AAOfile(aaoFile)
                .files(restFiles)
                .build();
    }

    @Override
    @Transactional
    public void uploadModifiedFiles(ModifiedDynaDocsRequest request) {

        List<ModifiedDynaDocs> actifs =  modifiedDynaDocsRepository.findByDacIdAndVersion(request.getDacId(), request.getDacVer());

        int nextVersion = actifs.stream()
                .map(ModifiedDynaDocs::getVersion)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0) + 1;

        Dac dac = dacService.findById(request.getDacId(), request.getDacVer())
                .orElseThrow(() -> new RuntimeException("DAC not found"));

        // AAO (ordre = 1)
        saveFile(request.getAAOfile(), nextVersion, dac, true, 1);

        // autres documents
        request.getFiles().forEach(rf -> saveFile(rf.getFile(), nextVersion, dac, false, rf.getOrder()) );

        // désactiver anciennes versions
        deactivateOldVersions(actifs);
    }



    private void saveFile(MultipartFile file, int version, Dac dac, boolean isAAO, int ordre) {

        try {
            String fileName = version + "_" + file.getOriginalFilename();
            Path path = Paths.get(basePath, fileName);
            OutputStream outputStream = Files.newOutputStream(path, StandardOpenOption.CREATE);
            IOUtils.copy(file.getInputStream(), outputStream);

            ModifiedDynaDocs entity = new ModifiedDynaDocs();
            entity.setIsAAO(isAAO);
            entity.setVersion(version);
            entity.setOrdre(ordre);
            entity.setName(FilenameUtils.getBaseName(file.getOriginalFilename()));
            entity.setTemplateFileName(file.getOriginalFilename());
            entity.setStatus(ActiveStatus.ACTIF);
            entity.setDac(dac);

            modifiedDynaDocsRepository.save(entity);

        } catch (IOException e) {
            throw new RuntimeException("Error uploading file " + file.getOriginalFilename(), e);
        }
    }

    private void deactivateOldVersions(List<ModifiedDynaDocs> list) {
        list.forEach(md -> {
            md.setStatus(ActiveStatus.NON_ACTIF);
            md.setUpdatedAt(Calendar.getInstance());
            md.setUpdatedBy(Authentication.getCurrentUserId().orElse("System"));
        });
        modifiedDynaDocsRepository.saveAll(list);
    }



}
