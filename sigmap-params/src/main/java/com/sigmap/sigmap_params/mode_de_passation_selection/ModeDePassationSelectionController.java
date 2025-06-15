package com.sigmap.sigmap_params.mode_de_passation_selection;

import com.sigmap.common.ModeDePassationSelection.ModeDePassationSelection;
import com.sigmap.common.ModeDePassationSelection.ModeDePassationSelectionService;
import com.sigmap.common.ModeDePassationSelection.mappers.ModeDePassationSelectionMapper;
import com.sigmap.common.ModeDePassationSelection.response.ModeDePassationSelectionResponseDto;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/mode_de_passation_selection")
public class ModeDePassationSelectionController {
    private final ModeDePassationSelectionService service;

    public ModeDePassationSelectionController(ModeDePassationSelectionService service) {
        this.service = service;
    }

    /**
     * Return list of mode de passation selection by cateigorie id .
     *
     * @param categorieId the query to use

     * @return a list of of mode de passation selection
     */
    @GetMapping("/categorie/{categorieId}")
    public ResponseEntity<List<ModeDePassationSelectionResponseDto>> findAllByCategorieId(
            @PathVariable Integer categorieId) throws RefParamNotFound {
        List<ModeDePassationSelection> params=service.findByCategorieId(categorieId);
        List<ModeDePassationSelectionResponseDto> response = params.stream()
                .map(ModeDePassationSelectionMapper.INSTANCE::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}
