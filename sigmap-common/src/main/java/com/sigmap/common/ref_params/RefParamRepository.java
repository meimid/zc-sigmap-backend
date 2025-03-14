package com.sigmap.common.ref_params;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefParamRepository extends JpaRepository<RefParam, Integer> {
}
