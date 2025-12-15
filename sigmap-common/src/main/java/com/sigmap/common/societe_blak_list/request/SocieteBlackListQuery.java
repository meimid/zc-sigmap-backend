package com.sigmap.common.societe_blak_list.request;

import com.sigmap.common.config.BlackList;
import com.sigmap.common.societe_blak_list.SocieteBlackList;
import com.sigmap.common.societe_blak_list.SocieteBlackListSpecifications;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SocieteBlackListQuery {

    private String search;
    private Integer societeId;
    private BlackList natureDecision;
    private LocalDateTime dateEffet;
    private LocalDateTime delai;
    private String nif;
    private String reference;
    private String paysCode;
    private String paysDescription;
    private Boolean deleted = false;

    /**
     * Creates a {@link Specification} from the properties of the current query object.
     *
     * The resulting specification will be used to filter black-listed sociétés.
     * Each non-null field will append a new condition to the final specification.
     *
     * Combines the following:
     * - Global search (search in ALL blacklist + societe + adresse + refParam fields)
     * - Filtering by societeId
     * - Filtering by nature decision
     * - Filtering by dates (dateEffet / delai)
     * - Filtering by deleted flag
     *
     * @return Specification<SocieteBlackList> used to query database.
     */
    public Specification<SocieteBlackList> toSpecification() {

        // Start with deleted flag
        Specification<SocieteBlackList> spec =
                Specification.where(SocieteBlackListSpecifications.deleted(deleted));


        if (search != null && !search.isBlank()) {
            spec = spec.and(SocieteBlackListSpecifications.search(search));
        }

        if (societeId != null) {
            spec = spec.and(SocieteBlackListSpecifications.societeId(societeId));
        }


        if (natureDecision != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("natureDecision"), natureDecision)
            );
        }

        if (dateEffet != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("dateEffet"), dateEffet)
            );
        }

        if (dateEffet != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("delai"), dateEffet)
            );
        }


        if (nif != null && !nif.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(root.join("societe").get("nif"), "%" + nif + "%")
            );
        }

        if (paysCode != null && !paysCode.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(
                            root.join("societe")
                                    .join("address")
                                    .join("pays")
                                    .get("code"),
                            "%" + paysCode + "%"
                    )
            );
        }

        // COUNTRY BY DESCRIPTION (NEW)
        if (paysDescription != null && !paysDescription.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(
                            root.join("societe")
                                    .join("address")
                                    .join("pays")
                                    .get("description"),
                            "%" + paysDescription + "%"
                    )
            );
        }
        return spec;
    }
}
