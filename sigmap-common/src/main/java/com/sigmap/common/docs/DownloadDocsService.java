package com.sigmap.common.docs;

import com.sigmap.common.agpm.exceptions.AgpmNotFound;
import com.sigmap.common.docs.exeptions.DocsNotFoundException;
import com.sigmap.common.paas.exceptions.PaaNotFound;
import com.sigmap.common.ppms.exception.PpmNotFound;
import com.sigmap.common.process_verbal_reunion_extrait.excepton.ExtraitNotFound;
import com.sigmap.common.reunion.exception.ReunionNotFound;

import java.io.IOException;

public interface DownloadDocsService {
    byte[] downloadModelPaa(String alfrescoId) throws IOException, DocsNotFoundException, PaaNotFound;
    byte[] downloadModelAgpm(String alfrescoId) throws IOException, DocsNotFoundException, AgpmNotFound;
    byte[] downloadPv(String alfrescoId) throws IOException, DocsNotFoundException;
    byte[] downloadExtrait(String alfrescoId) throws IOException, DocsNotFoundException, ReunionNotFound, ExtraitNotFound;
    byte[] downloadPpmArgumentDocument(String alfrescoId) throws IOException, DocsNotFoundException;
    byte[] downloadModelPpm(String alfrescoId) throws IOException, DocsNotFoundException, PpmNotFound;

}
