package com.sigmap.common.process_verbal;

import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.com.exception.ComNotFound;
import com.sigmap.common.docs.exeptions.DocNotFound;
import com.sigmap.common.ppms.exception.PpmNotFound;
import com.sigmap.common.process_verbal.exception.ProcessVerbalNotFound;
import com.sigmap.common.process_verbal.request.ProcessVerbalRequest;
import com.sigmap.common.process_verbal.request.PvRequest;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import jakarta.transaction.Transactional;
import org.springframework.security.access.method.P;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ProcessVerbalService {
     ProcessVerbal save(ProcessVerbal processVerbal);
     ProcessVerbal create(ProcessVerbalRequest request) throws PpmNotFound, AcNotFound, ComNotFound, RefParamNotFound, IOException;
    ProcessVerbal findByIds(Integer ppmId,Integer ppmVer) ;
    ProcessVerbal update(ProcessVerbalRequest request) throws ProcessVerbalNotFound, DocNotFound, RefParamNotFound, IOException;
    ProcessVerbal findByIdsAndTaskIdAndProcessInstanceId(Integer ppmId,Integer ppmVer,String taskId,String processInstanceId) ;
    ProcessVerbal addNewPv(PvRequest request) throws ProcessVerbalNotFound, IOException;

}
