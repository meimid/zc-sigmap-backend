package com.sigmap.common.dyna_docs_copy.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class DynaDocsCopyRequest {
    private Integer dacId;
    private Integer dacVer;
    private Integer dynaDocsId;
    private Integer dynaDocsVer;
    private MultipartFile AAOfile;
    private List<RestOfFiles> files;


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RestOfFiles {
        private MultipartFile file;
        private Integer order;
    }
}
