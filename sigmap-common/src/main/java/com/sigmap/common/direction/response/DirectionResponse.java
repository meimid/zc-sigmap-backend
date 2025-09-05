package com.sigmap.common.direction.response;


import com.sigmap.common.response.BaseEntityResponse;
import lombok.*;

@Data
public class DirectionResponse {
    private Integer id;
    private String libelle;
    private BaseEntityResponse metadata;
}
