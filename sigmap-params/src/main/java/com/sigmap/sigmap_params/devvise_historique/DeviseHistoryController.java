package com.sigmap.sigmap_params.devvise_historique;

import com.sigmap.common.devise_history.DeviseHistory;
import com.sigmap.common.devise_history.DeviseHistoryService;
import com.sigmap.common.devise_history.request.DeviseHistoryQuerry;
import com.sigmap.common.devise_history.response.DeviseHistoryResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/devise_historique")
@AllArgsConstructor
@Slf4j
public class DeviseHistoryController {

    private final DeviseHistoryService deviseHistoryService;


    @GetMapping
    public ResponseEntity<Page<DeviseHistoryResponse>> findAll(DeviseHistoryQuerry query, Pageable pageable) {
        Page<DeviseHistory> responses = deviseHistoryService.findAll(query.toSpecification(), pageable);
        Page<DeviseHistoryResponse> resp = responses.map(deviseHistoryService::entityToResponse);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{deviseId}")
    public ResponseEntity<Page<DeviseHistoryResponse>> findAllByCurrencyId(@PathVariable Integer deviseId, DeviseHistoryQuerry query, Pageable pageable) {
        Page<DeviseHistory> responses = deviseHistoryService.findAllByCurrencyId(deviseId,query.toSpecification(), pageable);
        Page<DeviseHistoryResponse> resp = responses.map(deviseHistoryService::entityToResponse);
        return ResponseEntity.ok(resp);
    }
}
