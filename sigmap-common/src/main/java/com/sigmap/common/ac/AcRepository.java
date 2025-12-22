package com.sigmap.common.ac;

import com.sigmap.common.com.Com;
import com.sigmap.common.repository.SoftDeleteRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AcRepository extends JpaRepository<Ac, Integer>,
        JpaSpecificationExecutor<Ac>,
        SoftDeleteRepository<Ac, Integer> {
   /* @Query("SELECT EXISTS (SELECT com FROM Com com JOIN com.acs ac WHERE ac.id = :acId and com.isDeleted=false and ac.isDeleted=false)")
    boolean hasAssociatedCom(@Param("acId") Integer acId);*/
   @Query("SELECT EXISTS (SELECT a FROM Agpm a WHERE a.ac.id = :acId and a.isDeleted=false)")
   boolean hasAssociatedAgpm(@Param("acId") Integer acId);
    @Query("SELECT EXISTS (SELECT p FROM Paa p WHERE p.ac.id = :acId and p.isDeleted=false)")
    boolean hasAssociatedPaa(@Param("acId") Integer acId);
    @Query("SELECT EXISTS (SELECT p FROM Ppm p WHERE p.ac.id = :acId and p.isDeleted=false)")
    boolean hasAssociatedPpm(@Param("acId") Integer acId);
    @Query("SELECT EXISTS (SELECT a FROM Ac a WHERE a.tutel.id = :acId and a.isDeleted=false)")
    boolean hasAssociatedAc(@Param("acId") Integer acId);
}

