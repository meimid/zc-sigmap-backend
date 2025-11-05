package com.sigmap.common.user_historique_ac.mapper;

import com.sigmap.common.user_historique_ac.UserAcHistorique;
import com.sigmap.common.user_historique_ac.response.UserAcHistoriqueResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserAcHistoriqueMapper {
    UserAcHistoriqueMapper INSTANCE = Mappers.getMapper(UserAcHistoriqueMapper.class);
    
    UserAcHistoriqueResponse toResponse(UserAcHistorique entity);
}
