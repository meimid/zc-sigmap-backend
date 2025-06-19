package com.sigmap.common.direction.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sigmap.common.ac.response.AcResponse;
import com.sigmap.common.ac.response.AcResponseDto;
import com.sigmap.common.response.BaseEntityResponse;
import lombok.*;

import java.util.List;

@Data
public class DirectionResponse {
    private Integer id;
    private String libelle;
    private BaseEntityResponse metadata;
}
