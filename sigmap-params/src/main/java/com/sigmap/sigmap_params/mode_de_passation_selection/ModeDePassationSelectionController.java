package com.sigmap.sigmap_params.mode_de_passation_selection;

import com.sigmap.common.ModeDePassationSelection.ModeDePassationSelection;
import com.sigmap.common.ModeDePassationSelection.ModeDePassationSelectionService;
import com.sigmap.common.ModeDePassationSelection.exception.ModeDePassationSelectionNotFound;
import com.sigmap.common.ModeDePassationSelection.mappers.ModeDePassationSelectionMapper;
import com.sigmap.common.ModeDePassationSelection.request.ModeDePassationSelectionQuery;
import com.sigmap.common.ModeDePassationSelection.request.ModeDePassationSelectionRequest;
import com.sigmap.common.ModeDePassationSelection.response.ModeDePassationSelectionResponse;
import com.sigmap.common.ModeDePassationSelection.response.ModeDePassationSelectionResponseDto;
import com.sigmap.common.ac.mapper.AcMapper;
import com.sigmap.common.compte_bancaire.CompteBancaire;
import com.sigmap.common.compte_bancaire.mapper.CompteBancaireMapper;
import com.sigmap.common.compte_bancaire.request.CompteBancaireQuery;
import com.sigmap.common.compte_bancaire.response.CompteBancaireResponse;
import com.sigmap.common.contact.Contact;
import com.sigmap.common.contact.Mapper.ContactMapper;
import com.sigmap.common.contact.exception.ContactNotFound;
import com.sigmap.common.contact.request.ContactQuery;
import com.sigmap.common.contact.request.ContactRequest;
import com.sigmap.common.contact.response.ContactResponse;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import com.sigmap.common.societe_contact.exception.SocieteContactNotFound;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/mode_passation")
public class ModeDePassationSelectionController {
    private final ModeDePassationSelectionService service;

    public ModeDePassationSelectionController(ModeDePassationSelectionService service) {
        this.service = service;
    }

    /**
     * Return list of mode de passation selection by cateigorie id .
     *
     * @param typeMarcherId the query to use

     * @return a list of of mode de passation selection
     */
    @GetMapping("/type_marcher/{typeMarcherId}")
    public ResponseEntity<List<ModeDePassationSelectionResponseDto>> findAllBytypeMarcherId(
            @PathVariable Integer typeMarcherId) throws RefParamNotFound {
        List<ModeDePassationSelection> params=service.findBytypeMarcherId(typeMarcherId);
        List<ModeDePassationSelectionResponseDto> response = params.stream()
                .map(ModeDePassationSelectionMapper.INSTANCE::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
    /**
     * Return a page of Contact based on the given specification and pageable.
     *
     * @param query the query to use
     * @param pageable the pageable to use
     * @return a page of Contact
     */
    @GetMapping
    public ResponseEntity<Page<ModeDePassationSelectionResponseDto>> findAll(
            ModeDePassationSelectionQuery query, Pageable pageable) {
        Page<ModeDePassationSelection> params = service.findAll(query.toSpecification(), pageable);
        Page<ModeDePassationSelectionResponseDto> response =
                params.map(ModeDePassationSelectionMapper.INSTANCE::mapToDto);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/deleted")
    public ResponseEntity<Page<ModeDePassationSelectionResponseDto>> findAllDeleted(
            ModeDePassationSelectionQuery query, Pageable pageable) {
        Page<ModeDePassationSelection> params = service.findAllDeleted(query.toSpecification(), pageable);
        Page<ModeDePassationSelectionResponseDto> response = params.map(ModeDePassationSelectionMapper.INSTANCE::mapToDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ModeDePassationSelectionResponse> findById(@PathVariable Integer id) throws ModeDePassationSelectionNotFound {
        ModeDePassationSelection param = service.findById(id);
        ModeDePassationSelectionResponse response = ModeDePassationSelectionMapper.INSTANCE.map(param);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ModeDePassationSelectionResponse> create(@RequestBody ModeDePassationSelectionRequest request) throws RefParamNotFound {
        ModeDePassationSelection modeDePassationSelection = service.create(request);
        ModeDePassationSelectionResponse response = ModeDePassationSelectionMapper.INSTANCE.map(modeDePassationSelection);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @PutMapping("/{id}")
    public ResponseEntity<ModeDePassationSelectionResponse> update(@PathVariable Integer id,@RequestBody ModeDePassationSelectionRequest request)throws RefParamNotFound,ModeDePassationSelectionNotFound {
        ModeDePassationSelection modeDePassationSelection = service.update(id,request);
        ModeDePassationSelectionResponse response = ModeDePassationSelectionMapper.INSTANCE.map(modeDePassationSelection);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ModeDePassationSelectionResponse> deleteById(@PathVariable Integer id) throws  ModeDePassationSelectionNotFound {
        service.delete(id);
        ModeDePassationSelection deleted = service.findDeletedById(id)
                .orElseThrow();
        ModeDePassationSelectionResponse response = ModeDePassationSelectionMapper.INSTANCE.map(deleted);
        return ResponseEntity.ok(response);
    }
}
