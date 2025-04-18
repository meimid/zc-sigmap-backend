package com.sigmap.common.menu_item;

import com.sigmap.common.authority.Authority;
import com.sigmap.common.authority.AuthorityRepository;
import com.sigmap.common.menu_item.exception.MenuItemAlreadyExistsExeption;
import com.sigmap.common.menu_item.exception.MenuItemNotFound;
import com.sigmap.common.menu_item.mappers.MenuItemMapper;
import com.sigmap.common.menu_item.request.MenuItemRequest;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;



@Slf4j
@Service
@RequiredArgsConstructor
public class MenuItemServiceImpl implements MenuItemService {

    private final MenuItemRepository repository;
    private final MenuItemMapper menuItemMapper;
    private final AuthorityRepository authorityRepository;

    @Override
    public List<MenuItem> findAll() {
        return repository.findAll();
    }

    @Override
    public Page<MenuItem> findAll(Specification<MenuItem> spec,Collection<String> userAuthorities, Pageable pageable) {
        Specification<MenuItem> authSpec = Specification.where(MenuItemSpecifications.authorityIn(userAuthorities))
                .and(MenuItemSpecifications.deleted(false));
        return repository.findAll(authSpec, pageable);
    }

    @Override
    public Page<MenuItem> findAllForAdmin(Specification<MenuItem> menuItem, Pageable pageable) {
        return repository.findAll(menuItem, pageable);
    }

    @Override
    public Page<MenuItem> findAllDeleted(Specification<MenuItem> spec, Pageable pageable) {
        Specification<MenuItem> deletedSpec = MenuItemSpecifications.deleted(true);
        if (spec != null) {
            deletedSpec = deletedSpec.and(spec);
        }
        return repository.findAll(deletedSpec, pageable);
    }

    @Override
    public Page<MenuItem> findAllDeleted(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public Optional<MenuItem> findDeletedById(Integer id) {
        Specification<MenuItem> specification = MenuItemSpecifications.id(id).and(MenuItemSpecifications.deleted(true));
        return findOne(specification);
    }

    @Override
    public Optional<MenuItem> findOne(Specification<MenuItem> specification) {
        return repository.findOne(specification);
    }

    @Override
    public Optional<MenuItem> findById(Integer id) {
        return repository.findById(id);
    }

    @Override
    public MenuItem save(MenuItem menuItem) {
        return repository.save(menuItem);
    }

    @Override
    @Transactional
    public MenuItem create(MenuItemRequest request) throws MenuItemAlreadyExistsExeption {

       MenuItem menuItem =  menuItemMapper.toEntity(request);
       if (request.getParentId() != null) {
           MenuItem parentMenuItem = repository.findById(request.getParentId()).orElseThrow(() -> new IllegalArgumentException("MenuItem with ID " + request.getParentId() + " not found"));
           menuItem.setParentId(parentMenuItem);
       }
        if (request.getAuthority() != null) {
            Authority authority = authorityRepository.findById(request.getAuthority()).orElseThrow(() -> new IllegalArgumentException("Authority with code " + request.getAuthority() + " not found"));
            menuItem.setAuthority(authority);
        }
       menuItem.setIcon(request.getIcon());
       menuItem.setLabel(request.getLabel());
       menuItem.setRoute(request.getRoute());
       menuItem.setOrderIndex(request.getOrderIndex());
       menuItem.setIsDeleted(false);
       repository.save(menuItem);

       return menuItem;
    }

    @Override
    public void delete(Integer id) {
        Optional<MenuItem> menuItem = repository.findById(id);
        if (menuItem.isEmpty()) {
            throw new IllegalArgumentException("MenuItem with ID " + id + " was not found");
        }
        Integer operation = repository.softDelete(id);
        if (operation != 1) {
            throw new IllegalStateException("Soft delete failed: Expected 1 row to be deleted, but " + operation + " rows were affected for MenuItem with ID " + id);
        }
    }

    @Override
    @Transactional
    public MenuItem update(Integer id, MenuItemRequest request) throws MenuItemNotFound {
        MenuItem menuItem = repository.findById(id)
                .orElseThrow(() -> new MenuItemNotFound(id,"MenuItem with ID " + id + " was not found in the database."));
        if (menuItem.getIsDeleted()) {
            throw new IllegalArgumentException("Cannot update MenuItem with ID " + id + " because it is marked as deleted in the data base.");
        }
        menuItemMapper.updateMenuItemFromRequest(request, menuItem);
        if (request.getParentId()!=null){
            MenuItem menuItemParent = repository.findById(request.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("MenuItem with ID " + request.getParentId() + " not found"));
            menuItem.setParentId(menuItemParent);
        }
        if (request.getAuthority()!=null){
            Authority authority = authorityRepository.findById(request.getAuthority())
                    .orElseThrow(() -> new IllegalArgumentException("Authority with Code " + request.getAuthority() + " not found"));
            menuItem.setAuthority(authority);
        }
        return repository.save(menuItem);
    }
}
