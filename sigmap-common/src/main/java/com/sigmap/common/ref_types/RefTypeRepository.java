package com.sigmap.common.ref_types;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefTypeRepository extends JpaRepository<RefType, Integer> {
}
