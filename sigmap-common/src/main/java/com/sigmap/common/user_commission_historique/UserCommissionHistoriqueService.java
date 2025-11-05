package com.sigmap.common.user_commission_historique;

import com.sigmap.common.entity.ActiveStatus;
import com.sigmap.common.user_commission_historique.exception.UserCommissionHistoriqueNotFound;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

public interface UserCommissionHistoriqueService {

    List<UserCommissionHistorique> findAll();


    /** Find all by Specification + pagination */
    Page<UserCommissionHistorique> findAll(Specification<UserCommissionHistorique> specification, Pageable pageable);

    /** Find one by Specification */
    Optional<UserCommissionHistorique> findOne(Specification<UserCommissionHistorique> specification);

    /** Find by ID (any status) */
    UserCommissionHistorique findById(Integer id) throws UserCommissionHistoriqueNotFound;

    /** Save directly */
    UserCommissionHistorique save(UserCommissionHistorique historique);

    /** Create new historique */
    UserCommissionHistorique create(UserCommissionHistorique historique);


    /** Switch status between ACTIF and NON_ACTIF */
 //   UserCommissionHistorique switchStatus(String userId, Integer comId, ActiveStatus status) throws UserCommissionHistoriqueNotFound;

    /**
     * Find one historique record by userId and comId.
     */
    Optional<UserCommissionHistorique> findByUserIdAndComId(String userId, Integer comId);
    /**
     * Get a paginated list of UserCommissionHistorique by comId.
     *
     * @param comId    the commission id to filter by
     * @param pageable pagination information
     * @return a page of UserCommissionHistorique
     */
    Page<UserCommissionHistorique> findAllByComId(Integer comId, Pageable pageable, Specification<UserCommissionHistorique> specification
    );


}
