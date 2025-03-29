package com.sigmap.common.societe_address.request;
import com.sigmap.common.societe_address.SocieteAddress;
import com.sigmap.common.societe_address.SocieteAddressSpecifications;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

@Data
public class SocieteAddressQuery {
    private String search;
    private String address;
    private boolean deleted;
    /**
     * Creates a {@link Specification} from the properties of the current object.
     * <p>
     * The resulting {@link Specification} will be used to query the database. The
     * specification will be created by combining the following JPA specifications:
     * <ul>
     * <li>{@link SocieteAddressSpecifications#search(String)} if the search property is
     * not null or blank.
     * <li>{@link SocieteAddressSpecifications#address(String)} if the address property is not
     * null or blank.
     * <li>{@link SocieteAddressSpecifications#deleted(boolean)} if the deleted property is
     * true.
     * </ul>
     * <p>
     * The specifications will be combined with the {@link Specification#and(Specification)}
     * method.
     *
     * @see SocieteAddressSpecifications
     * @see SocieteAddressService
     *
     * @return a {@link Specification} that can be used to query the database.
     */
    public Specification<SocieteAddress> toSpecification() {
        Specification<SocieteAddress> spec = Specification.where(SocieteAddressSpecifications.deleted(deleted));

        if (search != null && !search.isBlank()) {
            spec = spec.and(SocieteAddressSpecifications.search(search));
        }

        if (address != null && !address.isBlank()) {
            spec = spec.and(SocieteAddressSpecifications.address(address));
        }

        return spec;
    }
}
