package com.sigmap.common.user_commission_historique;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCommissionHistoriqueRepository extends JpaRepository<UserCommissionHistorique, Integer>,
        JpaSpecificationExecutor<UserCommissionHistorique> {

}