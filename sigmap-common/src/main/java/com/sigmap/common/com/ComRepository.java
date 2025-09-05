package com.sigmap.common.com;
import com.sigmap.common.repository.SoftDeleteRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ComRepository extends JpaRepository<Com, Integer>,
        JpaSpecificationExecutor<Com>,
        SoftDeleteRepository<Com, Integer> {

    /*@Query("SELECT EXISTS (SELECT ac FROM Ac ac JOIN ac.coms c WHERE c.id = :comId and ac.isDeleted=false and c.isDeleted=false)")
    boolean hasAssociatedAc(@Param("comId") Integer comId);*/

    @Query("SELECT c.responsableId FROM Com c WHERE c.id = :comId AND c.isDeleted = false")
    String findIdResponsableByComId(@Param("comId") Integer comId);

}
