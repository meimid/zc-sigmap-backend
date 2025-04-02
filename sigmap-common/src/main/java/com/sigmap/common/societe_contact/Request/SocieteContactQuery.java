package com.sigmap.common.societe_contact.Request;

import com.sigmap.common.societe.SocieteSpecifications;
import com.sigmap.common.societe_contact.SocieteContact;
import com.sigmap.common.societe_contact.SocieteContactSpecifications;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.Specification;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SocieteContactQuery {
    private String search;
    private boolean deleted;
    /**
     * Creates a {@link Specification} from the properties of the current object.
     * <p>
     * The resulting {@link Specification} will be used to query the database. The
     * specification will be created by Societebining the following JPA specifications:
     * <ul>
     * <li>{@link SocieteSpecifications#search(String)} if the search property is
     * not null or blank.
     * </ul>
     * <p>
     * The specifications will be Societebined with the {@link Specification#and(Specification)}
     * method.
     *
     * @see SocieteSpecifications
     *
     * @return a {@link Specification} that can be used to query the database.
     */

    public Specification<SocieteContact> toSpecification() {
        Specification<SocieteContact> spec = Specification.where(SocieteContactSpecifications.deleted(deleted));

        return spec;
    }
}
