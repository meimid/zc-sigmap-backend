package com.sigmap.common.model_agpm;

import com.sigmap.common.agpm.Agpm;
import com.sigmap.common.agpm.AgpmRepository;
import com.sigmap.common.agpm.exceptions.AgpmNotFound;
import com.sigmap.common.docs.Docs;
import com.sigmap.common.docs.DocsRepository;
import com.sigmap.common.docs.exeptions.DocNotFound;
import com.sigmap.common.entity.CompositeId;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class ModelAgpmServiceImpl implements ModelAgpmService {

    private final ModelAgpmRepository modelPaaRepository;
    private final DocsRepository docsRepository;
    private final AgpmRepository agpmRepository;

    @Transactional
    @Override
    public ModelAgpm setModel(Integer agpmId, Integer agpmVer, Integer docId)
            throws DocNotFound, AgpmNotFound {

        Agpm agpm = agpmRepository.findById(new CompositeId(agpmId, agpmVer))
                .orElseThrow(() -> new AgpmNotFound(
                        "Aucun AGPM trouvé pour l'ID et la version",
                        List.of(agpmId), List.of(agpmVer)));

        Docs newDoc = docsRepository.findById(docId)
                .orElseThrow(() -> new DocNotFound(
                        "Aucun DOC trouvé pour l'ID", List.of(docId)));

        Optional<ModelAgpm> existingOpt = modelPaaRepository
                .findByAgpmIdAndAgpmVer(agpmId, agpmVer);

        if (existingOpt.isPresent()) {
            ModelAgpm existing = existingOpt.get();

            Integer oldDocId = existing.getDocId();
            modelPaaRepository.deleteByAgpmIdAndAgpmVer(agpmId, agpmVer);

            if (oldDocId != null && !oldDocId.equals(docId)) {
                docsRepository.softDelete(oldDocId);
            }
        }

        ModelAgpm mp = new ModelAgpm();
        mp.setDocId(docId);
        mp.setAgpmId(agpmId);
        mp.setAgpmVer(agpmVer);
        mp.setDoc(newDoc);
        mp.setAgpm(agpm);

        return modelPaaRepository.save(mp);
    }
    @Override
    public Optional<ModelAgpm> getModel(Integer agpmId, Integer agpmVer) {
        return modelPaaRepository.findByAgpmIdAndAgpmVer(agpmId, agpmVer);
    }

    @Transactional
    @Override
    public void removeModel(Integer agpmId, Integer agpmVer) {
        modelPaaRepository.deleteByAgpmIdAndAgpmVer(agpmId, agpmVer);
    }
}