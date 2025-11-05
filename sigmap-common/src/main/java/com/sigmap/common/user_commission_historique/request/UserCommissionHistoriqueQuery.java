package com.sigmap.common.user_commission_historique.request;

import com.sigmap.common.entity.ActiveStatus;
import com.sigmap.common.user_commission_historique.UserCommissionHistorique;
import com.sigmap.common.user_commission_historique.UserCommissionHistoriqueSpecifications;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class UserCommissionHistoriqueQuery {

    private String search;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private LocalDate exactDate;

    /**
     * Creates a {@link Specification} for {@link UserCommissionHistorique}
     * by combining various filters based on the provided query parameters.
     *
     * <ul>
     *   <li>{@link UserCommissionHistoriqueSpecifications#search(String)} — global text search</li>
     *   <li>{@link UserCommissionHistoriqueSpecifications#hasUserId(String)} — filter by user id</li>
     *   <li>{@link UserCommissionHistoriqueSpecifications#hasComId(Integer)} — filter by commission id</li>
     *   <li>{@link UserCommissionHistoriqueSpecifications#dateDebutAfter(LocalDateTime)} — start date</li>
     *   <li>{@link UserCommissionHistoriqueSpecifications#dateFinBefore(LocalDateTime)} — end date</li>
     * </ul>
     *
     * All specifications are combined using {@link Specification#and(Specification)}.
     */
    public Specification<UserCommissionHistorique> toSpecification() {
        Specification<UserCommissionHistorique> spec = Specification.where(null);

        if (search != null && !search.isBlank()) {
            spec = spec.and(UserCommissionHistoriqueSpecifications.search(search));
        }


        if (dateDebut != null) {
            spec = spec.and(UserCommissionHistoriqueSpecifications.dateDebutAfter(dateDebut));
        }

        if (dateFin != null) {
            spec = spec.and(UserCommissionHistoriqueSpecifications.dateFinBefore(dateFin));
        }
        if (exactDate != null) {
            spec = spec.and(UserCommissionHistoriqueSpecifications.hasExactDate(exactDate));
        }
        return spec;
    }
}
