package com.sigmap.common.dac_docs;

import com.sigmap.common.dac.request.DacRequest;
import com.sigmap.common.dac_docs.request.DacDoccsAndDacRequest;
import com.sigmap.common.dac_docs.request.DacDocsRequest;
import com.sigmap.common.dac_docs.response.customResponceDTO.DacDocResponseDTO;

import com.sigmap.common.monnaie.exceptions.MonnaieNotFound;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

import org.springframework.web.multipart.MultipartFile;

public interface DacDocsService {
    DacDocs createDacDocs(DacDocsRequest request);
    DacDocs updateDacDocs(DacDocsRequest request);
    List<DacDocs> createDacDocsBatch(List<DacDocsRequest> requests);

    List<DacDocs> findOneById(Integer dacId, Integer dacVer);
    Optional<DacDocs> findById(Integer dacId, Integer dacVer, Integer dynaDocsId, Integer dynaDocsVer, Integer dynaFieldsId);
    List<DacDocResponseDTO> findByDacIdAndVer(Integer dacId, Integer dacVer);
    List<DacDocs> findByDacIdAndDacVer(Integer dacId, Integer dacVer);

    // save dacDocLis without versionning
    List<DacDocs> createDacDocsList(DacDoccsAndDacRequest dacDocsAndDacRequest)throws MonnaieNotFound;

    // New explicit methods
    DacDocs updateDacDocsWithVersioning(DacDocsRequest request);
    DacDocs updateDacDocsInPlace(DacDocsRequest request);

    // Upload static file and link docId to dac_docs
    DacDocs uploadStaticDoc(Integer dacId, Integer dynaDocsId, MultipartFile file);
}
