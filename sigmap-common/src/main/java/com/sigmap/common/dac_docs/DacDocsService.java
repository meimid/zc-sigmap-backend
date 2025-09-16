package com.sigmap.common.dac_docs;

import com.sigmap.common.dac_docs.request.DacDocsRequest;
import com.sigmap.common.dac_docs.response.customResponceDTO.DacDocResponseDTO;

import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

public interface DacDocsService {
    DacDocs createDacDocs(DacDocsRequest request);
    DacDocs updateDacDocs(DacDocsRequest request);
    List<DacDocs> createDacDocsBatch(List<DacDocsRequest> requests);

    List<DacDocs> findOneById(Integer dacId, Integer dacVer);
    Optional<DacDocs> findById(Integer dacId, Integer dacVer, Integer dynaDocsId, Integer dynaDocsVer, Integer dynaFieldsId);
    List<DacDocResponseDTO> findByDacIdAndVer(Integer dacId, Integer dacVer);
    List<DacDocs> findByDacIdAndDacVer(Integer dacId, Integer dacVer);

    // save dacDocLis without versionning
    List<DacDocs> createDacDocsList(List<DacDocsRequest> requests);

    // New explicit methods
    DacDocs updateDacDocsWithVersioning(DacDocsRequest request);
    DacDocs updateDacDocsInPlace(DacDocsRequest request);
}
