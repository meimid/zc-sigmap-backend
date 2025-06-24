package com.sigmap.common.process_verbal;

import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.com.exception.ComNotFound;
import com.sigmap.common.ppms.exception.PpmNotFound;
import com.sigmap.common.process_verbal.request.ProcessVerbalRequest;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import org.springframework.security.core.parameters.P;

import java.io.IOException;

public interface ProcessVerbalService {
    public ProcessVerbal save(ProcessVerbal processVerbal);
    public ProcessVerbal create(ProcessVerbalRequest request) throws PpmNotFound, AcNotFound, ComNotFound, RefParamNotFound, IOException;
}
