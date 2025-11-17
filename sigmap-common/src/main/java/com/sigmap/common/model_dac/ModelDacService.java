package com.sigmap.common.model_dac;

import com.sigmap.common.dac.exeption.DacNotFound;
import com.sigmap.common.docs.exeptions.DocNotFound;

import java.util.Optional;

public interface ModelDacService {

    ModelDac setModel(Integer dacId, Integer dacVer, Integer docId) throws DacNotFound, DocNotFound;

    Optional<ModelDac> getModel(Integer dacId, Integer dacVer);

    void removeModel(Integer dacId, Integer dacVer);
}
