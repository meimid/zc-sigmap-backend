package com.sigmap.common.user_commission_historique;

import com.sigmap.common.entity.ActiveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCommissionHistoriqueRepository extends
        JpaRepository<UserCommissionHistorique, Integer>,
        JpaSpecificationExecutor<UserCommissionHistorique> {

    List<UserCommissionHistorique> findByUserId(String userId);

    List<UserCommissionHistorique> findByComId(Integer comId);

    List<UserCommissionHistorique> findByUserIdAndStatus(String userId, ActiveStatus status);

    Optional<UserCommissionHistorique> findByUserIdAndComId(String userId, Integer comId);
}