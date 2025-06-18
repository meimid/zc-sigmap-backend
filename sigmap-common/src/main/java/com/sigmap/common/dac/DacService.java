package com.sigmap.common.dac;

import com.sigmap.common.dac.request.CreateDacRequest;
import com.sigmap.common.realisations.exceptions.RealisationNotFoundException;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import com.sigmap.common.realisations.Realisation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;
import java.util.List;

public interface DacService {

    List<Dac> findAll();
    Page<Dac> findAll(Specification<Dac> spec, Pageable pageable);
    Dac save(Dac dac);
    Dac create(CreateDacRequest request) throws RefParamNotFound, RealisationNotFoundException;
    List<String> generateListDocumentsWithBookmarks(Integer dacId, Integer dacVersion , String basePath, String customDirPath) ;

    String generateDocumentWithBookmarks(Integer dacId, Integer dacVersion, Integer dynaDocsId, Integer dynaDocsVer, String basePath, String customDirPath);
    
    /**
     * Récupère les réalisations qui ne sont pas liées à des DAC
     * @param pageable paramètres de pagination
     * @return page de réalisations non liées à des DAC
     */
    Page<Realisation> findRealisationsWithoutDac(Pageable pageable);
}
