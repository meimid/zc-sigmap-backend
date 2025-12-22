package com.sigmap.common.dac_dyna_form;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.List;


public interface DacDynaFormRepository
        extends JpaRepository<DacDynaForm, DacDynaFormId>,
        JpaSpecificationExecutor<DacDynaForm> {

    Page<DacDynaForm> findByDacIdAndDacVer(Integer dacId, Integer dacVer, Pageable pageable);
    List<DacDynaForm> findByDacIdAndDacVer(Integer dacId, Integer dacVer);

    @Query("""
           SELECT d FROM DacDynaForm d
           JOIN DynaForm f ON d.dynaFormId = f.id AND d.dynaFormVer = f.version
           WHERE f.typeDossier.id = :typeDossierId
           """)
    Page<DacDynaForm> findByTypeDossier(@Param("typeDossierId")Integer typeDossierId, Pageable pageable);

    @Query("""
    SELECT d FROM DacDynaForm d
    WHERE d.dacId = :dacId AND d.dacVer = :dacVer
          AND d.dynaFormId = :formId AND d.dynaFormVer = :formVer
""")
    List<DacDynaForm> findByDacAndForm(@Param("dacId") Integer dacId,
                                       @Param("dacVer") Integer dacVer,
                                       @Param("formId") Integer formId,
                                       @Param("formVer") Integer formVer);

    @Query("""
    SELECT DISTINCT d.dynaFormId, d.dynaFormVer
    FROM DacDynaForm d
    WHERE d.dacId = :dacId AND d.dacVer = :dacVer AND d.isDeleted = false
""")
    List<Object[]> findDistinctFormsByDac(@Param("dacId") Integer dacId, @Param("dacVer") Integer dacVer);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
    UPDATE DacDynaForm d 
       SET d.isDeleted = true, 
           d.deletedAt = :deletedAt, 
           d.deletedBy = ?#{principal?.subject}
     WHERE d.dacId = :dacId
       AND d.dacVer = :dacVer
       AND d.dynaFormId = :dynaFormId
       AND d.dynaFormVer = :dynaFormVer
       AND d.isDeleted = false
    """)
    int softDelete(@Param("dacId") Integer dacId,
                   @Param("dacVer") Integer dacVer,
                   @Param("dynaFormId") Integer dynaFormId,
                   @Param("dynaFormVer") Integer dynaFormVer,
                   @Param("deletedAt") Calendar deletedAt);

}
