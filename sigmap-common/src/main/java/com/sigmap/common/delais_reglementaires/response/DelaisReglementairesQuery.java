package com.sigmap.common.delais_reglementaires.response;

import com.sigmap.common.delais_reglementaires.DelaisReglementaires;
import com.sigmap.common.delais_reglementaires.DelaisReglementairesSpecifications;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

@Data
public class DelaisReglementairesQuery {
    private Integer typeMarcherId;
    private Integer modePassationId;
    private Integer delaiPublicationLancementMin;
    private Integer delaiPublicationLancementMax;
    private Integer delaiLancementAttributionMin;
    private Integer delaiLancementAttributionMax;
    private Integer delaiAttributionDemarrageMin;
    private Integer delaiAttributionDemarrageMax;
    private boolean deleted;
    private String search;

    public Specification<DelaisReglementaires> toSpecification() {
        Specification<DelaisReglementaires> spec = Specification.where(DelaisReglementairesSpecifications.deleted(deleted));

        if (search != null && !search.isBlank()) {
            spec = spec.and(DelaisReglementairesSpecifications.search(search));
        }

        if (typeMarcherId != null) {
            spec = spec.and(DelaisReglementairesSpecifications.typeMarcherId(typeMarcherId));
        }

        if (modePassationId != null) {
            spec = spec.and(DelaisReglementairesSpecifications.modePassationId(modePassationId));
        }

        if (delaiPublicationLancementMin != null) {
            spec = spec.and(DelaisReglementairesSpecifications.delaiPublicationLancementMin(delaiPublicationLancementMin));
        }

        if (delaiPublicationLancementMax != null) {
            spec = spec.and(DelaisReglementairesSpecifications.delaiPublicationLancementMax(delaiPublicationLancementMax));
        }

        if (delaiLancementAttributionMin != null) {
            spec = spec.and(DelaisReglementairesSpecifications.delaiLancementAttributionMin(delaiLancementAttributionMin));
        }

        if (delaiLancementAttributionMax != null) {
            spec = spec.and(DelaisReglementairesSpecifications.delaiLancementAttributionMax(delaiLancementAttributionMax));
        }

        if (delaiAttributionDemarrageMin != null) {
            spec = spec.and(DelaisReglementairesSpecifications.delaiAttributionDemarrageMin(delaiAttributionDemarrageMin));
        }

        if (delaiAttributionDemarrageMax != null) {
            spec = spec.and(DelaisReglementairesSpecifications.delaiAttributionDemarrageMax(delaiAttributionDemarrageMax));
        }

        return spec;
    }
} 