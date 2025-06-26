package com.sigmap.common.process_verbal;

import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.com.exception.ComNotFound;
import com.sigmap.common.docs.exeptions.DocNotFound;
import com.sigmap.common.ppms.exception.PpmNotFound;
import com.sigmap.common.process_verbal.exception.ProcessVerbalNotFound;
import com.sigmap.common.process_verbal.request.ProcessVerbalRequest;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import jakarta.transaction.Transactional;
import org.springframework.security.access.method.P;

import java.io.IOException;

public interface ProcessVerbalService {
     ProcessVerbal save(ProcessVerbal processVerbal);
     ProcessVerbal create(ProcessVerbalRequest request) throws PpmNotFound, AcNotFound, ComNotFound, RefParamNotFound, IOException;
    ProcessVerbal findByIds(Integer ppmId,Integer ppmVer) ;
    ProcessVerbal update(ProcessVerbalRequest request) throws ProcessVerbalNotFound, DocNotFound, RefParamNotFound, IOException;
}
