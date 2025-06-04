package com.sigmap.common.dyna_fields_details;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface DynaFieldsDetailsRepository
    extends JpaRepository<DynaFieldsDetails, DynaFieldsDetailsId>,
        JpaSpecificationExecutor<DynaFieldsDetails> {}
