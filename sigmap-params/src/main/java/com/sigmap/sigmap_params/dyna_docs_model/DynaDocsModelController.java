package com.sigmap.sigmap_params.dyna_docs_model;

import com.sigmap.common.dyna_docs_model.DynaDocsModel;
import com.sigmap.common.dyna_docs_model.DynaDocsModelService;
import com.sigmap.common.dyna_docs_model.DynaDocsModelSpecifications;
import com.sigmap.common.dyna_docs_model.exeptions.DynaDocsModelNotFoundExeption;
import com.sigmap.common.dyna_docs_model.mapper.DynaDocsModelMapper;
import com.sigmap.common.dyna_docs_model.request.DynaDocsModelRequest;
import com.sigmap.common.dyna_docs_model.response.DynaDocsModelResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dyna-docs-models")
@Tag(name = "DynaDocsModel", description = "API de gestion des modèles de documents dynamiques")
public class DynaDocsModelController {

    private final DynaDocsModelService dynaDocsModelService;

    @GetMapping
    public ResponseEntity<Page<DynaDocsModelResponse>> getAllDynaDocsModels(Pageable pageable) {
        log.debug("Récupération de tous les modèles de documents dynamiques");
        // Créer une spécification pour les éléments non supprimés
        Specification<DynaDocsModel> specification = Specification.where(DynaDocsModelSpecifications.deleted(false));
        Page<DynaDocsModel> result = dynaDocsModelService.findAll(specification, pageable);
        Page<DynaDocsModelResponse> response = result.map(DynaDocsModelMapper.INSTANCE::map);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DynaDocsModelResponse> getDynaDocsModelById(@PathVariable Integer id) {
        log.debug("Récupération du modèle de document dynamique avec l'ID: {}", id);

        Optional<DynaDocsModel> dynaDocsModel = dynaDocsModelService.findById(id);
        DynaDocsModelResponse response = DynaDocsModelMapper.INSTANCE.map(dynaDocsModel.orElseThrow(() -> new DynaDocsModelNotFoundExeption(id)));
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<DynaDocsModelResponse> createDynaDocsModel(@RequestBody DynaDocsModelRequest request) {
        log.debug("Création d'un nouveau modèle de document dynamique: {}", request);
        DynaDocsModel createdModel = dynaDocsModelService.save(request);
        DynaDocsModelResponse response = DynaDocsModelMapper.INSTANCE.map(createdModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DynaDocsModelResponse> updateDynaDocsModel(@PathVariable Integer id, @RequestBody DynaDocsModelRequest request) {
        log.debug("Mise à jour du modèle de document dynamique avec l'ID {}: {}", id, request);
        DynaDocsModel updatedModel = dynaDocsModelService.update(id, request);
        DynaDocsModelResponse response = DynaDocsModelMapper.INSTANCE.map(updatedModel);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDynaDocsModel(@PathVariable Integer id) {
        log.debug("Suppression du modèle de document dynamique avec l'ID: {}", id);
        dynaDocsModelService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
