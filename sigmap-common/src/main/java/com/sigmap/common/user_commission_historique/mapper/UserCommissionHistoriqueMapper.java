package com.sigmap.common.user_commission_historique.mapper;

import com.sigmap.common.user_commission_historique.UserCommissionHistorique;
import com.sigmap.common.user_commission_historique.response.UserCommissionHistoriqueResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")

public interface UserCommissionHistoriqueMapper {
    UserCommissionHistoriqueResponse mapToResponse(UserCommissionHistorique userCommissionHistorique);
}
