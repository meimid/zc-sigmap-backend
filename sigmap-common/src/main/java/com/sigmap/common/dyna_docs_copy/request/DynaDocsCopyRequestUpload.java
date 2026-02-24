package com.sigmap.common.dyna_docs_copy.request;


import com.sigmap.common.entity.ActiveStatus;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Getter
@Setter
public class DynaDocsCopyRequestUpload {

    private Integer id;
    private Integer version;
    private String name;
    private String templateFileName;
    private ActiveStatus status;
    private Boolean isAAO;
    private Integer ordre;
    private Integer dacId;
    private Integer dacVer;
    private Integer dynaDocsId;
    private Integer dynaDocsVer;
    private MultipartFile file;

}
