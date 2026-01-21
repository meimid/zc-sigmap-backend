package com.sigmap.common.reunion.response;

import lombok.Data;

import java.util.Calendar;

@Data
public class ReunionDropListDto {
    private Integer id;
    private Integer reunionNumCom;
    private String emplacement;
    private String object;
    private Calendar reunionDate;
    private Integer year;

}
