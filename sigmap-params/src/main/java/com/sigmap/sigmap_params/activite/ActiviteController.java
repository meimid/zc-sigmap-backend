package com.sigmap.sigmap_params.activite;

import com.sigmap.common.activite.Activite;
import com.sigmap.common.activite.ActiviteService;
import com.sigmap.common.activite.exception.ActiviteNotFound;
import com.sigmap.common.activite.mapper.ActiviteMapper;
import com.sigmap.common.activite.request.ActiviteQuery;
import com.sigmap.common.activite.request.ActiviteRequest;
import com.sigmap.common.activite.response.ActiviteResponse;
import com.sigmap.common.activite.response.ActiviteResponseDto;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/activite")
public class ActiviteController {
    private final ActiviteService service;

    public ActiviteController(ActiviteService service) {
        this.service = service;
    }

    /**
     * Return list of activite selection by cateigorie id .
     *
     * @param categoriesActiviteId the query to use

     * @return a list of of activite selection
     */
    @GetMapping("/categories_activite/{categoriesActiviteId}")
    public ResponseEntity<List<ActiviteResponseDto>> findAllBycategoriesActiviteId(
            @PathVariable Integer categoriesActiviteId) throws RefParamNotFound {
        List<Activite> params=service.findByCategoriesActiviteId(categoriesActiviteId);
        List<ActiviteResponseDto> response = params.stream()
                .map(ActiviteMapper.INSTANCE::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
    /**
     * Return a page of Activite based on the given specification and pageable.
     *
     * @param query the query to use
     * @param pageable the pageable to use
     * @return a page of Activite
     */
    @GetMapping
    public ResponseEntity<Page<ActiviteResponseDto>> findAll(
            ActiviteQuery query, Pageable pageable) {
        Page<Activite> params = service.findAll(query.toSpecification(), pageable);
        Page<ActiviteResponseDto> response =
                params.map(ActiviteMapper.INSTANCE::mapToDto);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/deleted")
    public ResponseEntity<Page<ActiviteResponseDto>> findAllDeleted(
            ActiviteQuery query, Pageable pageable) {
        Page<Activite> params = service.findAllDeleted(query.toSpecification(), pageable);
        Page<ActiviteResponseDto> response = params.map(ActiviteMapper.INSTANCE::mapToDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActiviteResponse> findById(@PathVariable Integer id) throws ActiviteNotFound {
        Activite param = service.findById(id);
        ActiviteResponse response = ActiviteMapper.INSTANCE.map(param);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ActiviteResponse> create(@RequestBody ActiviteRequest request) throws RefParamNotFound {
        Activite Activite = service.create(request);
        ActiviteResponse response = ActiviteMapper.INSTANCE.map(Activite);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @PutMapping("/{id}")
    public ResponseEntity<ActiviteResponse> update(@PathVariable Integer id,@RequestBody ActiviteRequest request)throws RefParamNotFound,ActiviteNotFound {
        Activite Activite = service.update(id,request);
        ActiviteResponse response = ActiviteMapper.INSTANCE.map(Activite);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ActiviteResponse> deleteById(@PathVariable Integer id) throws  ActiviteNotFound {
        service.delete(id);
        Activite deleted = service.findDeletedById(id)
                .orElseThrow();
        ActiviteResponse response = ActiviteMapper.INSTANCE.map(deleted);
        return ResponseEntity.ok(response);
    }
}
