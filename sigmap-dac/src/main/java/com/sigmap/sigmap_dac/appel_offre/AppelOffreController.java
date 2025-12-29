package com.sigmap.sigmap_dac.appel_offre;

import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.appel_offre.AppelOffre;
import com.sigmap.common.appel_offre.AppelOffreService;
import com.sigmap.common.appel_offre.mapper.AppelOffreMapper;
import com.sigmap.common.appel_offre.request.AppelOffreEditRequest;
import com.sigmap.common.appel_offre.request.CreateAppelOffreRequest;
import com.sigmap.common.appel_offre.response.AppelOffreQuerry;
import com.sigmap.common.appel_offre.response.AppelOffreResponse;
import com.sigmap.common.appel_offre.response.AppelOffreResponseById;
import com.sigmap.common.ppms.Ppm;
import com.sigmap.common.ppms.exception.PpmNotFound;
import com.sigmap.common.ppms.mappers.PpmMapper;
import com.sigmap.common.ppms.response.PpmResponseSoftDtoWithMetaData;
import com.sigmap.common.realisations.Realisation;
import com.sigmap.common.realisations.exceptions.RealisationNotFoundException;
import com.sigmap.common.realisations.mappers.RealisationMapper;
import com.sigmap.common.realisations.response.RealisationResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/appel-offre")
@Tag(name = "Appel Offre", description = "Operations for Appel d'Offre")
public class AppelOffreController {

    private final AppelOffreService appelOffreService;

    public AppelOffreController(AppelOffreService appelOffreService) {
        this.appelOffreService = appelOffreService;
    }

    @GetMapping
    public ResponseEntity<Page<AppelOffreResponse>> getAllAppelOffre(AppelOffreQuerry query, Pageable pageable) {
        Page<AppelOffre> appelOffre = appelOffreService.findAll(query.toSpecification(), pageable);
        Page<AppelOffreResponse> response = appelOffre.map(AppelOffreMapper.INSTANCE::map);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @GetMapping("/{id}")
    public ResponseEntity<AppelOffreResponseById> getAppelOffreById(@PathVariable Integer id) throws PpmNotFound, AcNotFound {
        AppelOffreResponseById appelOffre = appelOffreService.findById(id);
        return ResponseEntity.status(HttpStatus.CREATED).body(appelOffre);

    }

    @PostMapping
    public ResponseEntity<AppelOffreResponse> create(@RequestBody CreateAppelOffreRequest request) throws RealisationNotFoundException {
        AppelOffre appelOffre = appelOffreService.create(request);
        AppelOffreResponse response = AppelOffreMapper.INSTANCE.map(appelOffre);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AppelOffreResponse> Update(@PathVariable Integer id,@RequestBody AppelOffreEditRequest request) {
        AppelOffre appelOffre = appelOffreService.update(id,request);
        AppelOffreResponse response = AppelOffreMapper.INSTANCE.map(appelOffre);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }



    @GetMapping("/ppm")
    public ResponseEntity<List<PpmResponseSoftDtoWithMetaData>> getAllPpmsWithAcsforConnectedUser() {

        List<Ppm> ppms = appelOffreService.getAllPpmsWithAcsforConnectedUser();
        List<PpmResponseSoftDtoWithMetaData> ppmResponseDtoWithMetdata =
                ppms.stream()
                        .map(PpmMapper.INSTANCE::mapToDtoWithMetaData)
                        .collect(Collectors.toList());
        return ResponseEntity.ok(ppmResponseDtoWithMetdata);
    }

    @GetMapping("/ppm/realisation/{ppmId}/{ppmVer}")
    public ResponseEntity<List<RealisationResponseDTO>> getRealisationsFromSelectedPpm(@PathVariable Integer ppmId, @PathVariable Integer ppmVer) {

        List<Realisation> realisations =
                appelOffreService.getRealisationsNotRelatedToAppelOffreWithStatusCreeOrCloturer(ppmId, ppmVer);

        List<RealisationResponseDTO> dtoList = realisations.stream()
                        .map(RealisationMapper.INSTANCE::mapWithDTO).toList();

        return ResponseEntity.ok(dtoList);
    }

}
