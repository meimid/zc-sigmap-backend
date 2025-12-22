package com.sigmap.common.societe_dac;

import com.sigmap.common.repository.SoftDeleteRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;

@Repository
public interface SocieteDacRepository extends JpaRepository<SocieteDac, SocieteDacID>,
                                             JpaSpecificationExecutor<SocieteDac>,
                                             SoftDeleteRepository<SocieteDac, SocieteDacID> {

    @Query("SELECT sd FROM SocieteDac sd  WHERE sd.id.dac.dacId =:dacId AND sd.id.dac.dacVer =:dacVer AND sd.id.societe.id =:societeId and sd.isDeleted = false")
    Optional<SocieteDac> findById (@Param("dacId") Integer dacId, @Param("dacVer") Integer dacVer, @Param("societeId") Integer societeId);

    @Query("SELECT sd FROM SocieteDac sd  WHERE sd.id.dac.dacId =:dacId AND sd.id.dac.dacVer =:dacVer and sd.isDeleted = false")
    List<SocieteDac> findByDacIdAndDacVer (@Param("dacId") Integer dacId, @Param("dacVer") Integer dacVer);


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("update SocieteDac sd  set sd.isDeleted = true, sd.deletedAt = :deletedAt, sd.deletedBy = ?#{principal?.subject} where sd.id.dac.dacId = :dacId and sd.id.dac.dacVer = :dacVer and sd.id.societe.id = :societeId")
    void softDeleteCompositeKey(@Param("dacId") Integer dacId,
                                   @Param("version") Integer dacVer,
                                   @Param("societeId") Integer societeId,
                                   @Param("deletedAt") Calendar deletedAt);

    @Modifying
    @Transactional
    @Query("UPDATE SocieteDac s SET s.isDeleted = true, s.deletedAt = CURRENT_TIMESTAMP, s.deletedBy = ?#{principal.subject} WHERE s IN ?1")
    void softDelete(List<SocieteDac> list);

    @Query("""
    SELECT s
    FROM SocieteDac s
    WHERE s.isDeleted = false
      AND s.id.dac.dacId = :dacId
      AND s.id.dac.dacVer = (
          SELECT MAX(s2.id.dac.dacVer)
          FROM SocieteDac s2
          WHERE s2.isDeleted = false
            AND s2.id.dac.dacId = :dacId
      )
""")
    List<SocieteDac> findLatestByDacId(Integer dacId);

    @Query("SELECT sd FROM SocieteDac sd  WHERE sd.methodePaiement = com.sigmap.common.entity.MethodePaiement.MANUEL AND sd.justifPaiementId  is not null AND sd.statusPaiement is null and sd.isDeleted = false")
    Page<SocieteDac> findAllForAdmin (Pageable pageable);


}
