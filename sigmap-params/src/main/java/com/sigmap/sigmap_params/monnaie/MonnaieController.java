package com.sigmap.sigmap_params.monnaie;

import com.sigmap.common.monnaie.Monnaie;
import com.sigmap.common.monnaie.MonnaieService;
import com.sigmap.common.monnaie.exceptions.MonnaieNotFound;
import com.sigmap.common.monnaie.mappers.MonnaieMapper;
import com.sigmap.common.monnaie.request.MonnaieQuery;
import com.sigmap.common.monnaie.request.MonnaieRequest;
import com.sigmap.common.monnaie.response.MonnaieResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/monnaie")
public class MonnaieController {

  private final MonnaieService MonnaieService;

  public MonnaieController(MonnaieService MonnaieService) {
    this.MonnaieService = MonnaieService;
  }

  @GetMapping
  public ResponseEntity<List<Monnaie>> findAll() {
    List<Monnaie> params = MonnaieService.findAll();
    return ResponseEntity.ok(params);
  }


  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
  public ResponseEntity<Monnaie> findById(@PathVariable Integer id) throws MonnaieNotFound {
    Monnaie params = MonnaieService.findById(id);
    return ResponseEntity.ok(params);
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
  public ResponseEntity<Monnaie> create(@RequestBody MonnaieRequest request)  {
    Monnaie Monnaie = MonnaieService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(Monnaie);
  }
//new code
  @GetMapping("/deleted")
  @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
  public ResponseEntity<Page<MonnaieResponse>> findAllDeleted(
          MonnaieQuery query, Pageable pageable) {
    Page<Monnaie> params = MonnaieService.findAllDeleted(query.toSpecification(), pageable);
    Page<MonnaieResponse> MonnaieResponses = params.map(MonnaieMapper.INSTANCE::map);
    return ResponseEntity.ok(MonnaieResponses);
  }

  @GetMapping("/all")
  @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
  public ResponseEntity<Page<MonnaieResponse>> search(MonnaieQuery query, Pageable pageable) {
    Page<Monnaie> page = MonnaieService.findAll(query.toSpecification(), pageable);
    Page<MonnaieResponse> responses = page.map(MonnaieMapper.INSTANCE::map);
    return ResponseEntity.ok(responses);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
  public ResponseEntity<MonnaieResponse> update(@PathVariable Integer id,@RequestBody MonnaieRequest request) throws MonnaieNotFound {
    Monnaie Monnaie = MonnaieService.update(id,request);
    MonnaieResponse response = MonnaieMapper.INSTANCE.map(Monnaie);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
  public ResponseEntity<MonnaieResponse> deleteById(@PathVariable Integer id) throws MonnaieNotFound {
    MonnaieService.delete(id);
    Monnaie deleted = MonnaieService.findDeletedById(id)
            .orElseThrow();
    MonnaieResponse response = MonnaieMapper.INSTANCE.map(deleted);
    return ResponseEntity.ok(response);
  }
}
