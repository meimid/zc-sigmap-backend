package com.sigmap.common.societe_dac;

import com.sigmap.common.repository.SoftDeleteRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Calendar;
import java.util.Optional;

@Repository
public interface SociteDacRepository extends JpaRepository<SocieteDac, SocieteDacID>,
                                             JpaSpecificationExecutor<SocieteDac>,
                                             SoftDeleteRepository<SocieteDac, SocieteDacID> {

    @Query("SELECT sd FROM SocieteDac sd  WHERE sd.id.dac.dacId =:dacId AND sd.id.dac.dacVer =:dacVer AND sd.id.societe.id =:societeId and sd.isDeleted = false")
    Optional<SocieteDac> findById (@Param("dacId") Integer dacId, @Param("dacVer") Integer dacVer, @Param("societeId") Integer societeId);


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("update SocieteDac sd  set sd.isDeleted = true, sd.deletedAt = :deletedAt, sd.deletedBy = ?#{principal?.subject} where sd.id.dac.dacId = :dacId and sd.id.dac.dacVer = :dacVer and sd.id.societe.id = :societeId")
    void softDeleteCompositeKey(@Param("dacId") Integer dacId,
                                   @Param("version") Integer dacVer,
                                   @Param("societeId") Integer societeId,
                                   @Param("deletedAt") Calendar deletedAt);
}
