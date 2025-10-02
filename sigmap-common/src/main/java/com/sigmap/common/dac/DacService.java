package com.sigmap.common.dac;

import com.sigmap.common.ModeDePassationSelection.exception.ModeDePassationSelectionNotFound;
import com.sigmap.common.dac.exception.NoActiveAAOException;
import com.sigmap.common.agpm.Agpm;
import com.sigmap.common.agpm.exceptions.AgpmNotFound;
import com.sigmap.common.dac.exeption.DacNotFound;
import com.sigmap.common.dac.request.CreateDacRequest;
import com.sigmap.common.dac_docs.response.customResponceDTO.DacResponseDTO;
import com.sigmap.common.entity.ProcessStatus;
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
    Dac create(CreateDacRequest request) throws RefParamNotFound, RealisationNotFoundException, ModeDePassationSelectionNotFound, NoActiveAAOException;
    List<String> generateListDocumentsWithBookmarks(Integer dacId, Integer dacVersion , String basePath, String customDirPath) ;

    String generateDocumentWithBookmarks(Integer dacId, Integer dacVersion, Integer dynaDocsId, Integer dynaDocsVer, String basePath, String customDirPath);

    /**
     * Create a new DAC version synchronized to the latest active DynaDocs (and their fields) for the DAC's category.
     * Copies over existing field values by matching dynaFieldsId when possible.
     *
     * @param dacId the dac id
     * @param dacVer the dac version to base from
     * @return response describing the new DAC version and its documents
     */
    DacResponseDTO synchronizeToLatestDynaDocs(Integer dacId, Integer dacVer);

    Page<Dac> findAllLatest(org.springframework.data.domain.Pageable pageable);
    Dac updateStatus(Integer dacId, Integer dacVer, ProcessStatus status) throws DacNotFound;
}
