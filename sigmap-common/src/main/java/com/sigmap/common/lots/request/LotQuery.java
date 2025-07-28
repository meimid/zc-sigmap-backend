package com.sigmap.common.lots.request;

import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.lots.Lot;
import com.sigmap.common.lots.LotSpecifications;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;
import com.sigmap.common.lots.LotService;

import java.math.BigDecimal;

@Data
public class LotQuery {
    private String search;
    private CompositeId compositeId; // Corresponds to lots_id and lots_ver
    private String description;
    private boolean deleted;
    private BigDecimal budget;

    /**
     * Creates a {@link Specification} from the properties of the current object.
     * <p>
     * The resulting {@link Specification} will be used to query the database. The
     * specification will be created by combining the following JPA specifications:
     * <ul>
     * <li>{@link LotSpecifications#search(String)} if the search property is not null or blank.</li>
     * <li>{@link LotSpecifications#id(CompositeId)} if the compositeId property is not null.</li>
     * <li>{@link LotSpecifications#description(String)} if the description property is not null or blank.</li>
     * <li>{@link LotSpecifications#deleted(boolean)} if the deleted property is true.</li>
     * <li>{@link LotSpecifications#budget(BigDecimal)} if the budget property is not null.</li>
     * </ul>
     * <p>
     * The specifications will be combined with the {@link Specification#and(Specification)} method.
     *
     * @see LotSpecifications
     * @see LotService
     * @return a {@link Specification} that can be used to query the database.
     */
    public Specification<Lot> toSpecification() {
        Specification<Lot> spec = Specification.where(LotSpecifications.deleted(deleted));

        if (search != null && !search.isBlank()) {
            spec = spec.and(LotSpecifications.search(search));
        }

        if (compositeId != null) {
            spec = spec.and(LotSpecifications.id(compositeId));
        }

        if (description != null && !description.isBlank()) {
            spec = spec.and(LotSpecifications.description(description));
        }

        if (budget != null) {
            spec = spec.and(LotSpecifications.budget(budget));
        }

        return spec;
    }
}