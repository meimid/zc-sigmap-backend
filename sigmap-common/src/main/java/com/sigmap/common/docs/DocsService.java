package com.sigmap.common.docs;


import com.sigmap.common.AlfrescoConf.DocumentResponseDTO;
import com.sigmap.common.docs.Docs;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

public interface DocsService {
    /**
     * Retrieves an Docs entity by its ID and version.
     *
     * @param id  The ID of the Docs entity
     * @return An Optional containing the Docs entity if found, or empty if not found
     */
    Docs findById(Integer id);

    /**
     * Soft deletes an Docs entity by its ID and version.
     *
     * @param id  The ID of the Docs entity to delete

     */
    void delete(Integer id) ;

    /**
     * Retrieves an Docs entity by it ID Alfresco .
     *
     * @param alfrescoId The AlfrescoIdof the Docs entity
     * @return An Optional containing the Docs entity if found, or empty if not found
     */
    Optional<Docs> findByAlfescoId(String alfrescoId);


    /**
     * Retrieves List Docs entity by it PaaId and PaaVersion .
     *
     * @param paaId The id Of the Paa and
     *  @param  paaVersion is the version of paa entity
     * @return An Optional containing List Docs entity if found, or empty if not found
     */
    List<Docs> findDocsByPaasIdAndPaasVersion(Integer paaId, Integer paaVersion);

    /**
     * Retrieves List Docs entity by it realisationId and realisationVersion .
     *
     * @param realisationId The id Of the realisation and realisationVersion is the version of
     * @param realisationVersion entity
     * @return An Optional containing List Docs entity if found, or empty if not found
     */
    List<Docs> findDocsByRealisationIdAndRealisationVersion(Integer realisationId, Integer realisationVersion);

    /**
     * Retrieves List Docs entity by it ppmId and ppmVersion .
     *
     * @param ppmId The id Of the Ppm and ppmVersion is the version of
     * @param ppmVersion entity
     * @return An Optional containing List Docs entity if found, or empty if not found
     */
    List<Docs> findDocsByPpmIdAndPpmVersion(Integer ppmId, Integer ppmVersion);

    /**
     * Return the deleted Doc with the given id.
     *
     * @param id the id of the Ac to return
     * @return the Ac with the given id
     */
    Optional<Docs> findDeletedById(Integer id);

    /**
     * Return the Docs with the given specification.
     *
     * @param specification the specification to use
     * @return the Docs with the given specification
     */
    Optional<Docs> findOne(Specification<Docs> specification);
    Optional <DocumentResponseDTO> getDocumentInformation(String alfrescoId);

}
