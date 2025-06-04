package com.sigmap.common.dyna_fields;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DynaFieldsRepository extends JpaRepository<DynaFields, DynaFieldsId> {}
