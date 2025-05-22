package com.sigmap.sigmap_paa.categorie;

import com.sigmap.common.categorie.Categorie;
import com.sigmap.common.categorie.CategorieService;
import com.sigmap.common.categorie.CategorieServiceImpl;
import com.sigmap.common.categorie.exeption.CategorieNotFound;
import com.sigmap.common.categorie.exeption.CategorieUpdateExeption;
import com.sigmap.common.categorie.mappers.CategorieMapper;
import com.sigmap.common.categorie.request.CategorieRequest;
import com.sigmap.common.categorie.response.CategorieResponse;
import com.sigmap.common.menu_item.MenuItem;
import com.sigmap.common.menu_item.exception.MenuItemNotFound;
import com.sigmap.common.menu_item.exception.MenuItemSoftDeleteExeption;
import com.sigmap.common.menu_item.mappers.MenuItemMapper;
import com.sigmap.common.menu_item.response.MenuItemResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/categorie")
public class CategorieController {
    private final CategorieServiceImpl categorieService;

    public CategorieController(CategorieServiceImpl categorieService) {
        this.categorieService = categorieService;
    }

    @GetMapping
    public ResponseEntity<List<CategorieResponse>> findAll() {
        //log.debug("API for get All Categories .... ");

        List<Categorie> categories = categorieService.findAll();
        List<CategorieResponse> categorieResponses = categories.stream().map(CategorieMapper.INSTANCE::map).toList();
        return ResponseEntity.ok(categorieResponses);
    }

    @PostMapping
    public ResponseEntity<CategorieResponse> create(@RequestBody CategorieRequest request) {
        Categorie categorie = categorieService.create(request);
        CategorieResponse response = CategorieMapper.INSTANCE.map(categorie);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategorieResponse> update(@PathVariable Integer id ,@RequestBody CategorieRequest request) throws CategorieNotFound, CategorieUpdateExeption {
        Categorie categorie = categorieService.update(id,request);
        CategorieResponse response = CategorieMapper.INSTANCE.map(categorie);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CategorieResponse> deleteById(@PathVariable Integer id) {
        categorieService.delete(id);
        Categorie categorie = categorieService.findDeletedById(id)
                .orElseThrow();
        CategorieResponse response = CategorieMapper.INSTANCE.map(categorie);
        return ResponseEntity.ok(response);
    }


}
