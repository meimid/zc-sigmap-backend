package com.sigmap.common.dyna_fields.response;

import com.sigmap.common.dyna_docs.DynaDocs;
import com.sigmap.common.dyna_fields.DynaFieldType;
import com.sigmap.common.dyna_fields_details.DynaFieldsDetails;
import com.sigmap.common.dyna_fields_details.response.DynaFieldsDetailsDTO;
import com.sigmap.common.entity.ActiveStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class DynaFieldsDTO {
    private Integer dynaFieldsId;
    private Integer dynaDocsId;
    private Integer dynaDocsVer;
    private String name;
    private String ref;
    private String label;
    private String description;
    private DynaFieldType type;
    private String source;
    private String sourceField;
    private Integer dynaFieldsOrder;
    private ActiveStatus status;
    //private DynaDocs dynaDocs;
    private Set<DynaFieldsDetailsDTO> dynaFieldsDetails;
}
