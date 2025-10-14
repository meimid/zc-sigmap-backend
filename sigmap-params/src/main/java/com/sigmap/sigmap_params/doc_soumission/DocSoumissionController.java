package com.sigmap.sigmap_params.doc_soumission;




import com.sigmap.common.doc_soumission.DocSoumission;
import com.sigmap.common.doc_soumission.DocSoumissionService;
import com.sigmap.common.doc_soumission.exception.DocSoumissionNotFound;
import com.sigmap.common.doc_soumission.mapper.DocSoumissionMapper;
import com.sigmap.common.doc_soumission.request.DocSoumissionQuery;
import com.sigmap.common.doc_soumission.request.DocSoumissionRequest;
import com.sigmap.common.doc_soumission.response.DocSoumissionResponse;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/doc_soumission")
public class DocSoumissionController {
    private final DocSoumissionService service;

    public DocSoumissionController(DocSoumissionService service) {
        this.service = service;
    }

    /**
     * Return a page of DocSoumission based on the given specification and pageable.
     *
     * @param query the query to use
     * @param pageable the pageable to use
     * @return a page of DocSoumission
     */
    @GetMapping
    public ResponseEntity<Page<DocSoumissionResponse>> findAll(
            DocSoumissionQuery query, Pageable pageable) {
        Page<DocSoumission> params = service.findAll(query.toSpecification(), pageable);
        Page<DocSoumissionResponse> response =
                params.map(DocSoumissionMapper.INSTANCE::map);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/list")
    public ResponseEntity<List<DocSoumissionResponse>> findAllList(
            DocSoumissionQuery query) {
        List<DocSoumission> params = service.findAllList(query.toSpecification());
        List<DocSoumissionResponse> response =params.stream().map(DocSoumissionMapper.INSTANCE::map).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocSoumissionResponse> findById(@PathVariable Integer id) throws DocSoumissionNotFound {
        DocSoumission param = service.findById(id);
        DocSoumissionResponse response = DocSoumissionMapper.INSTANCE.map(param);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<DocSoumissionResponse> create(@RequestBody DocSoumissionRequest request)  {
        DocSoumission DocSoumission = service.create(request);
        DocSoumissionResponse response = DocSoumissionMapper.INSTANCE.map(DocSoumission);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DocSoumissionResponse> update(@PathVariable Integer id, @RequestBody DocSoumissionRequest request)throws DocSoumissionNotFound {
        DocSoumission DocSoumission = service.update(id,request);
        DocSoumissionResponse response = DocSoumissionMapper.INSTANCE.map(DocSoumission);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteById(@PathVariable Integer id) throws  DocSoumissionNotFound {
        service.delete(id);
        return ResponseEntity.ok("doc_soumission deleted successfully");
    }
}
