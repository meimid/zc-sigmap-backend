package com.sigmap.common.ac.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sigmap.common.com.Com;
import com.sigmap.common.com.response.ComResponse;
import com.sigmap.common.com.response.ComResponseDto;
import com.sigmap.common.response.BaseEntityResponse;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AcResponse {
    private Integer id;
    private String nom;
    private String ancrage;
    private String contact;
    private List<ComResponseDto> coms;
    private BaseEntityResponse metadata;

}
