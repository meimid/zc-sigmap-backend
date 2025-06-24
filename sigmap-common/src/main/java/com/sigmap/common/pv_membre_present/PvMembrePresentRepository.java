package com.sigmap.common.pv_membre_present;

import com.sigmap.common.process_verbal.pv.dto.MembreCommisionDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PvMembrePresentRepository extends JpaRepository<PvMembrePresent, PvMembrePresentId> {

    @Query(nativeQuery = true, value = " select ppm_id as ppmId, ppm_ver as ppmVersion, process_verbal_id as processVerbalId, " +
            "  pv_membre_present_id as pvMembrePresentId, nom as nom, prenom as prenom, present as present, fonction_id as fonctionId , " +
            " (select code from ref_param where ref_param_id = fonction_id) as fonction " +
            " from pv_membre_present where ppm_id = :ppm_id and ppm_ver = :ppm_ver and process_verbal_id = :process_verbal_id")
    List<MembreCommisionDTO> getAllMembreCommision(@Param("ppm_id") Integer ppmId,
                                                   @Param("ppm_ver") Integer ppmVersion,
                                                   @Param("process_verbal_id") String processVerbalId);
}
