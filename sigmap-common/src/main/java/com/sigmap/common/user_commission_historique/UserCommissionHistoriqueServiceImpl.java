package com.sigmap.common.user_commission_historique;

import com.sigmap.common.entity.ActiveStatus;
import com.sigmap.common.user_commission_historique.exception.ComIdRequired;
import com.sigmap.common.user_commission_historique.exception.UserCommissionHistoriqueNotFound;
import com.sigmap.common.user_societe.exception.UserIdRequiredException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class UserCommissionHistoriqueServiceImpl implements UserCommissionHistoriqueService {

    private final UserCommissionHistoriqueRepository repository;

    public UserCommissionHistoriqueServiceImpl(UserCommissionHistoriqueRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<UserCommissionHistorique> findAll() {
        return repository.findAll();
    }

    @Override
    public List<UserCommissionHistorique> findAllActif() {
        Specification<UserCommissionHistorique> spec = UserCommissionHistoriqueSpecifications.hasStatus(ActiveStatus.ACTIF);
        return repository.findAll(spec);
    }

    @Override
    public List<UserCommissionHistorique> findAllNonActif() {
        Specification<UserCommissionHistorique> spec = UserCommissionHistoriqueSpecifications.hasStatus(ActiveStatus.NON_ACTIF);
        return repository.findAll(spec);
    }

    @Override
    public Page<UserCommissionHistorique> findAll(Specification<UserCommissionHistorique> specification, Pageable pageable) {
        return repository.findAll(specification, pageable);
    }

    @Override
    public Optional<UserCommissionHistorique> findOne(Specification<UserCommissionHistorique> specification) {
        return repository.findOne(specification);
    }

    @Override
    public UserCommissionHistorique findById(Integer id) throws UserCommissionHistoriqueNotFound {
        Specification<UserCommissionHistorique> spec =UserCommissionHistoriqueSpecifications.id(id);
        return repository.findOne(spec)
                .orElseThrow(() -> new UserCommissionHistoriqueNotFound("Historique not found with id " , id));
    }

    @Override
    public UserCommissionHistorique save(UserCommissionHistorique historique) {
        return repository.save(historique);
    }

    @Override
    public UserCommissionHistorique create(UserCommissionHistorique historique) {
        if (historique.getUserId() == null || historique.getUserId().isBlank()) {
            throw new UserIdRequiredException("UserId is required");
        }

        if (historique.getComId() == null) {
            throw new ComIdRequired("Commission ID is required");
        }

        if (historique.getStatus() == null) {
            historique.setStatus(ActiveStatus.ACTIF);
        }

        if (historique.getDateDebut() == null) {
            historique.setDateDebut(LocalDateTime.now());
        }

        return repository.save(historique);
    }



    @Override
    public UserCommissionHistorique switchStatus(String userId,Integer comId,ActiveStatus status) throws UserCommissionHistoriqueNotFound {
        UserCommissionHistorique historique = findByUserIdAndComId(userId,comId).orElseThrow(()->new UserCommissionHistoriqueNotFound("Historique not found with id " , comId));

        if (status == ActiveStatus.NON_ACTIF) {
            historique.setStatus(ActiveStatus.NON_ACTIF);
            historique.setDateFin(LocalDateTime.now());
        } else {
            historique.setStatus(ActiveStatus.ACTIF);
            historique.setDateDebut(LocalDateTime.now());
            historique.setDateFin(null);
        }

        return save(historique);
    }
    @Override
    public Optional<UserCommissionHistorique> findByUserIdAndComId(String userId, Integer comId) {
        Specification<UserCommissionHistorique> spec =
                UserCommissionHistoriqueSpecifications.hasUserIdAndComId(userId, comId).and(UserCommissionHistoriqueSpecifications.hasStatus(ActiveStatus.ACTIF));
        return findOne(spec);
    }
    @Override
    public Page<UserCommissionHistorique> findAllByComId(Integer comId, Pageable pageable, Specification<UserCommissionHistorique> specification
    ) {
        Specification<UserCommissionHistorique> spec = UserCommissionHistoriqueSpecifications.hasComId(comId).and(specification).and(UserCommissionHistoriqueSpecifications.orderByIdDesc());
        return repository.findAll(spec, pageable);
    }

}
