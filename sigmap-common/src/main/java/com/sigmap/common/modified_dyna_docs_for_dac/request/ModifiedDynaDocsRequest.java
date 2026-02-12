package com.sigmap.common.modified_dyna_docs_for_dac.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ModifiedDynaDocsRequest {
    private MultipartFile AAOfile;
    private List<RestFiles> files;
    private Integer dacId;
    private Integer dacVer;

    @Data
    public static class RestFiles {
        private MultipartFile file;
        private Integer order;
    }
}
