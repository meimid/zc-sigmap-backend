package com.sigmap.common.dac;

import com.sigmap.common.dac.request.CreateDacRequest;

import java.util.List;

public interface DacService {

    Dac save(Dac dac);
    Dac create(CreateDacRequest request);
    List<String> generateListDocumentsWithBookmarks(Integer dacId, Integer dacVersion , String basePath, String customDirPath) ;

    String generateDocumentWithBookmarks(Integer dacId, Integer dacVersion, Integer dynaDocsId, Integer dynaDocsVer, String basePath, String customDirPath);
}
