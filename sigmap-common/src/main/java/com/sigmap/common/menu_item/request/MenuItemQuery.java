package com.sigmap.common.menu_item.request;


import com.sigmap.common.menu_item.MenuItem;
import com.sigmap.common.menu_item.MenuItemSpecifications;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;

@Data
@Slf4j
public class MenuItemQuery {
    private String search;
    private String label;
    private String icon;
    private String route;
    private Integer orderIndex;
    private String authority;
    private boolean deleted;

    /**
     * Creates a {@link Specification} from the properties of the current object.
     * <p>
     * The resulting {@link Specification} will be used to query the database. The
     * specification will be created by combining the following JPA specifications:
     * <ul>
     * <li>{@link MenuItemSpecifications#id(Integer)} if the id property is not null or blank.</li>
     * <li>{@link MenuItemSpecifications#search(String)} if the search property is not null or blank.</li>
     * <li>{@link MenuItemSpecifications#deleted(boolean)} if the deleted property is not null.</li>
     * </ul>
     * <p>
     * The specifications will be combined with the {@link Specification#and(Specification)}
     * method.
     *
     * @see MenuItemSpecifications
     *
     * @return a {@link Specification} that can be used to query the database.
     */

    public Specification<MenuItem> toSpecification() {
        Specification<MenuItem> spec = Specification.where(MenuItemSpecifications.deleted(deleted));

        if (search != null && !search.isBlank()) {
            spec = spec.and(MenuItemSpecifications.search(search));
        }

        if (label != null && !label.isBlank()) {
            spec = spec.and(MenuItemSpecifications.label(label));
        }

        if (icon != null && !icon.isBlank()) {
            spec = spec.and(MenuItemSpecifications.icon(icon));
        }

        if (route != null && !route.isBlank()) {
            spec = spec.and(MenuItemSpecifications.route(route));
        }

        if (orderIndex != null) {
            spec = spec.and(MenuItemSpecifications.orderIndex(orderIndex));
        }

        if (authority != null && !authority.isBlank()) {
            spec = spec.and(MenuItemSpecifications.authority(authority));
        }

        if (!deleted) {
            spec = spec.and(MenuItemSpecifications.deleted(deleted));
        }

        return spec;
    }
}
