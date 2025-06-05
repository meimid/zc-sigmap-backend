package com.sigmap.common.dyna_fields_details;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DynaFieldsDetailsRepository extends JpaRepository<DynaFieldsDetails, DynaFieldsDetailsId> {

    List<DynaFieldsDetails> findByDynaFieldsId(Integer dynaFieldsId);
}
