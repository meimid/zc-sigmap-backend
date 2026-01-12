package com.sigmap.common.reunion;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

@FunctionalInterface
public interface ReunionSortHandler {
    Specification<Reunion> apply(Sort.Order order, Specification<Reunion> spec, List<Sort.Order> orders);
}
