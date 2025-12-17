package com.sigmap.common.dac.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateDacFromAoRequest {

    @NotNull(message = "Appel d'Offre ID is required")
    private Integer appelOffreId;

    private Integer dynaDocsModelId;
}
