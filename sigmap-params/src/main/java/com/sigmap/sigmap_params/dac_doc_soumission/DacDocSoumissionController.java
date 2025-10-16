package com.sigmap.sigmap_params.dac_doc_soumission;


import com.sigmap.common.dac_doc_soumission.DacDocSoumission;
import com.sigmap.common.dac_doc_soumission.DacDocSoumissionService;
import com.sigmap.common.dac_doc_soumission.exception.DacDocSoumissionNotFound;
import com.sigmap.common.dac_doc_soumission.mapper.DacDocSoumissionMapper;
import com.sigmap.common.dac_doc_soumission.request.DacDocSoumissionQuery;
import com.sigmap.common.dac_doc_soumission.request.DacDocSoumissionCreateRequest;
import com.sigmap.common.dac_doc_soumission.request.DacDocSoumissionUpdateRequest;
import com.sigmap.common.dac_doc_soumission.response.DacDocSoumissionPageResponse;
import com.sigmap.common.dac_doc_soumission.response.DacDocSoumissionResponse;
import com.sigmap.common.doc_soumission.DocSoumission;
import com.sigmap.common.doc_soumission.mapper.DocSoumissionMapper;
import com.sigmap.common.doc_soumission.request.DocSoumissionQuery;
import com.sigmap.common.doc_soumission.response.DocSoumissionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dac-doc-soumission")
public class DacDocSoumissionController {
    private final DacDocSoumissionService service;

    public DacDocSoumissionController(DacDocSoumissionService service) {
        this.service = service;
    }

    /**
     * Return a page of DacDocSoumission based on the given specification and pageable.
     *
     * @param query the query to use
     * @param pageable the pageable to use
     * @return a page of DacDocSoumission
     */
    @GetMapping
    public ResponseEntity<Page<DacDocSoumissionResponse>> findAll(
            DacDocSoumissionQuery query, Pageable pageable) {
        Page<DacDocSoumission> params = service.findAll(query.toSpecification(), pageable);
        Page<DacDocSoumissionResponse> response =
                params.map(DacDocSoumissionMapper.INSTANCE::map);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/list")
    public ResponseEntity<List<DacDocSoumissionResponse>> findAllList(
            DacDocSoumissionQuery query) {
        List<DacDocSoumission> params = service.findAllList(query.toSpecification());
        List<DacDocSoumissionResponse> response =params.stream().map(DacDocSoumissionMapper.INSTANCE::map).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/dac/{dacId}/doc-soumission/{docSoumissionId}")
    public ResponseEntity<DacDocSoumissionResponse> findByDacIdAndDocSoumissionId(@PathVariable Integer dacId,@PathVariable Integer docSoumissionId) throws DacDocSoumissionNotFound {
        DacDocSoumission param = service.findByDacIdAndDocSoumissionId(dacId, docSoumissionId);
        DacDocSoumissionResponse response = DacDocSoumissionMapper.INSTANCE.map(param);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/dac/{dacId}")
    public ResponseEntity<DacDocSoumissionPageResponse> findByDacId(@PathVariable Integer dacId,
                                                                    DocSoumissionQuery query, Pageable pageable) throws DacDocSoumissionNotFound {
        List<DocSoumission> params =
                service.findExistingDocSoumissionOfDacDocSoumissionByDacId(dacId, query.toSpecification());
        params.sort(Comparator.comparing(DocSoumission::getId));

        int pageSize = pageable.getPageSize();
        int currentPage = pageable.getPageNumber();
        int startItem = currentPage * pageSize;

        List<DocSoumission> list;

        if (params.size() < startItem) {
            list = Collections.emptyList();
        } else {
            int toIndex = Math.min(startItem + pageSize, params.size());
            list = params.subList(startItem, toIndex);
        }

        Page<DocSoumissionResponse> page =
                new PageImpl<>(list.stream()
                        .map(DocSoumissionMapper.INSTANCE::map)
                        .collect(Collectors.toList()),
                        pageable,
                        params.size());

        List<DacDocSoumission> dacDocs = service.findByDacId(dacId);
        Date dateValidate = dacDocs.isEmpty() ? null : dacDocs.get(0).getDateValidate();

        DacDocSoumissionPageResponse response = DacDocSoumissionPageResponse.builder()
                .dacId(dacId)
                .dateValidate(dateValidate)
                .docSoumissions(page)
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<DacDocSoumissionResponse> create(@RequestBody DacDocSoumissionCreateRequest request)  {
        DacDocSoumission DacDocSoumission = service.create(request);
        DacDocSoumissionResponse response = DacDocSoumissionMapper.INSTANCE.map(DacDocSoumission);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/dac/{dacId}/doc-soumission/{docSoumissionId}")
    public ResponseEntity<DacDocSoumissionResponse> update(@PathVariable Integer dacId,@PathVariable Integer docSoumissionId, @RequestBody DacDocSoumissionUpdateRequest request)throws DacDocSoumissionNotFound {
        DacDocSoumission DacDocSoumission = service.update(dacId,docSoumissionId,request);
        DacDocSoumissionResponse response = DacDocSoumissionMapper.INSTANCE.map(DacDocSoumission);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/dac/{dacId}/doc-soumission/{docSoumissionId}")
    public ResponseEntity<DacDocSoumissionResponse> deleteById(@PathVariable Integer dacId,@PathVariable Integer docSoumissionId) throws  DacDocSoumissionNotFound {
        DacDocSoumission dacDocSoumission=service.findByDacIdAndDocSoumissionId(dacId,docSoumissionId);
        DacDocSoumissionResponse response = DacDocSoumissionMapper.INSTANCE.map(dacDocSoumission);
        service.delete(dacId,docSoumissionId);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/not-in-dac/{dacId}")
    public ResponseEntity<List<DocSoumissionResponse>> findAllListNotInDac(@PathVariable Integer dacId, DocSoumissionQuery query) {
        List<DocSoumission> params = service.findNotExistingDocSoumissionOfDacDocSoumissionByDacId(dacId,query.toSpecification());
        List<DocSoumissionResponse> response =params.stream().map(DocSoumissionMapper.INSTANCE::map).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/in-dac/{dacId}")
    public ResponseEntity<List<DocSoumissionResponse>> findAllListInDac(@PathVariable Integer dacId,DocSoumissionQuery query) {
        List<DocSoumission> params = service.findExistingDocSoumissionOfDacDocSoumissionByDacId(dacId,query.toSpecification());
        List<DocSoumissionResponse> response =params.stream().map(DocSoumissionMapper.INSTANCE::map).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}
