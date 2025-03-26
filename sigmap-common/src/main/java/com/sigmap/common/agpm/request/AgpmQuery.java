package com.sigmap.common.agpm.request;

import com.sigmap.common.agpm.Agpm;

import com.sigmap.common.agpm.AgpmSpecifications;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

/**
 * A query object for {@link com.sigmap.common.agpm.Agpm}.
 *
 * <p>This class is used to perform a search on a set of AGPM entities based on a
 * combination of properties such as search, id, ref, norm, content, status, and deleted.
 */
@Data
public class AgpmQuery {
    private String search;
    private Integer id;
    private String ref;
    private String nom;
    private String contenu;
    private Integer status;
    private boolean deleted;

    /**
     * Creates a {@link Specification} from the properties of the current object.
     * <p>
     * The resulting {@link Specification} will be used to query the database. The
     * specification will be created by combining the following JPA specifications:
     * <ul>
     * <li>{@link AgpmSpecifications#search(String)} if the search property is not null or blank.
     * <li>{@link AgpmSpecifications# id(Integer)} if the id property is not null.
     * <li>{@link AgpmSpecifications# ref(String)} if the ref property is not null or blank.
     * <li>{@link AgpmSpecifications# nom(String)} if the norm property is not null or blank.
     * <li>{@link AgpmSpecifications# content(String)} if the content property is not null or blank.
     * <li>{@link AgpmSpecifications# status(Integer)} if the status property is not null.
     * <li>{@link AgpmSpecifications# deleted(boolean)} if the deleted property is true.
     * </ul>
     * <p>
     * The specifications will be combined with the {@link Specification#and(Specification)}
     * method.
     *
     * @see AgpmSpecifications
     * @return a {@link Specification} that can be used to query the database.
     */
    public Specification<Agpm> toSpecification() {
        Specification<Agpm> spec = Specification.where(AgpmSpecifications.deleted(deleted));

        if (search != null && !search.isBlank()) {
            spec = spec.and(AgpmSpecifications.search(search));
        }

        if (id != null) {
            spec = spec.and(AgpmSpecifications.id(id));
        }

        if (ref != null && !ref.isBlank()) {
            spec = spec.and(AgpmSpecifications.ref(ref));
        }

        if (nom != null && !nom.isBlank()) {
            spec = spec.and(AgpmSpecifications.nom(nom));
        }

        return spec;
    }
}