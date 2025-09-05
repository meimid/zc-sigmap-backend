package com.sigmap.common.adresse;

import com.sigmap.common.repository.SoftDeleteRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AdresseRespository extends JpaRepository<Adresse, Integer>,
        JpaSpecificationExecutor<Adresse>,
        SoftDeleteRepository<Adresse, Integer> {
}
