package com.sigmap.common.process_verbal_reunion_extrait;

import com.sigmap.common.process_verbal_reunion_extrait.request.ProcessVerbalReunionExtraitRequest;

import java.io.IOException;
import java.util.Optional;

public interface ProcessVerbalReunionExtraitService {

    ProcessVerbalReunionExtrait create(ProcessVerbalReunionExtraitRequest extrait) throws IOException;
    ProcessVerbalReunionExtrait findByIds(ProcessVerbalReunionExtraitRequest request);
    ProcessVerbalReunionExtrait update( ProcessVerbalReunionExtraitRequest request) throws IOException;

}
