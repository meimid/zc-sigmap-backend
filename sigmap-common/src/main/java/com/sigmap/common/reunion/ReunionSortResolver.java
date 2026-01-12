package com.sigmap.common.reunion;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ReunionSortResolver {

    private static final Map<String, ReunionSortHandler> SORT_HANDLERS = new HashMap<>();

    static {
        SORT_HANDLERS.put("comName", (order, spec, orders) ->
                Specification.where(spec).and(ReunionSpecifications.sortByComNom(order.getDirection())));

        SORT_HANDLERS.put("metadata.createdAt", (order, spec, orders) -> {
            orders.add(new Sort.Order(order.getDirection(), "createdAt"));
            return spec;
        });
    }

    public ResolvedSort resolve(Specification<Reunion> spec, Pageable pageable) {
        if (!pageable.getSort().isSorted()) {
            return new ResolvedSort(spec, pageable);
        }

        List<Sort.Order> orders = new ArrayList<>();
        for (Sort.Order order : pageable.getSort()) {
            ReunionSortHandler handler = SORT_HANDLERS.getOrDefault(order.getProperty(), (o, s, list) -> {
                list.add(o);
                return s;
            });
            spec = handler.apply(order, spec, orders);
        }

        Pageable newPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(orders));
        return new ResolvedSort(spec, newPageable);
    }

    public record ResolvedSort(Specification<Reunion> spec, Pageable pageable) {}
}
