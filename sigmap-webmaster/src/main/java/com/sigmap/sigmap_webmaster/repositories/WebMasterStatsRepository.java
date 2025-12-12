package com.sigmap.sigmap_webmaster.repositories;

import com.sigmap.common.ref_params.RefParam;
import com.sigmap.sigmap_webmaster.dto.TypeMarcheStatProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WebMasterStatsRepository extends JpaRepository<RefParam, Integer> {

    @Query(value = """
        SELECT rp.description AS typeMarche, COUNT(*) AS nbr
        FROM (
            SELECT r.type_marcher_id AS tm_id
            FROM (
                SELECT paa_id AS id, MAX(paa_ver) AS ver
                FROM paa
                WHERE is_deleted = 0 AND status = 'PUBLIE'
                GROUP BY paa_id
            ) lp
            JOIN paa_realisation pr ON pr.paa_id = lp.id AND pr.paa_ver = lp.ver
            JOIN realisation r ON r.realisation_id = pr.realisation_id AND r.realisation_ver = pr.realisation_ver

            UNION ALL

            SELECT r.type_marcher_id AS tm_id
            FROM (
                SELECT ppm_id AS id, MAX(ppm_ver) AS ver
                FROM ppm
                WHERE is_deleted = 0 AND status = 'PUBLIE'
                GROUP BY ppm_id
            ) lm
            JOIN ppm_realisation pr2 ON pr2.ppm_id = lm.id AND pr2.ppm_ver = lm.ver
            JOIN realisation r ON r.realisation_id = pr2.realisation_id AND r.realisation_ver = pr2.realisation_ver
        ) u
        JOIN ref_param rp ON rp.ref_param_id = u.tm_id
        GROUP BY rp.description
        ORDER BY nbr DESC
        """, nativeQuery = true)
    List<TypeMarcheStatProjection> countRealisationsByTypeMarche();
}
