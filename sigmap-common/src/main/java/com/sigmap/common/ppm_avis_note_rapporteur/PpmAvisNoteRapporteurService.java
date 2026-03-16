package com.sigmap.common.ppm_avis_note_rapporteur;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PpmAvisNoteRapporteurService {

    private final PpmAvisNoteRapporteurRepository repository;

    public PpmAvisNoteRapporteurService(PpmAvisNoteRapporteurRepository repository) {
        this.repository = repository;
    }

    public PpmAvisNoteRapporteur save(PpmAvisNoteRapporteur entity) {
        return repository.save(entity);
    }

    public PpmAvisNoteRapporteur saveOrUpdate(
            String processInstanceId,
            String taskId,
            Integer ppmId,
            Integer ppmVer,
            String rapporteurUserId,
            String avis,
            Integer docIdNote) {

        Optional<PpmAvisNoteRapporteur> existing =
                repository.findFirstByProcessInstanceIdAndTaskIdAndRapporteurUserId(
                        processInstanceId, taskId, rapporteurUserId);

        PpmAvisNoteRapporteur entity = existing.orElseGet(PpmAvisNoteRapporteur::new);
        entity.setProcessInstanceId(processInstanceId);
        entity.setTaskId(taskId);
        entity.setPpmId(ppmId);
        entity.setPpmVer(ppmVer);
        entity.setRapporteurUserId(rapporteurUserId);
        entity.setAvis(avis);
        entity.setDocIdNote(docIdNote);

        return repository.save(entity);
    }

    public List<PpmAvisNoteRapporteur> findByPpm(Integer ppmId, Integer ppmVer) {
        return repository.findByPpmIdAndPpmVer(ppmId, ppmVer);
    }

    public List<PpmAvisNoteRapporteur> findByProcessInstanceId(String processInstanceId) {
        return repository.findByProcessInstanceId(processInstanceId);
    }

    public List<PpmAvisNoteRapporteur> findByTaskId(String taskId) {
        return repository.findByTaskId(taskId);
    }
}