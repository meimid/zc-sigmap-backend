package com.sigmap.common.model_paa;

import com.sigmap.common.docs.Docs;
import com.sigmap.common.docs.DocsRepository;
import com.sigmap.common.docs.exeptions.DocNotFound;
import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.paas.Paa;
import com.sigmap.common.paas.PaaRepository;
import com.sigmap.common.paas.exceptions.PaaNotFound;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class ModelPaaServiceImpl implements ModelPaaService {

    private final ModelPaaRepository modelPaaRepository;
    private final DocsRepository docsRepository;
    private final PaaRepository paaRepository;

    @Transactional
    @Override
    public ModelPaa setModel(Integer paaId, Integer paaVer, Integer docId)
            throws PaaNotFound, DocNotFound {

        // 1) Validate PPM and new DOC exist
        Paa paa = paaRepository.findById(new CompositeId(paaId, paaVer))
                .orElseThrow(() -> new PaaNotFound(
                        "Aucun PAA trouvé pour l'ID et la version",
                        List.of(paaId), List.of(paaVer)));

        Docs newDoc = docsRepository.findById(docId)
                .orElseThrow(() -> new DocNotFound(
                        "Aucun DOC trouvé pour l'ID", List.of(docId)));

        Optional<ModelPaa> existingOpt = modelPaaRepository
                .findByPaaIdAndPaaVer(paaId, paaVer);

        if (existingOpt.isPresent()) {
            ModelPaa existing = existingOpt.get();

            Integer oldDocId = existing.getDocId();
            modelPaaRepository.deleteByPaaIdAndPaaVer(paaId, paaVer);

            if (oldDocId != null && !oldDocId.equals(docId)) {
                docsRepository.softDelete(oldDocId);
            }
        }

        ModelPaa mp = new ModelPaa();
        mp.setDocId(docId);
        mp.setPaaId(paaId);
        mp.setPaaVer(paaVer);
        mp.setDoc(newDoc);
        mp.setPaa(paa);

        return modelPaaRepository.save(mp);
    }
    @Override
    public Optional<ModelPaa> getModel(Integer paaId, Integer paaVer) {
        return modelPaaRepository.findByPaaIdAndPaaVer(paaId, paaVer);
    }

    @Transactional
    @Override
    public void removeModel(Integer paaId, Integer paaVer) {
        modelPaaRepository.deleteByPaaIdAndPaaVer(paaId, paaVer);
    }

    @Override
    public Optional<ModelPaa> findOne(Specification<ModelPaa> spec) {
        return modelPaaRepository.findOne(spec);
    }
}