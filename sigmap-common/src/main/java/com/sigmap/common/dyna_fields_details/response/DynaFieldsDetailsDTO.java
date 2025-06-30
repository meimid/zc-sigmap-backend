package com.sigmap.common.dyna_fields_details.response;

import com.sigmap.common.dyna_fields_details.DynaFieldDetailsType;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.Data;

@Data
public class DynaFieldsDetailsDTO {
    private Integer dynaFieldsDetailsId;
    private Integer dynaDocsId;
    private Integer dynaDocsVer;
    private Integer dynaFieldsId;
    private String columnName;
    private Integer columnPos;
    private DynaFieldDetailsType fieldType;
    private String source;
    private String source_field;
}
