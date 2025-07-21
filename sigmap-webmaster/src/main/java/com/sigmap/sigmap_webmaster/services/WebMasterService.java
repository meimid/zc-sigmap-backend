package com.sigmap.sigmap_webmaster.services;

import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.agpm.Agpm;
import com.sigmap.common.agpm.exceptions.AgpmNotFound;
import com.sigmap.common.paas.exceptions.PaaNotFound;
import com.sigmap.common.ppms.exception.PpmNotFound;
import com.sigmap.common.realisations.Realisation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface WebMasterService {

    public Page<Realisation> getPpmById(Integer ppmId, Pageable pageable) throws PpmNotFound, AcNotFound;
    Page<Realisation> getPaaById(Integer paaId,  Pageable pageable)throws PaaNotFound;
    public Agpm getAgpmById (Integer agpmId) throws AgpmNotFound, AcNotFound;
}
