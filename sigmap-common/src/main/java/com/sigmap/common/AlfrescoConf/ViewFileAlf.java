package com.sigmap.common.AlfrescoConf;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ViewFileAlf {
    private String documentId;
    private String name;
    private String type;
    private byte[] data;
    private String documentSize;
}
