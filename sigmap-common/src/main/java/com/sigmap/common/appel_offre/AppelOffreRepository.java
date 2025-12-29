package com.sigmap.common.appel_offre;

import com.sigmap.common.appel_offre.AppelOffre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppelOffreRepository extends JpaRepository<AppelOffre, Integer>, JpaSpecificationExecutor<AppelOffre> {

    @Query("SELECT DISTINCT a FROM AppelOffre a " +
           "LEFT JOIN FETCH a.realisation r " +
           "LEFT JOIN FETCH r.monnaie m " +
           "LEFT JOIN FETCH r.modePassation mp " +
           "LEFT JOIN FETCH a.ppm p " +
           "WHERE a.id = :id")
    Optional<AppelOffre> findByIdWithRelations(@Param("id") Integer id);

    @Query("SELECT a from AppelOffre a where a.isDeleted = false")
    List<AppelOffre> getAllNotDeleted();
}
