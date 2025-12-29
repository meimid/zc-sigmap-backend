package com.sigmap.common.model_dac;

import com.sigmap.common.dac.exeption.DacNotFound;
import com.sigmap.common.docs.exeptions.DocNotFound;

import java.util.Optional;

public interface ModelDacService {

    ModelDac setModel(Integer dacId, Integer dacVer, Integer docId ,Boolean isAao) throws DacNotFound, DocNotFound;

    Optional<ModelDac> getModel(Integer dacId, Integer dacVer,Boolean isAao) throws DacNotFound, DocNotFound;

    void removeModel(Integer dacId, Integer dacVer,Boolean isAao);

    boolean isDocumentRelatedToDac(String alfrescoId);
    boolean isDocumentRelatedToDacShortlistForUser(String alfrescoId, String userId);
}
