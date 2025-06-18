package com.sigmap.sigmap_params.direction;

import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.direction.Direction;
import com.sigmap.common.direction.DirectionService;
import com.sigmap.common.direction.exception.DirectionNotFound;
import com.sigmap.common.direction.mappers.DirectionMapper;
import com.sigmap.common.direction.request.DirectionQuery;
import com.sigmap.common.direction.request.DirectionRequest;
import com.sigmap.common.direction.request.DirectionunderAcRequest;
import com.sigmap.common.direction.response.DirectionResponse;
import com.sigmap.common.direction.response.DirectionResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/api/direction")
public class DirectionController {
    private final DirectionService directionService;
    public DirectionController(DirectionService directionService) {
        this.directionService = directionService;
    }

    /**
     * Return a page of Direction based on the given specification and pageable.
     *
     * @param query the query to use
     * @param pageable the pageable to use
     * @return a page of Direction
     */
    @GetMapping
    public ResponseEntity<Page<DirectionResponseDto>> findAll(
            DirectionQuery query, Pageable pageable) {
        Page<Direction> params = directionService.findAll(query.toSpecification(), pageable);
        Page<DirectionResponseDto> acResponses = params.map(DirectionMapper.INSTANCE::mapToDto);
        return ResponseEntity.ok(acResponses);
    }

    /**
     * Return a page of deleted Direction based on the given specification and pageable.
     *
     * @param query the query to use
     * @param pageable the pageable to use
     * @return a page of deleted Direction
     */
    @GetMapping("/deleted")
    public ResponseEntity<Page<DirectionResponseDto>> findAllDeleted(
            DirectionQuery query, Pageable pageable) {
        Page<Direction> params = directionService.findAllDeleted(query.toSpecification(), pageable);
        Page<DirectionResponseDto> acResponses = params.map(DirectionMapper.INSTANCE::mapToDto);
        return ResponseEntity.ok(acResponses);
    }

    /**
     * Return a Direction based on the given id.
     *
     * @param id the id of the Direction to return
     * @return the Direction
     */
    @GetMapping("/{id}")
    public ResponseEntity<DirectionResponse> findById(@PathVariable Integer id) throws DirectionNotFound {
        Direction param = directionService.findById(id);
        DirectionResponse response = DirectionMapper.INSTANCE.map(param);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a Direction based on the given id.
     *
     * @param id the id of the Direction to delete
     * @return the deleted Direction
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<DirectionResponse> deleteById(@PathVariable Integer id) {
        directionService.delete(id);

        Direction deleted = directionService.findDeletedById(id)
                .orElseThrow();
        DirectionResponse response = DirectionMapper.INSTANCE.map(deleted);
        return ResponseEntity.ok(response);
    }

    // create under Ac with acId
    @PostMapping("/ac/{acId}")
    public ResponseEntity<DirectionResponse> createUnderAc(@PathVariable Integer acId, @RequestBody DirectionRequest request) throws AcNotFound {
        Direction direction = directionService.createUnderAc(acId,request);
        DirectionResponse response = DirectionMapper.INSTANCE.map(direction);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // create under Ac with acId
    @PutMapping("/ac/{acId}/{id}")
    public ResponseEntity<DirectionResponse> updateUnderAc(@PathVariable Integer acId,@PathVariable Integer id, @RequestBody DirectionRequest request) throws AcNotFound {
        Direction direction = directionService.updateUnderAc(acId,id,request);
        DirectionResponse response = DirectionMapper.INSTANCE.map(direction);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    //get all under Ac
    @GetMapping("/ac/{acId}")
    public ResponseEntity<Page<DirectionResponseDto>> findAllUnderAc(
            @PathVariable Integer acId, Pageable pageable) throws AcNotFound {
        Page <Direction> params = directionService.findAllunderAc(acId, pageable);
        Page<DirectionResponseDto> acResponses = params.map(DirectionMapper.INSTANCE::mapToDto);
        return ResponseEntity.ok(acResponses);
    }
}
