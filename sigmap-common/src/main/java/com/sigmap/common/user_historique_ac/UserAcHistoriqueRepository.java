package com.sigmap.common.user_historique_ac;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAcHistoriqueRepository extends JpaRepository<UserAcHistorique, Integer>,
                                                    JpaSpecificationExecutor<UserAcHistorique> {


    @Query("SELECT u FROM UserAcHistorique u WHERE u.userId = :userId AND u.ac.id = :acId AND u.dateFin IS NULL")
    Optional<UserAcHistorique> findByUserIdAndAcIdAndDateFinNull(@Param("userId")String userId,@Param("acId") Integer acId);
}
