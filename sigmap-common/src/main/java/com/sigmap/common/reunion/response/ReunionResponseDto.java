package com.sigmap.common.reunion.response;

import com.sigmap.common.com.response.ComResponseDto;
import com.sigmap.common.config.ReunionStatus;
import com.sigmap.common.response.BaseEntityResponse;
import lombok.Data;

import java.util.Calendar;

@Data
public class ReunionResponseDto {
    private Integer id;
    private String comName;
    private Calendar reunionDate;
    private String emplacement;
    private String object;
    private ReunionStatus status;
    private String note;
    private BaseEntityResponse metadata;

}
