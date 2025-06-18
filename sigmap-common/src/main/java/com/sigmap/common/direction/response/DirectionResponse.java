package com.sigmap.common.direction.response;

import com.sigmap.common.ac.response.AcResponse;
import com.sigmap.common.ac.response.AcResponseDto;
import com.sigmap.common.response.BaseEntityResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DirectionResponse {
    private Integer id;
    private String libelle;
    private AcResponseDto ac;
    private BaseEntityResponse metadata;
}
