package com.sigmap.common.dac_docs;

import com.sigmap.common.dac_docs.request.DacDocsRequest;
import com.sigmap.common.dac_docs.response.customResponceDTO.DacDocResponseDTO;
import com.sigmap.common.dac_docs.response.customResponceDTO.DacResponseDTO;

import java.util.List;
import java.util.Optional;

public interface DacDocsService {
  public DacDocs createDacDocs (DacDocsRequest request);
  public DacDocs updateDacDocs( DacDocsRequest request);
  public Optional<DacDocs> findById(Integer dacId, Integer dacVer, Integer dynaDocsId, Integer dynaDocsVer, Integer dynaFieldsId);
  public List<DacDocs> findByDacIdAndDacVer(Integer dacId, Integer dacVer);
  public List<DacDocs> findOneById(Integer dacId, Integer dacVer);
  List<DacDocResponseDTO> findByDacIdAndVer(Integer dacId, Integer dacVer);
  public List<DacDocs> createDacDocsBatch(List<DacDocsRequest> requests);
}
