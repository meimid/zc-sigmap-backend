package com.sigmap.common.monnaie;

import com.sigmap.common.repository.SoftDeleteRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for managing {@link Monnaie}.
 *
 * This repository extends {@link JpaRepository}, {@link JpaSpecificationExecutor}, and
 * {@link SoftDeleteRepository} for performing common operations on RefParams.
 */
@Repository
public interface MonnaieRepository extends JpaRepository<Monnaie, Integer>,
        JpaSpecificationExecutor<Monnaie>,
        SoftDeleteRepository<Monnaie, Integer> {


}
