package com.sigmap.common.user_historique_ac.request;

import com.sigmap.common.user_historique_ac.UserAcHistorique;
import com.sigmap.common.user_historique_ac.UserAcHistoriqueSpecifocations;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;



@Data
public class UserAcHistoriqueQuery {

    private String userId;
    private String status;
    private Integer acId;
    private String date;

    public Specification<UserAcHistorique> toSpecification() {
        Specification<UserAcHistorique> spec = Specification.where(null);

        if (userId != null && !userId.isBlank()) {
            spec = spec.and(UserAcHistoriqueSpecifocations.userId(userId));
        }

        if (acId != null) {
            spec = spec.and(UserAcHistoriqueSpecifocations.acId(acId));
        }

        if (status != null && !status.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        return spec;
    }
}
