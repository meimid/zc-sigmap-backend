package com.sigmap.common.pv_membre_present;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PvMembrePresentRepository  extends JpaRepository<PvMembrePresent, PvMembrePresentId> {

}
