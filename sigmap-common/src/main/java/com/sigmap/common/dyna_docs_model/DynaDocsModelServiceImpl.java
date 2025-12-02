package com.sigmap.common.dyna_docs_model;

import com.sigmap.common.dyna_docs.DynaDocsRepository;
import com.sigmap.common.dyna_docs_model.exeptions.DynaDocsModelAlreadyUsedExeption;
import com.sigmap.common.dyna_docs_model.exeptions.DynaDocsModelNotFoundExeption;
import com.sigmap.common.dyna_docs_model.request.DynaDocsModelRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DynaDocsModelServiceImpl implements DynaDocsModelService {

    private final DynaDocsModelRepository dynaDocsModelRepository;
    private final DynaDocsRepository dynaDocsRepository;

    @Override
    public List<DynaDocsModel> findAll() {
        log.debug("Récupération de tous les modèles de documents dynamiques");
        return dynaDocsModelRepository.findAllNotDeleted();
    }

    @Override
    public Page<DynaDocsModel> findAll(Specification<DynaDocsModel> spec, Pageable pageable) {


        log.debug("Récupération paginée des modèles de documents dynamiques avec spécifications");
        return dynaDocsModelRepository.findAll(spec, pageable);
    }

    @Override
    public Optional<DynaDocsModel> findById(Integer id) {
        log.debug("Recherche du modèle de document dynamique avec l'id: {}", id);
        return dynaDocsModelRepository.findById(id);
    }

    @Override
    @Transactional
    public DynaDocsModel save(DynaDocsModelRequest request) {
        log.debug("Enregistrement d'un nouveau modèle de document dynamique: {}", request);
        if (request.getDescription() == null) {
            throw new IllegalArgumentException("la description est obligatoire");
        }

        DynaDocsModel dynaDocsModel = new DynaDocsModel();
        dynaDocsModel.setDescription(request.getDescription());

        return dynaDocsModelRepository.save(dynaDocsModel);
    }

    @Override
    @Transactional
    public DynaDocsModel update(Integer id, DynaDocsModelRequest request) {
        log.debug("Mise à jour du modèle de document dynamique avec l'id: {}", id);

        DynaDocsModel existingModel = dynaDocsModelRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Modèle de document dynamique avec l'id {} non trouvé", id);
                    return new DynaDocsModelNotFoundExeption(id);
                });

        // Mettre à jour les champs modifiables
        existingModel.setDescription(request.getDescription());

        log.debug("Modèle mis à jour avec succès: {}", existingModel);
        return dynaDocsModelRepository.save(existingModel);
    }
        


    @Override
    @Transactional
    public void delete(Integer id) {
        log.debug("Suppression du modèle de document dynamique avec l'id: {}", id);
        
        // Vérifier si le modèle existe
        if (!dynaDocsModelRepository.existsById(id)) {
            log.error("Impossible de supprimer. Modèle de document dynamique avec l'id {} non trouvé", id);
            throw new DynaDocsModelNotFoundExeption(id);
        }
        
        // Vérifier si le modèle est utilisé dans des documents dynamiques
        if (dynaDocsRepository.existsByDynaDocsModelId(id)) {
            log.error("Impossible de supprimer. Le modèle avec l'id {} est utilisé dans des documents dynamiques actifs", id);
            throw new DynaDocsModelAlreadyUsedExeption(id);
        }
        
        // Si tout est bon, procéder à la suppression logique
        dynaDocsModelRepository.softDelete(id);
        log.debug("Modèle de document dynamique avec l'id {} a été marqué comme supprimé avec succès", id);
    }
}
