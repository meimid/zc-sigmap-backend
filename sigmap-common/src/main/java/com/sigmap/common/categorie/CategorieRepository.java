package com.sigmap.common.categorie;

import com.sigmap.common.menu_item.MenuItem;
import com.sigmap.common.repository.SoftDeleteRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CategorieRepository extends JpaRepository<Categorie, Integer>,
        JpaSpecificationExecutor<Categorie>, SoftDeleteRepository<Categorie, Integer> {
}
