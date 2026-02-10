package com.sigmap.common.user_historique_ac;

import com.sigmap.common.ac.Ac;
import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.user_historique_ac.response.UserAcHistoriqueResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface UserAcHistoriqueService {

    public UserAcHistorique saveUserAcHistorique(UserAcHistorique userAcHistorique);

    public Page<UserAcHistorique> getAllUserAcHistorique(Specification<UserAcHistorique> spec, Pageable pageable);

    Page<UserAcHistorique> findAllByAcIdAndDate(Integer acId, String date, Specification<UserAcHistorique> spec, Pageable pageable);

    public void createEndHistory(String userIds, Integer acId);

    public void createStartHistory(String userId, Integer acId) throws AcNotFound;

    public UserAcHistoriqueResponse entityToResponse (UserAcHistorique entity);
}
