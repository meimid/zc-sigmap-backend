package com.sigmap.common.process_verbal_reunion.request;


import lombok.Data;
import org.springframework.web.multipart.MultipartFile;


@Data
public class ProcessVerbalReunionRequest {
    private String observation;
    private MultipartFile file;

}
