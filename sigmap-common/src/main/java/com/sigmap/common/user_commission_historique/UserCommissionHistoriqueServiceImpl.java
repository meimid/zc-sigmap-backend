package com.sigmap.common.user_commission_historique;

import com.sigmap.common.entity.ActiveStatus;
import com.sigmap.common.user_commission_historique.exception.ComIdRequired;
import com.sigmap.common.user_commission_historique.exception.UserCommissionHistoriqueNotFound;
import com.sigmap.common.user_societe.exception.UserIdRequiredException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

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

        if (historique.getDateDebut() == null) {
            historique.setDateDebut(LocalDateTime.now());
        }

        return repository.save(historique);
    }

    @Override
    public Optional<UserCommissionHistorique> findByUserIdAndComId(String userId, Integer comId) {
        Specification<UserCommissionHistorique> spec =
                UserCommissionHistoriqueSpecifications.hasUserIdAndComId(userId, comId);
        return findOne(spec);
    }
    @Override
    public Page<UserCommissionHistorique> findAllByComId(Integer comId,String date, Pageable pageable, Specification<UserCommissionHistorique> specification
    ) {
        if (date == null || date.trim().isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        specification = specification.and(UserCommissionHistoriqueSpecifications.hasComId(comId));

        try {
            // Parse the input date string in yyyy-MM-dd format
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date parsedDate = sdf.parse(date);

            // Create a Calendar instance with UTC timezone
            Calendar targetDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            targetDate.setTime(parsedDate);

            // Clear the time part to ensure we're working with the start of the day in UTC
            targetDate.set(Calendar.HOUR_OF_DAY, 0);
            targetDate.set(Calendar.MINUTE, 0);
            targetDate.set(Calendar.SECOND, 0);
            targetDate.set(Calendar.MILLISECOND, 0);

            // Add date range specification to find records for the entire day
            specification = specification.and(UserCommissionHistoriqueSpecifications.dateRange(targetDate));

            return repository.findAll(specification, pageable);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid date format. Please use format: yyyy-MM-dd", e);
        }
    }

}
