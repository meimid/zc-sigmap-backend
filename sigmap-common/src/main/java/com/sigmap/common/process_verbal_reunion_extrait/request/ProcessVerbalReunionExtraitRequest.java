package com.sigmap.common.process_verbal_reunion_extrait.request;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProcessVerbalReunionExtraitRequest {
    private Integer extraitId;
    private Integer acId;
    private MultipartFile file;
}
