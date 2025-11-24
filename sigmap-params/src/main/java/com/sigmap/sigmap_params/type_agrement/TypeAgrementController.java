package com.sigmap.sigmap_params.type_agrement;


import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import com.sigmap.common.type_agrement.TypeAgrement;
import com.sigmap.common.type_agrement.TypeAgrementService;
import com.sigmap.common.type_agrement.exception.TypeAgrementNotFound;
import com.sigmap.common.type_agrement.mapper.TypeAgrementMapper;
import com.sigmap.common.type_agrement.request.TypeAgrementQuery;
import com.sigmap.common.type_agrement.request.TypeAgrementRequest;
import com.sigmap.common.type_agrement.response.TypeAgrementResponse;
import com.sigmap.common.type_agrement.response.TypeAgrementResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/type_agrement")
public class TypeAgrementController {

    private final TypeAgrementService service;
    
    public TypeAgrementController(TypeAgrementService service) {
        this.service = service;
    }

    /**
     * Return a page of Type Agrement based on the given specification and pageable.
     *
     * @param query the query to use
     * @param pageable the pageable to use
     * @return a page of Type Agrement
     */
    @GetMapping
    public ResponseEntity<Page<TypeAgrementResponseDto>> findAll(
            TypeAgrementQuery query, Pageable pageable) {
        Page<TypeAgrement> params = service.findAll(query.toSpecification(), pageable);
        Page<TypeAgrementResponseDto> response =
                params.map(TypeAgrementMapper.INSTANCE::mapToDto);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/deleted")
    public ResponseEntity<Page<TypeAgrementResponseDto>> findAllDeleted(
            TypeAgrementQuery query, Pageable pageable) {
        Page<TypeAgrement> params = service.findAllDeleted(query.toSpecification(), pageable);
        Page<TypeAgrementResponseDto> response = params.map(TypeAgrementMapper.INSTANCE::mapToDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TypeAgrementResponse> findById(@PathVariable Integer id) throws TypeAgrementNotFound {
        TypeAgrement param = service.findById(id);
        TypeAgrementResponse response = TypeAgrementMapper.INSTANCE.map(param);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<TypeAgrementResponse> create(@RequestBody TypeAgrementRequest request) throws RefParamNotFound {
        TypeAgrement TypeAgrement = service.create(request);
        TypeAgrementResponse response = TypeAgrementMapper.INSTANCE.map(TypeAgrement);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @PutMapping("/{id}")
    public ResponseEntity<TypeAgrementResponse> update(@PathVariable Integer id,@RequestBody TypeAgrementRequest request)throws RefParamNotFound,TypeAgrementNotFound {
        TypeAgrement TypeAgrement = service.update(id,request);
        TypeAgrementResponse response = TypeAgrementMapper.INSTANCE.map(TypeAgrement);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<TypeAgrementResponse> deleteById(@PathVariable Integer id) throws  TypeAgrementNotFound {
        service.delete(id);
        TypeAgrement deleted = service.findDeletedById(id)
                .orElseThrow();
        TypeAgrementResponse response = TypeAgrementMapper.INSTANCE.map(deleted);
        return ResponseEntity.ok(response);
    }
}
