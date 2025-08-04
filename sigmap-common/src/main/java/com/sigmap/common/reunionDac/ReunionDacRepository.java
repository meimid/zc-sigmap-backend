package com.sigmap.common.reunionDac;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReunionDacRepository extends JpaRepository<ReunionDac, ReunionDacId>, JpaSpecificationExecutor<ReunionDac> {
    Optional<ReunionDac> findReunionDacByReunionIdAndDacIdAndDacVer(Integer reunionId, Integer DacId, Integer DacVer);
    Optional<ReunionDac> findReunionDacByReunionIdAndDacIdAndDacVerAndProcessInstanceIdAndTaskId(Integer reunionId, Integer DacId, Integer DacVer, String processInstanceId, String taskId);
    List<ReunionDac> findByReunionId(Integer reunionId);
}