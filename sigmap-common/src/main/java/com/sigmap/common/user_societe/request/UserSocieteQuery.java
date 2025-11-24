package com.sigmap.common.user_societe.request;

import com.sigmap.common.entity.ActiveStatus;
import com.sigmap.common.user_societe.UserSociete;
import com.sigmap.common.user_societe.UserSocieteSpecifications;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.Specification;

/**
 * Query request object used to build {@link Specification} for {@link UserSociete}.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserSocieteQuery {

    private String search;
    private String userId;
    private Integer societeId;
    private Boolean responsable;
    private ActiveStatus status;
    private boolean deleted;

    /**
     * Creates a {@link Specification} from the properties of this query.
     *
     * The resulting {@link Specification} will be composed of:
     * <ul>
     *     <li>{@link UserSocieteSpecifications#search(String)} if search is not null or blank</li>
     *     <li>{@link UserSocieteSpecifications#userId(String)} if userId is not null or blank</li>
     *     <li>{@link UserSocieteSpecifications#societeId(Integer)} if societeId is not null</li>
     *     <li>{@link UserSocieteSpecifications#responsable(Boolean)} if responsable is not null</li>
     *     <li>{@link UserSocieteSpecifications#status(ActiveStatus)} if status is not null</li>
     *     <li>{@link UserSocieteSpecifications#deleted(boolean)} always applied</li>
     * </ul>
     *
     * @return a {@link Specification<UserSociete>} for filtering query results.
     */
    public Specification<UserSociete> toSpecification() {
        Specification<UserSociete> spec = Specification.where(UserSocieteSpecifications.deleted(deleted));

        if (search != null && !search.isBlank()) {
            spec = spec.and(UserSocieteSpecifications.search(search));
        }

        if (userId != null && !userId.isBlank()) {
            spec = spec.and(UserSocieteSpecifications.userId(userId));
        }

        if (societeId != null) {
            spec = spec.and(UserSocieteSpecifications.societeId(societeId));
        }

        if (responsable != null) {
            spec = spec.and(UserSocieteSpecifications.responsable(responsable));
        }

        if (status != null) {
            spec = spec.and(UserSocieteSpecifications.status(status));
        }

        return spec;
    }
}