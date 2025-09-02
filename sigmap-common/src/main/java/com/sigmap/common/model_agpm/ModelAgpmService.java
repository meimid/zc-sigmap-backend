package com.sigmap.common.model_agpm;

import com.sigmap.common.agpm.exceptions.AgpmNotFound;
import com.sigmap.common.docs.exeptions.DocNotFound;
import com.sigmap.common.paas.exceptions.PaaNotFound;
import com.sigmap.common.ppms.exception.PpmNotFound;

import java.util.Optional;

public interface ModelAgpmService {

    ModelAgpm setModel(Integer agpmId, Integer agpmVer, Integer docId) throws  DocNotFound, AgpmNotFound;

    Optional<ModelAgpm> getModel(Integer agpmId, Integer agpmVer);

    void removeModel(Integer agpmId, Integer agpmVer);
}
