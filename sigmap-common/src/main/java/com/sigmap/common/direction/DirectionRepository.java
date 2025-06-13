package com.sigmap.common.direction;

import com.sigmap.common.repository.SoftDeleteRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DirectionRepository extends JpaRepository<Direction, Integer>,
        JpaSpecificationExecutor<Direction>,
        SoftDeleteRepository<Direction, Integer> {
}
