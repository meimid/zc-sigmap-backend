package com.sigmap.common.modified_dyna_docs_for_dac;

import com.sigmap.common.modified_dyna_docs_for_dac.request.ModifiedDynaDocsRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ModifiedDynaDocsService {

    public void uploadModifiedFiles(ModifiedDynaDocsRequest request, MultipartFile AAOfile, List<MultipartFile> files);
}
