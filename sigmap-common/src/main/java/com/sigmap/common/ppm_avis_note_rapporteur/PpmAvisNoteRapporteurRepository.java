package com.sigmap.common.ppm_avis_note_rapporteur;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PpmAvisNoteRapporteurRepository extends JpaRepository<PpmAvisNoteRapporteur, Integer> {

    List<PpmAvisNoteRapporteur> findByPpmIdAndPpmVer(Integer ppmId, Integer ppmVer);

    List<PpmAvisNoteRapporteur> findByProcessInstanceId(String processInstanceId);

    List<PpmAvisNoteRapporteur> findByTaskId(String taskId);

    Optional<PpmAvisNoteRapporteur> findFirstByProcessInstanceIdAndTaskIdAndRapporteurUserId(
            String processInstanceId,
            String taskId,
            String rapporteurUserId);
}