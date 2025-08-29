package com.sigmap.common.model_ppm;

import com.sigmap.common.docs.exeptions.DocNotFound;
import com.sigmap.common.ppms.exception.PpmNotFound;

import java.util.Optional;

public interface ModelPpmService {

    ModelPpm setModel(Integer ppmId, Integer ppmVer, Integer docId) throws PpmNotFound , DocNotFound;

    Optional<ModelPpm> getModel(Integer ppmId, Integer ppmVer);

    void removeModel(Integer ppmId, Integer ppmVer);
}
