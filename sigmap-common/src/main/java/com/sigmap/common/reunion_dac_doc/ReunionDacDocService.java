package com.sigmap.common.reunion_dac_doc;

import org.springframework.data.jpa.domain.Specification;

import java.io.IOException;
import java.util.Optional;

public interface ReunionDacDocService {
    Optional<ReunionDacDoc> findOne(Specification<ReunionDacDoc> specification) throws IOException;
}
