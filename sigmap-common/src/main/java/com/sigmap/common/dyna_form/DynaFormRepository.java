package com.sigmap.common.dyna_form;

import com.sigmap.common.entity.ActiveStatus;
import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.entity.TypeDossier;
import com.sigmap.common.repository.CompositeSoftDeleteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DynaFormRepository extends
        JpaRepository<DynaForm, CompositeId>,
        JpaSpecificationExecutor<DynaForm>,
        CompositeSoftDeleteRepository<DynaForm, Integer> {

    @Query("select max(f.version) from DynaForm f where f.id = :id and f.isDeleted = false")
    Integer findMaxVersionById(@Param("id") Integer id);

    @Query("select f from DynaForm f where f.id = :id and f.version = :ver and f.isDeleted = false")
    Optional<DynaForm> findActiveByIdAndVersion(@Param("id") Integer id, @Param("ver") Integer ver);

    @Query("select f from DynaForm f where f.typeDossier = :type and f.isDeleted = false")
    Page<DynaForm> findByTypeDossier(@Param("type") TypeDossier type, Pageable pageable);

    @Query("""
       select f from DynaForm f
       where f.isDeleted = false and f.status = :status
       order by f.id asc, f.version desc
    """)
    List<DynaForm> findAllActive(@Param("status") ActiveStatus status);

    @Query("SELECT f FROM DynaForm f WHERE f.id = :id AND (f.isDeleted = false OR f.isDeleted IS NULL)")
    List<DynaForm> findAllById(@Param("id") Integer id);


    @Modifying
    @Query("update DynaForm f set f.isDeleted = true where f.id = :id and f.version = :ver")
    void softDeleteByIdAndVersion(@Param("id") Integer id, @Param("ver") Integer ver);
}
