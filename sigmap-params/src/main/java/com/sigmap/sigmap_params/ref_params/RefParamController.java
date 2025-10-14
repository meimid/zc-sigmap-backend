package com.sigmap.sigmap_params.ref_params;

import com.sigmap.common.config.Constants;
import com.sigmap.common.config.RefTypes;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.RefParamService;
import com.sigmap.common.ref_params.exceptions.*;
import com.sigmap.common.ref_params.request.RefParamQuery;
import com.sigmap.common.ref_params.mappers.RefParamMapper;
import com.sigmap.common.ref_params.request.RefParamRequest;
import com.sigmap.common.ref_params.request.RefParamRequestWithoutRefTypeIdDto;
import com.sigmap.common.ref_params.response.RefParamResponse;
import com.sigmap.common.ref_params.response.RefParamResponseDto;
import com.sigmap.common.ref_types.exceptions.RefTypeNotFound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller to manage ref params.
 *
 * @see RefParam
 * @see RefParamRequest
 * @see RefParamResponse
 * @see RefParamQuery
 * @see RefParamService
 */
@RestController
@RequestMapping("/api/ref-params")
public class RefParamController {
    private final Logger logger = LoggerFactory.getLogger(RefParamController.class);

    private final RefParamService refParamService;

    /**
     * Constructor.
     *
     * @param refParamService the service to use for managing ref params
     */
    public RefParamController(RefParamService refParamService) {
        this.refParamService = refParamService;
    }

    /**
     * Return a page of ref params based on the given specification and pageable.
     *
     * @param query    the query to use
     * @param pageable the pageable to use
     * @return a page of ref params
     */
    @GetMapping
    public ResponseEntity<Page<RefParamResponse>> findAll(
            RefParamQuery query, Pageable pageable) {

        Page<RefParam> params = refParamService.findAll(query.toSpecification(), pageable);
        Page<RefParamResponse> paramResponses = params.map(RefParamMapper.INSTANCE::map);
        return ResponseEntity.ok(paramResponses);
    }

    /**
     * Return a page of deleted ref params based on the given specification and pageable.
     *
     * @param query    the query to use
     * @param pageable the pageable to use
     * @return a page of deleted ref params
     */
    @GetMapping("/deleted")
    public ResponseEntity<Page<RefParamResponse>> findAllDeleted(
            RefParamQuery query, Pageable pageable) {
        Page<RefParam> params = refParamService.findAllDeleted(query.toSpecification(), pageable);
        Page<RefParamResponse> paramResponses = params.map(RefParamMapper.INSTANCE::map);
        return ResponseEntity.ok(paramResponses);
    }

    /**
     * Return a ref param based on the given id.
     *
     * @param id the id of the ref param to return
     * @return the ref param
     */
    @GetMapping("/{id}")
    public ResponseEntity<RefParamResponse> findById(@PathVariable Integer id) throws RefParamNotFound {
        RefParam param = refParamService.findById(id);
        RefParamResponse response = RefParamMapper.INSTANCE.map(param);
        return ResponseEntity.ok(response);
    }

    /**
     * Create a new ref param.
     *
     * @param request the request to create the ref param from
     * @return the created ref param
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
    public ResponseEntity<RefParamResponse> create(@RequestBody RefParamRequest request) throws RefTypeNotFound {

        RefParam refParam = refParamService.create(request);
        RefParamResponse response = RefParamMapper.INSTANCE.map(refParam);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update  RefParam.
     *
     * @param request the request to update the RefParam from
     * @return the updated RefParam
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
    public ResponseEntity<RefParamResponse> update(@PathVariable Integer id, @RequestBody RefParamRequest request) throws RefParamNotFound, RefTypeNotFound {
        RefParam refParam = refParamService.update(id, request);
        RefParamResponse response = RefParamMapper.INSTANCE.map(refParam);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Delete a ref param based on the given id.
     *
     * @param id the id of the ref param to delete
     * @return the deleted ref param
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
    public ResponseEntity<RefParamResponse> deleteById(@PathVariable Integer id) throws RefParamHasAssociatedCom,
            RefParamHasAssociatedCompteBancaire,
            RefParamHasAssociatedSociete,
            RefParamHasAssociatedSocieteAddress, RefParamNotFound {
        refParamService.delete(id);

        RefParam deleted = refParamService.findDeletedById(id)
                .orElseThrow();
        RefParamResponse response = RefParamMapper.INSTANCE.map(deleted);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/type-societe")
    public ResponseEntity<List<RefParamResponseDto>> findAllTypeSociete() throws RefTypeNotFound {
        List<RefParam> params = refParamService.findByRefType(RefTypes.TYPE_SOCIETE);
        List<RefParamResponseDto> responses = params.stream()
                .map(RefParamMapper.INSTANCE::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/categorie-activite")
    public ResponseEntity<List<RefParamResponseDto>> findAllCategorieActivite() throws RefTypeNotFound {
        List<RefParam> params = refParamService.findByRefType(RefTypes.CATEGORIE_ACTIVITE);
        List<RefParamResponseDto> responses = params.stream()
                .map(RefParamMapper.INSTANCE::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/pays")
    public ResponseEntity<List<RefParamResponseDto>> findAllPays() throws RefTypeNotFound {
        List<RefParam> params = refParamService.findByRefType(RefTypes.PAYS);
        List<RefParamResponseDto> responses = params.stream()
                .map(RefParamMapper.INSTANCE::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/type-comite")
    public ResponseEntity<List<RefParamResponseDto>> findAllTypeComite() throws RefTypeNotFound {
     //   List<RefParam> params = refParamService.findByRefType(RefTypes.TYPE_COMITE);
        List<RefParam> params = refParamService.findByRefTypeWithoutSystemType(RefTypes.TYPE_COMITE);
        List<RefParamResponseDto> responses = params.stream()
                .map(RefParamMapper.INSTANCE::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/type-address")
    public ResponseEntity<List<RefParamResponseDto>> findAllTypeAddres() throws RefTypeNotFound {
        List<RefParam> params = refParamService.findByRefType(RefTypes.TYPE_ADDRESS);
        List<RefParamResponseDto> responses = params.stream()
                .map(RefParamMapper.INSTANCE::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }


    @GetMapping("/dac-category")
    public ResponseEntity<List<RefParamResponseDto>> findAllDacCategory() throws RefTypeNotFound  {
        List<RefParam> params = refParamService.findByRefType(RefTypes.CATEGORIE_DAC);
        List<RefParamResponseDto> responses = params.stream()
                .map(RefParamMapper.INSTANCE::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }


    @GetMapping("/type-societe/{id}")
    public ResponseEntity<RefParamResponse> findByTypeSocieteAndById(@PathVariable Integer id) throws RefParamNotFound {
        RefParam param = refParamService.findByIdAndRefTypeId(id, RefTypes.TYPE_SOCIETE);
        RefParamResponse response = RefParamMapper.INSTANCE.map(param);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/categorie-activite/{id}")
    public ResponseEntity<RefParamResponse> findByCategorieActiviteAndById(@PathVariable Integer id) throws RefParamNotFound {
        RefParam param = refParamService.findByIdAndRefTypeId(id, RefTypes.CATEGORIE_ACTIVITE);
        RefParamResponse response = RefParamMapper.INSTANCE.map(param);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pays/{id}")
    public ResponseEntity<RefParamResponse> findByPaysAndById(@PathVariable Integer id) throws RefParamNotFound {
        RefParam param = refParamService.findByIdAndRefTypeId(id, RefTypes.PAYS);
        RefParamResponse response = RefParamMapper.INSTANCE.map(param);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/type-comite/{id}")
    public ResponseEntity<RefParamResponse> findByTypeComiteAndById(@PathVariable Integer id) throws RefParamNotFound {
        RefParam param = refParamService.findByIdAndRefTypeId(id, RefTypes.TYPE_COMITE);
        RefParamResponse response = RefParamMapper.INSTANCE.map(param);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/type-address/{id}")
    public ResponseEntity<RefParamResponse> findByTypeAddressAndById(@PathVariable Integer id) throws RefParamNotFound {
        RefParam param = refParamService.findByIdAndRefTypeId(id, RefTypes.TYPE_ADDRESS);
        RefParamResponse response = RefParamMapper.INSTANCE.map(param);
        return ResponseEntity.ok(response);
    }



    @GetMapping("/dac-category/{id}")
    public ResponseEntity<RefParamResponse> findByDacCategoryAndById(@PathVariable Integer id) throws RefParamNotFound  {
        RefParam param = refParamService.findByIdAndRefTypeId(id,RefTypes.CATEGORIE_DAC);
        RefParamResponse response = RefParamMapper.INSTANCE.map(param);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/type-societe/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
    public ResponseEntity<RefParamResponse> UpdateByTypeSocieteAndById(@PathVariable Integer id, @RequestBody RefParamRequestWithoutRefTypeIdDto requestDto) throws RefParamNotFound {
        RefParamRequest request = RefParamMapper.INSTANCE.mapToRequest(requestDto);
        request.setRefType(RefTypes.TYPE_SOCIETE);
        RefParam param = refParamService.UpdateRefParamByRefTypeId(id, RefTypes.TYPE_SOCIETE, request);
        RefParamResponse response = RefParamMapper.INSTANCE.map(param);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/categorie-activite/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
    public ResponseEntity<RefParamResponse> UpdateByCategorieActiviteAndById(@PathVariable Integer id, @RequestBody RefParamRequestWithoutRefTypeIdDto requestDto) throws RefParamNotFound {
        RefParamRequest request = RefParamMapper.INSTANCE.mapToRequest(requestDto);
        request.setRefType(RefTypes.CATEGORIE_ACTIVITE);
        RefParam param = refParamService.UpdateRefParamByRefTypeId(id, RefTypes.CATEGORIE_ACTIVITE, request);
        RefParamResponse response = RefParamMapper.INSTANCE.map(param);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/pays/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
    public ResponseEntity<RefParamResponse> UpdateByPaysAndById(@PathVariable Integer id, @RequestBody RefParamRequestWithoutRefTypeIdDto requestDto) throws RefParamNotFound {
        RefParamRequest request = RefParamMapper.INSTANCE.mapToRequest(requestDto);
        request.setRefType(RefTypes.PAYS);
        RefParam param = refParamService.UpdateRefParamByRefTypeId(id, RefTypes.PAYS, request);
        RefParamResponse response = RefParamMapper.INSTANCE.map(param);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/type-comite/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
    public ResponseEntity<RefParamResponse> UpdateByTypeComiteAndById(@PathVariable Integer id, @RequestBody RefParamRequestWithoutRefTypeIdDto requestDto) throws RefParamNotFound {
        RefParamRequest request = RefParamMapper.INSTANCE.mapToRequest(requestDto);
        request.setRefType(RefTypes.TYPE_COMITE);
        RefParam param = refParamService.UpdateRefParamByRefTypeId(id, RefTypes.TYPE_COMITE, request);
        RefParamResponse response = RefParamMapper.INSTANCE.map(param);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/type-address/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
    public ResponseEntity<RefParamResponse> UpdateByTypeAddressAndById(@PathVariable Integer id, @RequestBody RefParamRequestWithoutRefTypeIdDto requestDto) throws RefParamNotFound {
        RefParamRequest request = RefParamMapper.INSTANCE.mapToRequest(requestDto);
        request.setRefType(RefTypes.TYPE_ADDRESS);
        RefParam param = refParamService.UpdateRefParamByRefTypeId(id, RefTypes.TYPE_ADDRESS, request);
        RefParamResponse response = RefParamMapper.INSTANCE.map(param);
        return ResponseEntity.ok(response);
    }


    @PutMapping("/dac-category/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
    public ResponseEntity<RefParamResponse> UpdateByDacCategoryAndById(@PathVariable Integer id,@RequestBody RefParamRequestWithoutRefTypeIdDto requestDto) throws RefParamNotFound  {
        RefParamRequest request=RefParamMapper.INSTANCE.mapToRequest(requestDto);
        request.setRefType(RefTypes.CATEGORIE_DAC);
        RefParam param = refParamService.UpdateRefParamByRefTypeId(id,RefTypes.CATEGORIE_DAC,request);
        RefParamResponse response = RefParamMapper.INSTANCE.map(param);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/type-societe/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
    public ResponseEntity<RefParamResponse> deleteByTypeSocieteAndById(@PathVariable Integer id) throws RefParamHasAssociatedCom,
            RefParamHasAssociatedCompteBancaire,
            RefParamHasAssociatedSociete,
            RefParamHasAssociatedSocieteAddress, RefParamNotFound {
        refParamService.deleteByIdAndRefTypeId(id, RefTypes.TYPE_SOCIETE);
        RefParam deleted = refParamService.findDeletedById(id)
                .orElseThrow();
        RefParamResponse response = RefParamMapper.INSTANCE.map(deleted);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/service")
    public ResponseEntity<List<RefParamResponseDto>> findAllServices() throws RefTypeNotFound {
        List<RefParam> params = refParamService.findByRefType(RefTypes.SERVICE);
        List<RefParamResponseDto> responses = params.stream()
                .map(RefParamMapper.INSTANCE::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/direction")
    public ResponseEntity<List<RefParamResponseDto>> findAllDirections() throws RefTypeNotFound {
        List<RefParam> params = refParamService.findByRefType(RefTypes.DIRECTION);
        List<RefParamResponseDto> responses = params.stream()
                .map(RefParamMapper.INSTANCE::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/source-financement")
    public ResponseEntity<List<RefParamResponseDto>> findAllSourceFinancements() throws RefTypeNotFound {
        List<RefParam> params = refParamService.findByRefType(RefTypes.SOURCE_FINANCEMENT);
        List<RefParamResponseDto> responses = params.stream()
                .map(RefParamMapper.INSTANCE::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/monais")
    public ResponseEntity<List<RefParamResponseDto>> findAllMonais() throws RefTypeNotFound {
        List<RefParam> params = refParamService.findByRefType(RefTypes.MONAIS);
        List<RefParamResponseDto> responses = params.stream()
                .map(RefParamMapper.INSTANCE::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/type-marcher")
    public ResponseEntity<List<RefParamResponseDto>> findAllTypeMarchers() throws RefTypeNotFound {
        List<RefParam> params = refParamService.findByRefType(RefTypes.TYPE_MARCHER);
        List<RefParamResponseDto> responses = params.stream()
                .map(RefParamMapper.INSTANCE::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/mode-selection")
    public ResponseEntity<List<RefParamResponseDto>> findAllModeSelections() throws RefTypeNotFound {
        List<RefParam> params = refParamService.findByRefType(RefTypes.MODE_SELECTION);
        List<RefParamResponseDto> responses = params.stream()
                .map(RefParamMapper.INSTANCE::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }


    @DeleteMapping("/categorie-activite/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
    public ResponseEntity<RefParamResponse> deleteByCategorieActiviteAndById(@PathVariable Integer id) throws RefParamHasAssociatedCom,
            RefParamHasAssociatedCompteBancaire,
            RefParamHasAssociatedSociete,
            RefParamHasAssociatedSocieteAddress, RefParamNotFound {
        refParamService.deleteByIdAndRefTypeId(id, RefTypes.CATEGORIE_ACTIVITE);

        RefParam deleted = refParamService.findDeletedById(id)
                .orElseThrow();
        RefParamResponse response = RefParamMapper.INSTANCE.map(deleted);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/pays/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
    public ResponseEntity<RefParamResponse> deletePaysAndById(@PathVariable Integer id) throws RefParamHasAssociatedCom,
            RefParamHasAssociatedCompteBancaire,
            RefParamHasAssociatedSociete,
            RefParamHasAssociatedSocieteAddress, RefParamNotFound {
        refParamService.deleteByIdAndRefTypeId(id, RefTypes.PAYS);

        RefParam deleted = refParamService.findDeletedById(id)
                .orElseThrow();
        RefParamResponse response = RefParamMapper.INSTANCE.map(deleted);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/type-comite/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
    public ResponseEntity<RefParamResponse> deleteByTypeComiteAndById(@PathVariable Integer id) throws RefParamHasAssociatedCom,
            RefParamHasAssociatedCompteBancaire,
            RefParamHasAssociatedSociete,
            RefParamHasAssociatedSocieteAddress, RefParamNotFound {
        refParamService.deleteByIdAndRefTypeId(id, RefTypes.TYPE_COMITE);

        RefParam deleted = refParamService.findDeletedById(id)
                .orElseThrow();
        RefParamResponse response = RefParamMapper.INSTANCE.map(deleted);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/type-address/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
    public ResponseEntity<RefParamResponse> deleteByTypeAddressAndById(@PathVariable Integer id) throws RefParamHasAssociatedCom,
            RefParamHasAssociatedCompteBancaire,
            RefParamHasAssociatedSociete,
            RefParamHasAssociatedSocieteAddress, RefParamNotFound {
        refParamService.deleteByIdAndRefTypeId(id, RefTypes.TYPE_ADDRESS);

        RefParam deleted = refParamService.findDeletedById(id)
                .orElseThrow();
        RefParamResponse response = RefParamMapper.INSTANCE.map(deleted);
        return ResponseEntity.ok(response);
    }



    @DeleteMapping("/dac-category/{id}")
    public ResponseEntity<RefParamResponse> deleteByDacCategoryAndById(@PathVariable Integer id)
        throws HasAssociatedDynaDoc, RefParamNotFound {
        refParamService.deleteByIdAndRefTypeId(id,RefTypes.CATEGORIE_DAC);

        RefParam deleted = refParamService.findDeletedById(id)
                .orElseThrow();
        RefParamResponse response = RefParamMapper.INSTANCE.map(deleted);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/type-societe")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
    public ResponseEntity<RefParamResponse> createUnderTypeSociete(@RequestBody RefParamRequestWithoutRefTypeIdDto requestDto) throws RefTypeNotFound {
        RefParamRequest request = RefParamMapper.INSTANCE.mapToRequest(requestDto);
        request.setRefType(RefTypes.TYPE_SOCIETE);
        RefParam refParam = refParamService.createUnderRefType(RefTypes.TYPE_SOCIETE, request);
        RefParamResponse response = RefParamMapper.INSTANCE.map(refParam);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/dac-category")
    public ResponseEntity<RefParamResponse> createUnderDacCategory(@RequestBody RefParamRequestWithoutRefTypeIdDto requestDto)throws RefTypeNotFound {
        RefParamRequest request=RefParamMapper.INSTANCE.mapToRequest(requestDto);
        request.setRefType(RefTypes.CATEGORIE_DAC);
        RefParam refParam = refParamService.createUnderRefType(RefTypes.CATEGORIE_DAC,request);
        RefParamResponse response = RefParamMapper.INSTANCE.map(refParam);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/categorie-activite")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
    public ResponseEntity<RefParamResponse> createUnderActiviteAndById(@RequestBody RefParamRequestWithoutRefTypeIdDto requestDto) throws RefTypeNotFound {
        RefParamRequest request = RefParamMapper.INSTANCE.mapToRequest(requestDto);
        request.setRefType(RefTypes.CATEGORIE_ACTIVITE);
        RefParam refParam = refParamService.createUnderRefType(RefTypes.CATEGORIE_ACTIVITE, request);
        RefParamResponse response = RefParamMapper.INSTANCE.map(refParam);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/pays")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
    public ResponseEntity<RefParamResponse> createUnderPays(@RequestBody RefParamRequestWithoutRefTypeIdDto requestDto) throws RefTypeNotFound {
        RefParamRequest request = RefParamMapper.INSTANCE.mapToRequest(requestDto);
        request.setRefType(RefTypes.PAYS);
        RefParam refParam = refParamService.createUnderRefType(RefTypes.PAYS, request);
        RefParamResponse response = RefParamMapper.INSTANCE.map(refParam);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/type-comite")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
    public ResponseEntity<RefParamResponse> createUnderTypeComite(@RequestBody RefParamRequestWithoutRefTypeIdDto requestDto) throws RefTypeNotFound {
        RefParamRequest request = RefParamMapper.INSTANCE.mapToRequest(requestDto);
        request.setRefType(RefTypes.TYPE_COMITE);
        RefParam refParam = refParamService.createUnderRefType(RefTypes.TYPE_COMITE, request);
        RefParamResponse response = RefParamMapper.INSTANCE.map(refParam);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/type-address")
    @PreAuthorize("hasAnyRole('ADMIN_CENTRAL', 'SUPER_ADMIN')")
    public ResponseEntity<RefParamResponse> createUnderTypeAddress(@RequestBody RefParamRequestWithoutRefTypeIdDto requestDto) throws RefTypeNotFound {
        RefParamRequest request = RefParamMapper.INSTANCE.mapToRequest(requestDto);
        request.setRefType(RefTypes.TYPE_ADDRESS);
        RefParam refParam = refParamService.createUnderRefType(RefTypes.TYPE_ADDRESS, request);
        RefParamResponse response = RefParamMapper.INSTANCE.map(refParam);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // @PostMapping("/status")
    // public ResponseEntity<RefParamResponse> createUnderStatus(@RequestBody RefParamRequestWithoutRefTypeIdDto requestDto) throws RefTypeNotFound {
    //     RefParamRequest request = RefParamMapper.INSTANCE.mapToRequest(requestDto);
    //     request.setRefTypeId(Constants.STATUS);
    //     RefParam refParam = refParamService.createUnderRefType(Constants.STATUS, request);
    //     RefParamResponse response = RefParamMapper.INSTANCE.map(refParam);
    //     return ResponseEntity.status(HttpStatus.CREATED).body(response);
    // }

    @GetMapping("/mode-passation")
    public ResponseEntity<List<RefParamResponseDto>> findAllModePassation() throws RefTypeNotFound {
        List<RefParam> params = refParamService.findByRefType(RefTypes.MODE_PASSATION);
        List<RefParamResponseDto> responses = params.stream()
                .map(RefParamMapper.INSTANCE::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
    @GetMapping("/tutel")
    public ResponseEntity<List<RefParamResponseDto>> findAllTutel() throws RefTypeNotFound {
        List<RefParam> params = refParamService.findByRefType(RefTypes.TUTEL);
        List<RefParamResponseDto> responses = params.stream()
                .map(RefParamMapper.INSTANCE::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
    @GetMapping("/Imputation_budgetaire")
    public ResponseEntity<List<RefParamResponseDto>> findImputationBudgetaire() throws RefTypeNotFound {
        List<RefParam> params = refParamService.findByRefType(RefTypes.IMPLUMENTATION_BUDGETAIRE);
        List<RefParamResponseDto> responses = params.stream()
                .map(RefParamMapper.INSTANCE::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/organisme_de_delivrance_agrements")
    public ResponseEntity<List<RefParamResponseDto>> findOrganismeDeDelivranceAgrements() throws RefTypeNotFound {
        List<RefParam> params = refParamService.findByRefType(RefTypes.ORGANISME_DE_DELIVRANCE_AGREMENTS);
        List<RefParamResponseDto> responses = params.stream()
                .map(RefParamMapper.INSTANCE::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

}
