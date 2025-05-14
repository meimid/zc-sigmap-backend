package com.sigmap.common.dac;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DacRepository extends JpaRepository<Dac, DacId> {}

