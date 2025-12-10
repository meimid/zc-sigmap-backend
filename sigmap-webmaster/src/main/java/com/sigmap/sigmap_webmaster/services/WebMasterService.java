package com.sigmap.sigmap_webmaster.services;

import com.sigmap.common.AlfrescoConf.DocumentResponseDTO;
import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.agpm.Agpm;
import com.sigmap.common.agpm.exceptions.AgpmNotFound;
import com.sigmap.common.agpm.request.AgpmQuery;
import com.sigmap.common.agpm.response.AgpmWebsiteResponse;
import com.sigmap.common.docs.exeptions.DocsNotFoundException;
import com.sigmap.common.paas.Paa;
import com.sigmap.common.paas.exceptions.PaaNotFound;
import com.sigmap.common.paas.request.PaaQuery;
import com.sigmap.common.paas.response.PaaDetailsWebsite;
import com.sigmap.common.paas.response.PaaWebsiteResponse;
import com.sigmap.common.ppms.Ppm;
import com.sigmap.common.ppms.exception.PpmNotFound;
import com.sigmap.common.ppms.request.PpmQuery;
import com.sigmap.common.ppms.response.PpmDetailsWebsite;
import com.sigmap.common.ppms.response.PpmWebsiteResponse;
import com.sigmap.common.realisations.Realisation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface WebMasterService {

    PpmDetailsWebsite getPpmById(Integer ppmId, Pageable pageable) throws PpmNotFound, AcNotFound;

    PaaDetailsWebsite getPaaById(Integer paaId, Pageable pageable) throws PaaNotFound;

    AgpmWebsiteResponse getAgpmById(Integer agpmId) throws AgpmNotFound, AcNotFound;

    Optional<DocumentResponseDTO> getDocumentInformation(String alfrescoId);

    byte[] downloadDocument(String alfrescoId) throws IOException, DocsNotFoundException;

    Page<PpmWebsiteResponse> findAllPpmForWebsite(Specification<Ppm> spec, Pageable pageable);
    Page<PaaWebsiteResponse> findPaaAllForWebsite(Specification<Paa> spec, Pageable pageable);
    Page<AgpmWebsiteResponse> findAgpmAllForWebsite(Specification <Agpm> spec, Pageable pageable);

}
