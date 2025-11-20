package com.sigmap.sigmap_params.user_historique_ac;

import com.sigmap.common.user_historique_ac.UserAcHistorique;
import com.sigmap.common.user_historique_ac.UserAcHistoriqueService;
import com.sigmap.common.user_historique_ac.mapper.UserAcHistoriqueMapper;
import com.sigmap.common.user_historique_ac.request.UserAcHistoriqueQuery;
import com.sigmap.common.user_historique_ac.response.UserAcHistoriqueResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user-ac-historique")
@RequiredArgsConstructor
@Slf4j
public class UserAcHistoriqueController {

    private final UserAcHistoriqueService userAcHistoriqueService;

    @GetMapping({"/{acId}"})
    public ResponseEntity<Page<UserAcHistoriqueResponse>> findAllByAcIdAndDate(
            @PathVariable Integer acId, @RequestParam(name = "date", required = false) String date,
            UserAcHistoriqueQuery query, 
            Pageable pageable) {
        
        log.info("Fetching UserAcHistorique with date: {}, query: {}, pageable: {}", date, query, pageable);
        
        Page<UserAcHistorique> responses = userAcHistoriqueService.findAllByAcIdAndDate(acId,date, query.toSpecification(), pageable);
        Page<UserAcHistoriqueResponse> responsePage = responses.map(userAcHistoriqueService::entityToResponse);
        
        return ResponseEntity.ok(responsePage);
    }
}
