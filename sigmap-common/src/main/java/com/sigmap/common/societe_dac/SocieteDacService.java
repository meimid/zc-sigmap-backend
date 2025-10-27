package com.sigmap.common.societe_dac;


import com.sigmap.common.societe.exception.SocieteNotFound;
import com.sigmap.common.societe_dac.request.SocieteDacRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.io.IOException;

public interface SocieteDacService {

    Page<SocieteDac> findAll(Specification<SocieteDac> spec, Pageable pageable);
    Page<SocieteDac> findAllNotDeleted(Specification<SocieteDac> societeDac, Pageable pageable);

    SocieteDac findById(Integer dacId, Integer dacVersion, Integer societeId);

    SocieteDac create(SocieteDacRequest request) throws SocieteNotFound, IOException;

    SocieteDac update( SocieteDacRequest request);

    void delete(Integer dacId, Integer dacVersion, Integer societeId);

    SocieteDac rejectePaiement(Integer dacId, Integer dacVersion, Integer societeId);
    SocieteDac approuverPaiement(Integer dacId, Integer dacVersion, Integer societeId);

}
