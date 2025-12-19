package com.sigmap.common.dac;

import com.sigmap.common.ModeDePassationSelection.exception.ModeDePassationSelectionNotFound;
import com.sigmap.common.appel_offre.exception.AppelOffreNotFound;
import com.sigmap.common.dac.exeption.NoActiveAAOException;
import com.sigmap.common.dac.exeption.DacNotFound;
import com.sigmap.common.dac.exeption.InvalidDacRequestException;
import com.sigmap.common.dac.request.CreateDacFromAoRequest;
import com.sigmap.common.dac.request.CreateDacRequest;
import com.sigmap.common.dac.request.DacQuery;
import com.sigmap.common.dac.response.DacWithAoReferenceResponse;
import com.sigmap.common.dac.response.DacWebsiteResponse;
import com.sigmap.common.dac_docs.response.customResponceDTO.DacResponseDTO;
import com.sigmap.common.entity.ProcessStatus;
import com.sigmap.common.realisations.exceptions.RealisationNotFoundException;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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

    Page<Dac> findAllLatestWithQuery(DacQuery query, org.springframework.data.domain.Pageable pageable);
    Dac updateStatus(Integer dacId, Integer dacVer, ProcessStatus status) throws DacNotFound;
    Integer findLatestVersionById(Integer ppmId);

    public String generateDAODocumentWithBookmarks(Integer dacId, Integer dacVersion, String signataire, String fonction, String basePath, String customDirPath);

    /**
     * Find all published DACs for a given realisation ID and version
     * @param realisationId The ID of the realisation
     * @param realisationVer The version of the realisation
     * @return List of published DACs for the given realisation
     */
    List<Dac> findPublishedDacsByRealisationIdAndVersion(Integer realisationId, Integer realisationVer);

    /**
     * Create DAC(s) from an existing Appel d'Offre.
     * Based on etapeDac ("1" or "2"), creates one or two DACs.
     * Validates request and populates reference data from AO/PPM/Realisation.
     *
     * @param request The DAC creation request containing AO reference and user inputs
     * @return List of created DACs
     * @throws AppelOffreNotFound if the specified AO doesn't exist
     * @throws InvalidDacRequestException if validation fails
     */
    Dac createFromAppelOffre(CreateDacFromAoRequest request)
            throws AppelOffreNotFound, InvalidDacRequestException;

    /**
     * Get DAC with full reference data from associated AO, PPM, and Realisation.
     *
     * @param dacId The DAC ID
     * @param dacVer The DAC version
     * @return DAC with populated reference data
     * @throws DacNotFound if DAC doesn't exist
     */
    DacWithAoReferenceResponse getDacWithAoReference(Integer dacId, Integer dacVer) 
            throws DacNotFound;

    /**
     * Find all published DACs for the website.
     * @param pageable Pagination information
     * @return Page of DacWebsiteResponse
     */
    Page<DacWebsiteResponse> findPublishedDacsForWebsite(Pageable pageable);

    /**
     * Find published DACs for a specific user based on their associated societies.
     * Returns only DACs where shortListing=true or etapeDac='2', and only the latest version.
     * 
     * @param userId The authenticated user ID
     * @param pageable Pagination information
     * @return Page of published DACs for the user
     */
    Page<com.sigmap.common.dac.response.DacWebsiteResponse> findPublishedDacsForUser(String userId, Pageable pageable);

}
