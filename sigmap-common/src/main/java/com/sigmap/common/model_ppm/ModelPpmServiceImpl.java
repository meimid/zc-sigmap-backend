package com.sigmap.common.model_ppm;

import com.sigmap.common.docs.Docs;
import com.sigmap.common.docs.DocsRepository;
import com.sigmap.common.docs.exeptions.DocNotFound;
import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.ppms.Ppm;
import com.sigmap.common.ppms.PpmRepository;
import com.sigmap.common.ppms.exception.PpmNotFound;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class ModelPpmServiceImpl implements ModelPpmService {

    private final ModelPpmRepository modelPpmRepository;
    private final DocsRepository docsRepository;
    private final PpmRepository ppmRepository;

    @Transactional
    @Override
    public ModelPpm setModel(Integer ppmId, Integer ppmVer, Integer docId)
            throws PpmNotFound, DocNotFound {

        // 1) Validate PPM and new DOC exist
        Ppm ppm = ppmRepository.findById(new CompositeId(ppmId, ppmVer))
                .orElseThrow(() -> new PpmNotFound(
                        "Aucun PPM trouvé pour l'ID et la version",
                        List.of(ppmId), List.of(ppmVer)));

        Docs newDoc = docsRepository.findById(docId)
                .orElseThrow(() -> new DocNotFound(
                        "Aucun DOC trouvé pour l'ID", List.of(docId)));

        Optional<ModelPpm> existingOpt = modelPpmRepository
                .findByPpmIdAndPpmVer(ppmId, ppmVer);

        if (existingOpt.isPresent()) {
            ModelPpm existing = existingOpt.get();

            Integer oldDocId = existing.getDocId();
            modelPpmRepository.deleteByPpmIdAndPpmVer(ppmId, ppmVer);

            if (oldDocId != null && !oldDocId.equals(docId)) {
                docsRepository.softDelete(oldDocId);
            }
        }

        ModelPpm mp = new ModelPpm();
        mp.setDocId(docId);
        mp.setPpmId(ppmId);
        mp.setPpmVer(ppmVer);
        mp.setDoc(newDoc);
        mp.setPpm(ppm);

        return modelPpmRepository.save(mp);
    }
    @Override
    public Optional<ModelPpm> getModel(Integer ppmId, Integer ppmVer) {
        return modelPpmRepository.findByPpmIdAndPpmVer(ppmId, ppmVer);
    }

    @Transactional
    @Override
    public void removeModel(Integer ppmId, Integer ppmVer) {
        modelPpmRepository.deleteByPpmIdAndPpmVer(ppmId, ppmVer);
    }
}