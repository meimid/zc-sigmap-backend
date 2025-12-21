package com.sigmap.common.model_dac;

import com.sigmap.common.dac.Dac;
import com.sigmap.common.dac.DacId;
import com.sigmap.common.dac.DacRepository;
import com.sigmap.common.dac.exeption.DacNotFound;
import com.sigmap.common.docs.Docs;
import com.sigmap.common.docs.DocsRepository;
import com.sigmap.common.docs.exeptions.DocNotFound;
import com.sigmap.common.entity.CompositeId;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Slf4j
@Service
@RequiredArgsConstructor
public class ModelDacServiceImpl implements ModelDacService {

    private final ModelDacRepository modelDacRepository;
    private final DocsRepository docsRepository;
    private final DacRepository dacRepository;

    @Transactional
    @Override
    public ModelDac setModel(Integer dacId, Integer dacVer, Integer docId)
            throws DacNotFound, DocNotFound {

        // 1) Validate DAC and new DOC exist
        Dac dac = dacRepository.findById(new DacId(dacId, dacVer))
                .orElseThrow(() -> new DacNotFound(
                        "Aucun DAC trouvé pour l'ID et la version",
                        dacId , dacVer));

        Docs newDoc = docsRepository.findById(docId)
                .orElseThrow(() -> new DocNotFound(
                        "Aucun DOC trouvé pour l'ID", List.of(docId)));

        // 2) If a model already exists for this DAC version, soft-delete mapping
        //    and optionally soft-delete the old doc if it differs
        Optional<ModelDac> existingOpt = modelDacRepository.findByDacIdAndDacVer(dacId, dacVer);
        if (existingOpt.isPresent()) {
            ModelDac existing = existingOpt.get();
            Integer oldDocId = existing.getDocId();

            // mark old mapping as deleted
            modelDacRepository.deleteByDacIdAndDacVer(dacId, dacVer);

            // if changing the model doc, soft delete the old one
            if (oldDocId != null && !oldDocId.equals(docId)) {
                docsRepository.softDelete(oldDocId);
            }
        }

        // 3) Create and persist the new mapping
        ModelDac md = new ModelDac();
        md.setDocId(docId);
        md.setDacId(dacId);
        md.setDacVer(dacVer);
        md.setDoc(newDoc);
        md.setDac(dac);

        return modelDacRepository.save(md);
    }

    @Override
    public Optional<ModelDac> getModel(Integer dacId, Integer dacVer) {
        return modelDacRepository.findByDacIdAndDacVer(dacId, dacVer);
    }

    @Transactional
    @Override
    public void removeModel(Integer dacId, Integer dacVer) {
        modelDacRepository.deleteByDacIdAndDacVer(dacId, dacVer);
    }

    @Override
    public boolean isDocumentRelatedToDac(String alfrescoId) {
        return modelDacRepository.isDocumentRelatedToDac(alfrescoId);
    }

    @Override
    public boolean isDocumentRelatedToDacShortlistForUser(String alfrescoId, String userId) {
        return modelDacRepository.isDocumentRelatedToDacShortlistForUser(alfrescoId, userId);
    }
}
