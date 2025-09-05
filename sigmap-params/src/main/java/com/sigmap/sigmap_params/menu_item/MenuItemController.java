package com.sigmap.sigmap_params.menu_item;

import com.sigmap.common.authority.exeption.AuthorityNotFound;
import com.sigmap.common.menu_item.MenuItem;
import com.sigmap.common.menu_item.MenuItemService;
import com.sigmap.common.menu_item.exception.MenuItemAlreadyExistsExeption;
import com.sigmap.common.menu_item.exception.MenuItemNotFound;
import com.sigmap.common.menu_item.exception.MenuItemSoftDeleteExeption;
import com.sigmap.common.menu_item.exception.MenuItemUpdateExeption;
import com.sigmap.common.menu_item.mappers.MenuItemMapper;
import com.sigmap.common.menu_item.request.MenuItemQuery;
import com.sigmap.common.menu_item.request.MenuItemRequest;
import com.sigmap.common.menu_item.response.MenuItemResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/menu")
public class MenuItemController {

    private final MenuItemService menuItemService;

    /**
     *
     * @param query
     * @param pageable
     * @return MenuItemResponse : le menu hiérarchique filtré selon les rôles de l’utilisateur connecté
     */
    @GetMapping
    public ResponseEntity<Page<MenuItemResponse>> findAll(MenuItemQuery query, Pageable pageable) {
        log.debug("API for get All MenuItem .... ");

        // Get roles from Security Context
        Collection<? extends GrantedAuthority> authorities =
                SecurityContextHolder.getContext().getAuthentication().getAuthorities();

        // Extract role names, e.g. ROLE_ADMIN → ADMIN
        List<String> roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.replace("ROLE_", "")) // your logic may vary
                .toList();

        Page<MenuItem> menuItems = menuItemService.findAll(query.toSpecification(),roles, pageable);
        Page<MenuItemResponse> menuItemResponse = menuItems.map(MenuItemMapper.INSTANCE::map);
        return ResponseEntity.ok(menuItemResponse);
    }

    /**
     *
     * @param query
     * @param pageable
     * @return MenuItemResponse : Retourne tous les menus sans filtrage par rôle pour l'admin
     */
    @GetMapping("/all")
    public ResponseEntity<Page<MenuItemResponse>> findAllForAdmin(MenuItemQuery query, Pageable pageable) {
        Page<MenuItem> menuItems = menuItemService.findAllForAdmin(query.toSpecification(), pageable);
        Page<MenuItemResponse> menuItemResponse = menuItems.map(MenuItemMapper.INSTANCE::map);
        return ResponseEntity.ok(menuItemResponse);
    }


    /**
     * Créer un nouveau menu item
     * @param request
     * @return MenuItemResponse
     * @throws MenuItemAlreadyExistsExeption
     */
    @PostMapping
    public ResponseEntity<MenuItemResponse> create(@RequestBody MenuItemRequest request)throws MenuItemAlreadyExistsExeption, MenuItemNotFound, AuthorityNotFound {
        MenuItem menuItem = menuItemService.create(request);
        MenuItemResponse response = MenuItemMapper.INSTANCE.map(menuItem);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    /**
     * Met à jour un menu existant
     * @param id
     * @param request
     * @return MenuItemResponse
     * @throws MenuItemNotFound
     */
    @PutMapping("/{id}")
    public ResponseEntity<MenuItemResponse> update(@PathVariable Integer id ,@RequestBody MenuItemRequest request) throws MenuItemNotFound, AuthorityNotFound, MenuItemUpdateExeption {
        MenuItem menuItem = menuItemService.update(id,request);
        MenuItemResponse response = MenuItemMapper.INSTANCE.map(menuItem);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    /**
     * Supprime un menu item
     * @param id
     * @return MenuItemResponse
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<MenuItemResponse> deleteById(@PathVariable Integer id) throws MenuItemNotFound, MenuItemSoftDeleteExeption {
        menuItemService.delete(id);
        MenuItem menuItemDeleted = menuItemService.findDeletedById(id)
                .orElseThrow();
        MenuItemResponse response = MenuItemMapper.INSTANCE.map(menuItemDeleted);
        return ResponseEntity.ok(response);
    }
















    @GetMapping("/deleted")
    public ResponseEntity<Page<MenuItemResponse>> findAllDeleted(
            MenuItemQuery query, Pageable pageable) {
        Page<MenuItem> params = menuItemService.findAllDeleted(query.toSpecification(), pageable);
        Page<MenuItemResponse> menuItemResponses = params.map(MenuItemMapper.INSTANCE::map);
        return ResponseEntity.ok(menuItemResponses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MenuItemResponse> findById(@PathVariable Integer id) throws MenuItemNotFound {
        MenuItem menuItem = menuItemService.findById(id);
        MenuItemResponse response = MenuItemMapper.INSTANCE.map(menuItem);
        return ResponseEntity.ok(response);
    }






}
