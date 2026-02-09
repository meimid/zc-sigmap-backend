package com.sigmap.common.user_commission_historique;

import com.sigmap.common.user_historique_ac.UserAcHistorique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserCommissionHistoriqueRepository extends JpaRepository<UserCommissionHistorique, Integer>,
        JpaSpecificationExecutor<UserCommissionHistorique> {


    @Query("SELECT u FROM UserCommissionHistorique u WHERE u.userId = :userId AND u.com.id = :comId AND u.dateFin IS NULL")
    Optional<UserCommissionHistorique> findByUserIdAndComIdAndDateFinNull(@Param("userId")String userId, @Param("comId") Integer comId);

}