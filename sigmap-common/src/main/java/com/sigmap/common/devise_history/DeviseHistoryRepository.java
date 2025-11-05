package com.sigmap.common.devise_history;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviseHistoryRepository extends JpaRepository<DeviseHistory, Integer>,
                                                JpaSpecificationExecutor<DeviseHistory> {


    @Query("SELECT MAX(id) FROM DeviseHistory WHERE deviseId = :deviseId")
    Integer findMaxId(@Param("deviseId") Integer deviseId);

    @Query("SELECT dh FROM DeviseHistory dh WHERE dh.id = :id AND dh.deviseId = :deviseId ")
    DeviseHistory findLastOne (@Param("id") Integer id,@Param("deviseId") Integer deviseId);
}
