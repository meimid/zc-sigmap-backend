package com.sigmap.common.reunion.response;

import com.sigmap.common.config.ReunionStatus;
import com.sigmap.common.response.BaseEntityResponse;
import lombok.Data;

import java.util.Calendar;
@Data
public class ReunionSearchResponseDto {
    private Integer id;
    private Integer reunionNumCom;
    private String comName;
    private Calendar reunionDate;
    private Integer year;
    private String emplacement;
    private String object;
    private ReunionStatus status;
    private String decision;
    private String note;
    boolean haveExtraits = false;
    private BaseEntityResponse metadata;
}
