package com.sigmap.sigmap_webmaster.services;

import com.sigmap.common.agpm.Agpm;
import com.sigmap.common.agpm.exceptions.AgpmNotFound;
import com.sigmap.common.paas.exceptions.PaaNotFound;
import com.sigmap.common.ppms.exception.PpmNotFound;
import com.sigmap.common.realisations.Realisation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface WebMasterService {

    public Page<Realisation> getPpmByIdAndVersion(Integer ppmId, Integer ppmVer,Pageable pageable) throws PpmNotFound;
    // New signature in your service interface/class
    Page<Realisation> getPaaByIdAndVersion(Integer paaId, Integer paaVer, Pageable pageable)throws PaaNotFound;
    public Agpm getAgpmByIdAndVersion (Integer agpmId, Integer agpmVer) throws AgpmNotFound;
}
