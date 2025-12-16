package com.sigmap.common.appel_offre;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppelOffreRepository extends JpaRepository<AppelOffre, Integer>, JpaSpecificationExecutor<AppelOffre> {

    @Query("SELECT a from AppelOffre a where a.isDeleted = false")
    List<AppelOffre> getAllNotDeleted();
}
