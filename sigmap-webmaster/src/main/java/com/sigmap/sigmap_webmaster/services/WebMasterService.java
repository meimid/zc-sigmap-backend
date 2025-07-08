package com.sigmap.sigmap_webmaster.services;

import com.sigmap.common.agpm.Agpm;
import com.sigmap.common.agpm.exceptions.AgpmNotFound;
import com.sigmap.common.paas.Paa;
import com.sigmap.common.paas.exceptions.PaaNotFound;
import com.sigmap.common.ppms.Ppm;
import com.sigmap.common.ppms.exception.PpmNotFound;

public interface WebMasterService {

    public Ppm getPpmByIdAndVersion(Integer ppmId, Integer ppmVer) throws PpmNotFound;
    public Paa getPaaByIdAndVersion(Integer paaId, Integer paaVer) throws PaaNotFound;
    public Agpm getAgpmByIdAndVersion (Integer agpmId, Integer agpmVer) throws AgpmNotFound;
}
