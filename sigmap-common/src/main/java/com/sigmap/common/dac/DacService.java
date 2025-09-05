package com.sigmap.common.dac;

import com.sigmap.common.dac.request.CreateDacRequest;
import com.sigmap.common.dac_docs.response.customResponceDTO.DacResponseDTO;
import com.sigmap.common.realisations.exceptions.RealisationNotFoundException;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import com.sigmap.common.realisations.Realisation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DacService {

    List<Dac> findAll();
    Page<Dac> findAll(Specification<Dac> spec, Pageable pageable);
    Optional<Dac> findById(Integer dacId, Integer dacVer);
    public Optional<Dac> findOne(Specification<Dac> specification);
    public DacResponseDTO findByDacIdAndVer(Integer dacId, Integer dacVer);
    Dac save(Dac dac);
    Dac create(CreateDacRequest request) throws RefParamNotFound, RealisationNotFoundException;
    List<String> generateListDocumentsWithBookmarks(Integer dacId, Integer dacVersion , String basePath, String customDirPath) ;

    String generateDocumentWithBookmarks(Integer dacId, Integer dacVersion, Integer dynaDocsId, Integer dynaDocsVer, String basePath, String customDirPath);

}
