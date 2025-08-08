package com.sigmap.sigmap_params.delais_reglementaires;

import com.sigmap.common.ModeDePassationSelection.exception.ModeDePassationSelectionNotFound;
import com.sigmap.common.delais_reglementaires.DelaisReglementaires;
import com.sigmap.common.delais_reglementaires.DelaisReglementairesService;
import com.sigmap.common.delais_reglementaires.exceptions.DelaisReglementairesNotFound;
import com.sigmap.common.delais_reglementaires.request.DelaisReglementairesRequest;
import com.sigmap.common.delais_reglementaires.response.DelaisReglementairesQuery;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/delais-reglementaires")
@AllArgsConstructor
@Slf4j
@Tag(name = "Delais_Reglementaires", description = "Delais Reglementaires API")
public class DelaisReglementairesController {

    private final DelaisReglementairesService delaisReglementairesService;

    @PostMapping
    public ResponseEntity<DelaisReglementaires> createDelaisReglementaires(@RequestBody DelaisReglementairesRequest request) throws
            RefParamNotFound, ModeDePassationSelectionNotFound {
        DelaisReglementaires savedDelaisReglementaires = delaisReglementairesService.save(request);
        return ResponseEntity.ok(savedDelaisReglementaires);
    }

    @PutMapping
    public ResponseEntity<DelaisReglementaires> updateDelaisReglementaires(@RequestBody DelaisReglementairesRequest request)
            throws RefParamNotFound, ModeDePassationSelectionNotFound, DelaisReglementairesNotFound {
        DelaisReglementaires updatedDelaisReglementaires = delaisReglementairesService.update(request);
        return ResponseEntity.ok(updatedDelaisReglementaires);
    }

    @GetMapping
    public ResponseEntity<Page<DelaisReglementaires>> getAllDelaisReglementaires(DelaisReglementairesQuery query, Pageable pageable) {
        Page<DelaisReglementaires> delaisReglementairesPage = delaisReglementairesService.getAllDelaisReglementaires(query.toSpecification(), pageable);
        return ResponseEntity.ok(delaisReglementairesPage);
    }

} 