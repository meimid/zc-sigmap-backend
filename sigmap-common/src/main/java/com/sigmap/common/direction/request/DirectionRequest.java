package com.sigmap.common.direction.request;

import com.sigmap.common.ac.request.AcRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DirectionRequest {
    private String libelle;
    Integer acId;
}
