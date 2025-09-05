package com.sigmap.common.delais_reglementaires;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface DelaisReglementairesRepository extends JpaRepository<DelaisReglementaires, DelaisReglementairesId>,
                                                     JpaSpecificationExecutor<DelaisReglementaires> {
}
