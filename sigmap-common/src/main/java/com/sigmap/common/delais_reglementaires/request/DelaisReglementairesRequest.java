package com.sigmap.common.delais_reglementaires.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DelaisReglementairesRequest {

    private Integer typeMarcherId;
    private Integer modePassationId;
    private Integer delaiPublicationLancement;
    private Integer delaiLancementAttribution;
    private Integer delaiAttributionDemarrage;
}
