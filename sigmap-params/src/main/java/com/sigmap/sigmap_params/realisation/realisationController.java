package com.sigmap.sigmap_params.realisation;

import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.ppms.Ppm;
import com.sigmap.common.ppms.exception.PpmNotFound;
import com.sigmap.common.ppms.mappers.PpmMapper;
import com.sigmap.common.ppms.request.PpmQuery;
import com.sigmap.common.ppms.response.PpmResponseDto;
import com.sigmap.common.realisations.Realisation;
import com.sigmap.common.realisations.RealisationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/realisation")
@Slf4j
@AllArgsConstructor
public class realisationController {

    private final RealisationService realisationService;



    @GetMapping("/for-dac/{ppmId}/{ppmVer}")
    public ResponseEntity<List<Realisation>> findAllPubliee(@PathVariable Integer ppmId,
                                                            @PathVariable Integer ppmVer) throws PpmNotFound, AcNotFound {



        List<Realisation> realisations = realisationService.findRealisationsNotAttachedToDac ( ppmId, ppmVer);
        log.info("Found for ppm with id {} and version {} ------------------  {} realisations", ppmId, ppmVer,realisations.size());
        return ResponseEntity.ok(realisations);
    }

}
