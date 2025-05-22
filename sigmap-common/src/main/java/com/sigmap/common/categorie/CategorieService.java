package com.sigmap.common.categorie;

import com.sigmap.common.categorie.exeption.CategorieNotFound;
import com.sigmap.common.categorie.exeption.CategorieUpdateExeption;
import com.sigmap.common.categorie.request.CategorieRequest;
import org.springframework.data.jpa.domain.Specification;


import java.util.List;
import java.util.Optional;

public interface CategorieService {

    List<Categorie> findAll();
    Categorie findById(Integer id) throws CategorieNotFound;
    Optional<Categorie> findOne(Specification<Categorie> specification);
    Optional<Categorie> findDeletedById(Integer id);

    Categorie save(Categorie categorie);

    Categorie create(CategorieRequest request) ;

    Categorie update(Integer id, CategorieRequest request) throws CategorieNotFound, CategorieUpdateExeption;

    void delete(Integer id);
}
