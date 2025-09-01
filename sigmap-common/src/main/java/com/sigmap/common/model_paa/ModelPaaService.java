package com.sigmap.common.model_paa;

import com.sigmap.common.docs.exeptions.DocNotFound;
import com.sigmap.common.paas.exceptions.PaaNotFound;
import com.sigmap.common.ppms.exception.PpmNotFound;

import java.util.Optional;

public interface ModelPaaService {

    ModelPaa setModel(Integer paaId, Integer paaVer, Integer docId) throws PaaNotFound, DocNotFound, PpmNotFound;

    Optional<ModelPaa> getModel(Integer paaId, Integer paaVer);

    void removeModel(Integer paaId, Integer paaVer);
}
