package com.sigmap.common.dyna_docs_copy;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DynaDocsCopyService {

    public DynaDocsCopy save(DynaDocsCopy dynaDocsCopy);

    public List<DynaDocsCopy> findActiveLastVersion(Integer dacId, Integer dacVer);

    public void uploadModifiedFiles(Integer id, MultipartFile file);

    public DynaDocsCopy findById(Integer id);

    public byte[] downloadModifiedFile(Integer id);
}
