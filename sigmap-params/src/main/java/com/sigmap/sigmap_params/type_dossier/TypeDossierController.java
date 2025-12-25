package com.sigmap.sigmap_params.type_dossier;

import com.sigmap.common.type_dossier.TypeDossier;
import com.sigmap.common.type_dossier.TypeDossierService;
import com.sigmap.common.type_dossier.mapper.TypeDossierMapper;
import com.sigmap.common.type_dossier.request.TypeDossierQuery;
import com.sigmap.common.type_dossier.response.TypeDossierResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/type-dossier")
public class TypeDossierController {
    private final TypeDossierService service;

    public TypeDossierController(TypeDossierService service) {
        this.service = service;
    }
    @GetMapping
    ResponseEntity<List<TypeDossierResponse>> findAll(TypeDossierQuery query){
        List<TypeDossier> list=service.findAll(query.toSpecification());
        List<TypeDossierResponse> response =
                list.stream()
                        .map(TypeDossierMapper.INSTANCE::map)
                        .toList();
        return ResponseEntity.ok(response);
    }
}
