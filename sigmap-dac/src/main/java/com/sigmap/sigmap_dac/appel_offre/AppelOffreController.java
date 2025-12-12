package com.sigmap.sigmap_dac.appel_offre;

import com.sigmap.common.appel_offre.AppelOffre;
import com.sigmap.common.appel_offre.AppelOffreService;
import com.sigmap.common.appel_offre.mapper.AppelOffreMapper;
import com.sigmap.common.appel_offre.request.CreateAppelOffreRequest;
import com.sigmap.common.appel_offre.response.AppelOffreQuerry;
import com.sigmap.common.appel_offre.response.AppelOffreResponse;
import com.sigmap.common.authority.exeption.AuthorityNotFound;
import com.sigmap.common.dac_docs.response.customResponceDTO.DacDocResponseDTO;
import com.sigmap.common.menu_item.MenuItem;
import com.sigmap.common.menu_item.exception.MenuItemAlreadyExistsExeption;
import com.sigmap.common.menu_item.exception.MenuItemNotFound;
import com.sigmap.common.menu_item.mappers.MenuItemMapper;
import com.sigmap.common.menu_item.request.MenuItemQuery;
import com.sigmap.common.menu_item.request.MenuItemRequest;
import com.sigmap.common.menu_item.response.MenuItemResponse;
import com.sigmap.common.realisations.exceptions.RealisationNotFoundException;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<AppelOffreResponse> getAppelOffreById(@PathVariable Integer id) {
        AppelOffre appelOffre = appelOffreService.findById(id);
        AppelOffreResponse response = AppelOffreMapper.INSTANCE.map(appelOffre);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @PostMapping
    public ResponseEntity<AppelOffreResponse> create(@RequestBody CreateAppelOffreRequest request) throws RealisationNotFoundException {
        AppelOffre appelOffre = appelOffreService.create(request);
        AppelOffreResponse response = AppelOffreMapper.INSTANCE.map(appelOffre);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
