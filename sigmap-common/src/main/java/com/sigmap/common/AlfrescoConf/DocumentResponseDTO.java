package com.sigmap.common.AlfrescoConf;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DocumentResponseDTO {
    private byte[] file;
    private boolean erreur;
    private String msg;
    private String documentId;

}
