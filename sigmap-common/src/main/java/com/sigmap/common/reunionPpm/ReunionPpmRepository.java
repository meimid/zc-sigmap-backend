package com.sigmap.common.reunionPpm;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReunionPpmRepository extends JpaRepository<ReunionPpm, ReunionPpmId>, JpaSpecificationExecutor<ReunionPpm> {
    Optional<ReunionPpm> findReunionPpmByReunionIdAndPpmIdAndPpmVer(Integer reunionId, Integer ppmId, Integer ppmVer);
    Optional<ReunionPpm> findReunionPpmByReunionIdAndPpmIdAndPpmVerAndProcessInstanceIdAndTaskId(Integer reunionId, Integer ppmId, Integer ppmVer, String processInstanceId, String taskId);
    List<ReunionPpm> findByReunionId(Integer reunionId);
}
