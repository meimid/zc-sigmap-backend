package com.sigmap.common.categorie;

import com.sigmap.common.categorie.exeption.CategorieNotFound;
import com.sigmap.common.categorie.exeption.CategorieUpdateExeption;
import com.sigmap.common.categorie.mappers.CategorieMapper;
import com.sigmap.common.categorie.request.CategorieRequest;
import com.sigmap.common.menu_item.MenuItem;
import com.sigmap.common.menu_item.MenuItemSpecifications;
import com.sigmap.common.menu_item.exception.MenuItemNotFound;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategorieServiceImpl implements CategorieService{

    private final CategorieRepository categorieRepository;
    private final CategorieMapper categorieMapper;

    @Override
    public List<Categorie> findAll() {
        return categorieRepository.findAll();
    }

    @Override
    public Optional<Categorie> findOne(Specification<Categorie> specification) {
        return categorieRepository.findOne(specification);
    }

    @Override
    public Optional<Categorie> findDeletedById(Integer id) {
        Specification<Categorie> specification = CategorieSpecifications.id(id).and(CategorieSpecifications.deleted(true));
        return findOne(specification);
    }

    @Override
    public Categorie findById(Integer id) throws CategorieNotFound {
        return categorieRepository.findById(id).orElseThrow(() -> new CategorieNotFound("", List.of(id)));
    }

    @Override
    public Categorie save(Categorie categorie) {
        return categorieRepository.save(categorie);
    }

    @Override
    @Transactional
    public Categorie create(CategorieRequest request) {
        Categorie categorie = categorieMapper.toEntity(request);
        return categorieRepository.save(categorie);
    }

    @Override
    @Transactional
    public Categorie update(Integer id, CategorieRequest request) throws CategorieNotFound, CategorieUpdateExeption {
        Categorie categorie = categorieRepository.findById(id)
                .orElseThrow(() -> new CategorieNotFound("", List.of(id)));
        if (categorie.getIsDeleted()) {
            throw new CategorieUpdateExeption("", id);
        }
        categorieMapper.updateCategorieFromRequest(request, categorie);
        return categorieRepository.save(categorie);
    }

    @Override
    public void delete(Integer id) {
        Optional<Categorie> categorie = categorieRepository.findById(id);
        categorie.ifPresent(categorieRepository::delete);

    }
}
