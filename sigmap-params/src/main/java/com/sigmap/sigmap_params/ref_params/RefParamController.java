package com.sigmap.sigmap_params.ref_params;

import com.sigmap.common.config.Constants;
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
     * @param query the query to use
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
     * @param query the query to use
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
    public ResponseEntity<RefParamResponse> create(@RequestBody RefParamRequest request)throws RefTypeNotFound {

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
    public ResponseEntity<RefParamResponse> update(@PathVariable Integer id,@RequestBody RefParamRequest request) throws RefParamNotFound, RefTypeNotFound {
        RefParam refParam = refParamService.update(id,request);
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
    public ResponseEntity<RefParamResponse> deleteById(@PathVariable Integer id)throws RefParamHasAssociatedCom,
            RefParamHasAssociatedCompteBancaire,
            RefParamHasAssociatedSociete,
            RefParamHasAssociatedSocieteAddress {
        refParamService.delete(id);

        RefParam deleted = refParamService.findDeletedById(id)
                .orElseThrow();
        RefParamResponse response = RefParamMapper.INSTANCE.map(deleted);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/type-societe")
    public ResponseEntity<List<RefParamResponseDto>> findAllTypeSociete() throws RefTypeNotFound  {
        List<RefParam> params = refParamService.findByRefType(Constants.TYPE_SOCIETE);
        List<RefParamResponseDto> responses = params.stream()
                .map(RefParamMapper.INSTANCE::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/categorie-soummisionaire")
    public ResponseEntity<List<RefParamResponseDto>> findAllCategorieSoumissionaire() throws RefTypeNotFound  {
        List<RefParam> params = refParamService.findByRefType(Constants.CATEGORIE_SOMISSIONAIRE);
        List<RefParamResponseDto> responses = params.stream()
                .map(RefParamMapper.INSTANCE::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/pays")
    public ResponseEntity<List<RefParamResponseDto>> findAllPays() throws RefTypeNotFound  {
        List<RefParam> params = refParamService.findByRefType(Constants.PAYS);
        List<RefParamResponseDto> responses = params.stream()
                .map(RefParamMapper.INSTANCE::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/type-comite")
    public ResponseEntity<List<RefParamResponseDto>> findAllTypeComite() throws RefTypeNotFound  {
        List<RefParam> params = refParamService.findByRefType(Constants.TYPE_COMITE);
        List<RefParamResponseDto> responses = params.stream()
                .map(RefParamMapper.INSTANCE::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/type-address")
    public ResponseEntity<List<RefParamResponseDto>> findAllTypeAddres() throws RefTypeNotFound  {
        List<RefParam> params = refParamService.findByRefType(Constants.TYPE_ADDRESS);
        List<RefParamResponseDto> responses = params.stream()
                .map(RefParamMapper.INSTANCE::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/status")
    public ResponseEntity<List<RefParamResponseDto>> findAllStatus() throws RefTypeNotFound  {
        List<RefParam> params = refParamService.findByRefType(Constants.STATUS);
        List<RefParamResponseDto> responses = params.stream()
                .map(RefParamMapper.INSTANCE::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/dac-category")
    public ResponseEntity<List<RefParamResponseDto>> findAllDacCategory() throws RefTypeNotFound  {
        List<RefParam> params = refParamService.findByRefType(Constants.CATEGORIE_DAC);
        List<RefParamResponseDto> responses = params.stream()
                .map(RefParamMapper.INSTANCE::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/type-dyna-field")
    public ResponseEntity<List<RefParamResponseDto>> findAllTypeDynaField() throws RefTypeNotFound  {
        List<RefParam> params = refParamService.findByRefType(Constants.TYPE_FIELD_DAC);
        List<RefParamResponseDto> responses = params.stream()
                .map(RefParamMapper.INSTANCE::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/type-societe/{id}")
    public ResponseEntity<RefParamResponse> findByTypeSocieteAndById(@PathVariable Integer id) throws RefParamNotFound  {
       RefParam param = refParamService.findByIdAndRefTypeId(id,Constants.TYPE_SOCIETE);
        RefParamResponse response = RefParamMapper.INSTANCE.map(param);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/categorie-soummisionnaire/{id}")
    public ResponseEntity<RefParamResponse> findByCategorieSoumissionnaireAndById(@PathVariable Integer id) throws RefParamNotFound  {
        RefParam param = refParamService.findByIdAndRefTypeId(id,Constants.CATEGORIE_SOMISSIONAIRE);
        RefParamResponse response = RefParamMapper.INSTANCE.map(param);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/pays/{id}")
    public ResponseEntity<RefParamResponse> findByPaysAndById(@PathVariable Integer id) throws RefParamNotFound  {
        RefParam param = refParamService.findByIdAndRefTypeId(id,Constants.PAYS);
        RefParamResponse response = RefParamMapper.INSTANCE.map(param);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/type-comite/{id}")
    public ResponseEntity<RefParamResponse> findByTypeComiteAndById(@PathVariable Integer id) throws RefParamNotFound  {
        RefParam param = refParamService.findByIdAndRefTypeId(id,Constants.TYPE_COMITE);
        RefParamResponse response = RefParamMapper.INSTANCE.map(param);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/type-address/{id}")
    public ResponseEntity<RefParamResponse> findByTypeAddressAndById(@PathVariable Integer id) throws RefParamNotFound  {
        RefParam param = refParamService.findByIdAndRefTypeId(id,Constants.TYPE_ADDRESS);
        RefParamResponse response = RefParamMapper.INSTANCE.map(param);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/status/{id}")
    public ResponseEntity<RefParamResponse> findByStatusAndById(@PathVariable Integer id) throws RefParamNotFound  {
        RefParam param = refParamService.findByIdAndRefTypeId(id,Constants.STATUS);
        RefParamResponse response = RefParamMapper.INSTANCE.map(param);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/dac-category/{id}")
    public ResponseEntity<RefParamResponse> findByDacCategoryAndById(@PathVariable Integer id) throws RefParamNotFound  {
        RefParam param = refParamService.findByIdAndRefTypeId(id,Constants.CATEGORIE_DAC);
        RefParamResponse response = RefParamMapper.INSTANCE.map(param);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/type-dyna-field/{id}")
    public ResponseEntity<RefParamResponse> findByTypeDynaFieldAndById(@PathVariable Integer id) throws RefParamNotFound  {
        RefParam param = refParamService.findByIdAndRefTypeId(id,Constants.TYPE_FIELD_DAC);
        RefParamResponse response = RefParamMapper.INSTANCE.map(param);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/type-societe/{id}")
    public ResponseEntity<RefParamResponse> UpdateByTypeSocieteAndById(@PathVariable Integer id,@RequestBody RefParamRequestWithoutRefTypeIdDto requestDto) throws RefParamNotFound  {
        RefParamRequest request=RefParamMapper.INSTANCE.mapToRequest(requestDto);
        request.setRefTypeId(Constants.TYPE_SOCIETE);
        RefParam param = refParamService.UpdateRefParamByRefTypeId(id,Constants.TYPE_SOCIETE,request);
        RefParamResponse response = RefParamMapper.INSTANCE.map(param);
        return ResponseEntity.ok(response);
    }
    @PutMapping("/categorie-soummisionnaire/{id}")
    public ResponseEntity<RefParamResponse> UpdateByCategorieSoumissionnaireAndById(@PathVariable Integer id,@RequestBody RefParamRequestWithoutRefTypeIdDto requestDto) throws RefParamNotFound  {
        RefParamRequest request=RefParamMapper.INSTANCE.mapToRequest(requestDto);
        request.setRefTypeId(Constants.CATEGORIE_SOMISSIONAIRE);
        RefParam param = refParamService.UpdateRefParamByRefTypeId(id,Constants.CATEGORIE_SOMISSIONAIRE,request);
        RefParamResponse response = RefParamMapper.INSTANCE.map(param);
        return ResponseEntity.ok(response);
    }
    @PutMapping("/pays/{id}")
    public ResponseEntity<RefParamResponse> UpdateByPaysAndById(@PathVariable Integer id,@RequestBody RefParamRequestWithoutRefTypeIdDto requestDto) throws RefParamNotFound  {
        RefParamRequest request=RefParamMapper.INSTANCE.mapToRequest(requestDto);
        request.setRefTypeId(Constants.PAYS);
        RefParam param = refParamService.UpdateRefParamByRefTypeId(id,Constants.PAYS,request);
        RefParamResponse response = RefParamMapper.INSTANCE.map(param);
        return ResponseEntity.ok(response);
    }
    @PutMapping("/type-comite/{id}")
    public ResponseEntity<RefParamResponse> UpdateByTypeComiteAndById(@PathVariable Integer id,@RequestBody RefParamRequestWithoutRefTypeIdDto requestDto) throws RefParamNotFound  {
        RefParamRequest request=RefParamMapper.INSTANCE.mapToRequest(requestDto);
        request.setRefTypeId(Constants.TYPE_COMITE);
        RefParam param = refParamService.UpdateRefParamByRefTypeId(id,Constants.TYPE_COMITE,request);
        RefParamResponse response = RefParamMapper.INSTANCE.map(param);
        return ResponseEntity.ok(response);
    }
    @PutMapping("/type-address/{id}")
    public ResponseEntity<RefParamResponse> UpdateByTypeAddressAndById(@PathVariable Integer id,@RequestBody RefParamRequestWithoutRefTypeIdDto requestDto) throws RefParamNotFound  {
        RefParamRequest request=RefParamMapper.INSTANCE.mapToRequest(requestDto);
        request.setRefTypeId(Constants.TYPE_ADDRESS);
        RefParam param = refParamService.UpdateRefParamByRefTypeId(id,Constants.TYPE_ADDRESS,request);
        RefParamResponse response = RefParamMapper.INSTANCE.map(param);
        return ResponseEntity.ok(response);
    }
    @PutMapping("/status/{id}")
    public ResponseEntity<RefParamResponse> UpdateByStatusAndById(@PathVariable Integer id,@RequestBody RefParamRequestWithoutRefTypeIdDto requestDto) throws RefParamNotFound  {
        RefParamRequest request=RefParamMapper.INSTANCE.mapToRequest(requestDto);
        request.setRefTypeId(Constants.STATUS);
        RefParam param = refParamService.UpdateRefParamByRefTypeId(id,Constants.STATUS,request);
        RefParamResponse response = RefParamMapper.INSTANCE.map(param);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/dac-category/{id}")
    public ResponseEntity<RefParamResponse> UpdateByDacCategoryAndById(@PathVariable Integer id,@RequestBody RefParamRequestWithoutRefTypeIdDto requestDto) throws RefParamNotFound  {
        RefParamRequest request=RefParamMapper.INSTANCE.mapToRequest(requestDto);
        request.setRefTypeId(Constants.CATEGORIE_DAC);
        RefParam param = refParamService.UpdateRefParamByRefTypeId(id,Constants.CATEGORIE_DAC,request);
        RefParamResponse response = RefParamMapper.INSTANCE.map(param);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/type-dyna-field/{id}")
    public ResponseEntity<RefParamResponse> UpdateByTypeDynaFieldAndById(@PathVariable Integer id,@RequestBody RefParamRequestWithoutRefTypeIdDto requestDto) throws RefParamNotFound  {
        RefParamRequest request=RefParamMapper.INSTANCE.mapToRequest(requestDto);
        request.setRefTypeId(Constants.TYPE_FIELD_DAC);
        RefParam param = refParamService.UpdateRefParamByRefTypeId(id,Constants.TYPE_FIELD_DAC, request);
        RefParamResponse response = RefParamMapper.INSTANCE.map(param);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/type-societe/{id}")
    public ResponseEntity<RefParamResponse> deleteByTypeSocieteAndById(@PathVariable Integer id) throws RefParamHasAssociatedCom,
            RefParamHasAssociatedCompteBancaire,
            RefParamHasAssociatedSociete,
            RefParamHasAssociatedSocieteAddress, RefParamNotFound {
        refParamService.deleteByIdAndRefTypeId(id,Constants.TYPE_SOCIETE);
        RefParam deleted = refParamService.findDeletedById(id)
                .orElseThrow();
        RefParamResponse response = RefParamMapper.INSTANCE.map(deleted);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/service")
    public ResponseEntity<List<RefParamResponseDto>> findAllServices() throws RefTypeNotFound  {
        List<RefParam> params = refParamService.findByRefType(Constants.SERVICE);
        List<RefParamResponseDto> responses = params.stream()
                .map(RefParamMapper.INSTANCE::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
    @GetMapping("/direction")
    public ResponseEntity<List<RefParamResponseDto>> findAllDirections() throws RefTypeNotFound  {
        List<RefParam> params = refParamService.findByRefType(Constants.DIRECTION);
        List<RefParamResponseDto> responses = params.stream()
                .map(RefParamMapper.INSTANCE::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
    @GetMapping("/source-financement")
    public ResponseEntity<List<RefParamResponseDto>> findAllSourceFinancements() throws RefTypeNotFound  {
        List<RefParam> params = refParamService.findByRefType(Constants.SOURCE_FINANCEMENT);
        List<RefParamResponseDto> responses = params.stream()
                .map(RefParamMapper.INSTANCE::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
    @GetMapping("/monais")
    public ResponseEntity<List<RefParamResponseDto>> findAllMonais() throws RefTypeNotFound  {
        List<RefParam> params = refParamService.findByRefType(Constants.MONAIS);
        List<RefParamResponseDto> responses = params.stream()
                .map(RefParamMapper.INSTANCE::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
    @GetMapping("/type-marcher")
    public ResponseEntity<List<RefParamResponseDto>> findAllTypeMarchers() throws RefTypeNotFound  {
        List<RefParam> params = refParamService.findByRefType(Constants.TYPE_MARCHER);
        List<RefParamResponseDto> responses = params.stream()
                .map(RefParamMapper.INSTANCE::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
    @GetMapping("/mode-selection")
    public ResponseEntity<List<RefParamResponseDto>> findAllModeSelections() throws RefTypeNotFound  {
        List<RefParam> params = refParamService.findByRefType(Constants.MODE_SELECTION);
        List<RefParamResponseDto> responses = params.stream()
                .map(RefParamMapper.INSTANCE::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }


    @DeleteMapping("/categorie-soummisionnaire/{id}")
    public ResponseEntity<RefParamResponse> deleteByCategorieSoumissionnaireAndById(@PathVariable Integer id) throws RefParamHasAssociatedCom,
            RefParamHasAssociatedCompteBancaire,
            RefParamHasAssociatedSociete,
            RefParamHasAssociatedSocieteAddress, RefParamNotFound {
        refParamService.deleteByIdAndRefTypeId(id,Constants.CATEGORIE_SOMISSIONAIRE);

        RefParam deleted = refParamService.findDeletedById(id)
                .orElseThrow();
        RefParamResponse response = RefParamMapper.INSTANCE.map(deleted);
        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/pays/{id}")
    public ResponseEntity<RefParamResponse> deletePaysAndById(@PathVariable Integer id) throws RefParamHasAssociatedCom,
            RefParamHasAssociatedCompteBancaire,
            RefParamHasAssociatedSociete,
            RefParamHasAssociatedSocieteAddress, RefParamNotFound {
        refParamService.deleteByIdAndRefTypeId(id,Constants.PAYS);

        RefParam deleted = refParamService.findDeletedById(id)
                .orElseThrow();
        RefParamResponse response = RefParamMapper.INSTANCE.map(deleted);
        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/type-comite/{id}")
    public ResponseEntity<RefParamResponse> deleteByTypeComiteAndById(@PathVariable Integer id) throws RefParamHasAssociatedCom,
            RefParamHasAssociatedCompteBancaire,
            RefParamHasAssociatedSociete,
            RefParamHasAssociatedSocieteAddress, RefParamNotFound {
        refParamService.deleteByIdAndRefTypeId(id,Constants.TYPE_COMITE);

        RefParam deleted = refParamService.findDeletedById(id)
                .orElseThrow();
        RefParamResponse response = RefParamMapper.INSTANCE.map(deleted);
        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/type-address/{id}")
    public ResponseEntity<RefParamResponse> deleteByTypeAddressAndById(@PathVariable Integer id) throws RefParamHasAssociatedCom,
            RefParamHasAssociatedCompteBancaire,
            RefParamHasAssociatedSociete,
            RefParamHasAssociatedSocieteAddress, RefParamNotFound {
        refParamService.deleteByIdAndRefTypeId(id,Constants.TYPE_ADDRESS);

        RefParam deleted = refParamService.findDeletedById(id)
                .orElseThrow();
        RefParamResponse response = RefParamMapper.INSTANCE.map(deleted);
        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/status/{id}")
    public ResponseEntity<RefParamResponse> deleteByStatusAndById(@PathVariable Integer id) throws RefParamHasAssociatedCom,
            RefParamHasAssociatedCompteBancaire,
            RefParamHasAssociatedSociete,
            RefParamHasAssociatedSocieteAddress, RefParamNotFound {
        refParamService.deleteByIdAndRefTypeId(id,Constants.STATUS);

        RefParam deleted = refParamService.findDeletedById(id)
                .orElseThrow();
        RefParamResponse response = RefParamMapper.INSTANCE.map(deleted);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/dac-category/{id}")
    public ResponseEntity<RefParamResponse> deleteByDacCategoryAndById(@PathVariable Integer id) 
        throws HasAssociatedDynaDoc, RefParamNotFound {
        refParamService.deleteByIdAndRefTypeId(id,Constants.CATEGORIE_DAC);

        RefParam deleted = refParamService.findDeletedById(id)
                .orElseThrow();
        RefParamResponse response = RefParamMapper.INSTANCE.map(deleted);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/type-dyna-field/{id}")
    public ResponseEntity<RefParamResponse> deleteByTypeDynaFieldAndById(@PathVariable Integer id) 
        throws HasAssociatedDynaDoc, RefParamNotFound {
        refParamService.deleteByIdAndRefTypeId(id,Constants.TYPE_FIELD_DAC);

        RefParam deleted = refParamService.findDeletedById(id)
                .orElseThrow();
        RefParamResponse response = RefParamMapper.INSTANCE.map(deleted);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/type-societe")
    public ResponseEntity<RefParamResponse> createUnderTypeSociete(@RequestBody RefParamRequestWithoutRefTypeIdDto requestDto)throws RefTypeNotFound {
        RefParamRequest request=RefParamMapper.INSTANCE.mapToRequest(requestDto);
        request.setRefTypeId(Constants.TYPE_SOCIETE);
        RefParam refParam = refParamService.createUnderRefType(Constants.TYPE_SOCIETE,request);
        RefParamResponse response = RefParamMapper.INSTANCE.map(refParam);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @PostMapping("/categorie-soummisionnaire")
    public ResponseEntity<RefParamResponse> createUnderCategorieSoumissionnaireAndById(@RequestBody RefParamRequestWithoutRefTypeIdDto requestDto)throws RefTypeNotFound {
        RefParamRequest request=RefParamMapper.INSTANCE.mapToRequest(requestDto);
        request.setRefTypeId(Constants.CATEGORIE_SOMISSIONAIRE);
        RefParam refParam = refParamService.createUnderRefType(Constants.CATEGORIE_SOMISSIONAIRE,request);
        RefParamResponse response = RefParamMapper.INSTANCE.map(refParam);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @PostMapping("/pays")
    public ResponseEntity<RefParamResponse> createUnderPays(@RequestBody RefParamRequestWithoutRefTypeIdDto requestDto)throws RefTypeNotFound {
        RefParamRequest request=RefParamMapper.INSTANCE.mapToRequest(requestDto);
        request.setRefTypeId(Constants.PAYS);
        RefParam refParam = refParamService.createUnderRefType(Constants.PAYS,request);
        RefParamResponse response = RefParamMapper.INSTANCE.map(refParam);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @PostMapping("/type-comite")
    public ResponseEntity<RefParamResponse> createUnderTypeComite(@RequestBody RefParamRequestWithoutRefTypeIdDto requestDto)throws RefTypeNotFound {
        RefParamRequest request=RefParamMapper.INSTANCE.mapToRequest(requestDto);
        request.setRefTypeId(Constants.TYPE_COMITE);
        RefParam refParam = refParamService.createUnderRefType(Constants.TYPE_COMITE,request);
        RefParamResponse response = RefParamMapper.INSTANCE.map(refParam);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @PostMapping("/type-address")
    public ResponseEntity<RefParamResponse> createUnderTypeAddress(@RequestBody RefParamRequestWithoutRefTypeIdDto requestDto)throws RefTypeNotFound {
        RefParamRequest request=RefParamMapper.INSTANCE.mapToRequest(requestDto);
        request.setRefTypeId(Constants.TYPE_ADDRESS);
        RefParam refParam = refParamService.createUnderRefType(Constants.TYPE_ADDRESS,request);
        RefParamResponse response = RefParamMapper.INSTANCE.map(refParam);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @PostMapping("/status")
    public ResponseEntity<RefParamResponse> createUnderStatus(@RequestBody RefParamRequestWithoutRefTypeIdDto requestDto)throws RefTypeNotFound {
        RefParamRequest request=RefParamMapper.INSTANCE.mapToRequest(requestDto);
        request.setRefTypeId(Constants.STATUS);
        RefParam refParam = refParamService.createUnderRefType(Constants.STATUS,request);
        RefParamResponse response = RefParamMapper.INSTANCE.map(refParam);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/dac-category")
    public ResponseEntity<RefParamResponse> createUnderDacCategory(@RequestBody RefParamRequestWithoutRefTypeIdDto requestDto)throws RefTypeNotFound {
        RefParamRequest request=RefParamMapper.INSTANCE.mapToRequest(requestDto);
        request.setRefTypeId(Constants.CATEGORIE_DAC);
        RefParam refParam = refParamService.createUnderRefType(Constants.CATEGORIE_DAC,request);
        RefParamResponse response = RefParamMapper.INSTANCE.map(refParam);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/type-dyna-field")
    public ResponseEntity<RefParamResponse> createUnderTypeDynaField(@RequestBody RefParamRequestWithoutRefTypeIdDto requestDto)throws RefTypeNotFound {
        RefParamRequest request=RefParamMapper.INSTANCE.mapToRequest(requestDto);
        request.setRefTypeId(Constants.TYPE_FIELD_DAC);
        RefParam refParam = refParamService.createUnderRefType(Constants.TYPE_FIELD_DAC,request);
        RefParamResponse response = RefParamMapper.INSTANCE.map(refParam);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
