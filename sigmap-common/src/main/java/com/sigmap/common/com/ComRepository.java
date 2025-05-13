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

    @Query("SELECT EXISTS (SELECT ac FROM Ac ac JOIN ac.coms c WHERE c.id = :comId)")
    boolean hasAssociatedAc(@Param("comId") Integer comId);

}
