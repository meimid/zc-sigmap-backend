package com.sigmap.common.user_historique_ac;


import com.sigmap.common.ac.Ac;
import com.sigmap.common.ac.mapper.AcMapper;
import com.sigmap.common.entity.ActiveStatus;
import com.sigmap.common.keycloakAdmin.dto.UserDTO;
import com.sigmap.common.keycloakAdmin.service.KeycloakAdminService;
import com.sigmap.common.user_historique_ac.response.UserAcHistoriqueResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.text.SimpleDateFormat;
import java.text.ParseException;

@Service
@AllArgsConstructor
@Slf4j
public class UserAcHistoriqueServiceImpl implements UserAcHistoriqueService{

    private final UserAcHistoriqueRepository userAcHistoriqueRepository;
    private final KeycloakAdminService keycloakAdminService;


    @Override
    public UserAcHistorique saveUserAcHistorique(UserAcHistorique userAcHistorique) {
        return userAcHistoriqueRepository.save(userAcHistorique);
    }

    @Override
    public Page<UserAcHistorique> getAllUserAcHistorique(Specification<UserAcHistorique> spec, Pageable pageable) {
        return userAcHistoriqueRepository.findAll(spec, pageable);
    }

    private String getUserConnectedName(String userId){
        UserDTO keycloakUser = keycloakAdminService.getUserById(userId);
        return keycloakUser.getFirstName()+ " "+keycloakUser.getLastName();
    }

    @Override
    public void createStartHistory(String userId, Ac ac) {

        if (userId == null || ac == null || ac.getId() == null) {
            throw new IllegalArgumentException("User ID and AC ID must not be null");
        }

        UserAcHistorique newEntry = new UserAcHistorique();
        newEntry.setUserId(userId);
        newEntry.setDateDebut(LocalDateTime.now());
        newEntry.setStatus(ActiveStatus.ACTIF);
        newEntry.setAc(ac);
        userAcHistoriqueRepository.save(newEntry);

    }

    @Override
    public void createEndHistory(String userId, Integer acId){


            // 1. Chercher une entrée active (ACTIF + dateFin null)
            Optional<UserAcHistorique> userAcHistorique = userAcHistoriqueRepository.findByUserIdAndAcIdAndDateFinNull(userId, acId);

            // 2. Si existe → la fermer
            userAcHistorique.ifPresent(oldEntry -> {
                oldEntry.setDateFin(LocalDateTime.now());
                oldEntry.setStatus(ActiveStatus.NON_ACTIF);
                userAcHistoriqueRepository.save(oldEntry);
            });

    }
    
    @Override
    public Page<UserAcHistorique> findAllByAcIdAndDate(Integer acId, String date, Specification<UserAcHistorique> spec, Pageable pageable) {
        if (date == null || date.trim().isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        spec = spec.and(UserAcHistoriqueSpecifocations.acId(acId));

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
            spec = spec.and(UserAcHistoriqueSpecifocations.dateRange(targetDate));

            return userAcHistoriqueRepository.findAll(spec, pageable);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid date format. Please use format: yyyy-MM-dd", e);
        }
    }

    @Override
    public UserAcHistoriqueResponse entityToResponse (UserAcHistorique entity){

        if (entity == null){
            return null;
        }

        UserAcHistoriqueResponse response = new UserAcHistoriqueResponse();

        response.setId(entity.getId());
        response.setUserId(entity.getUserId());
        
        // Map Ac to AcResponseDto using AcMapper
        if (entity.getAc() != null) {
            AcMapper acMapper = AcMapper.INSTANCE;
            response.setAc(acMapper.mapToDto(entity.getAc()));
        }
        
        response.setDateDebut(entity.getDateDebut());
        response.setDateFin(entity.getDateFin());
        response.setStatus(entity.getStatus());
        response.setUserName(getUserConnectedName(entity.getUserId()));

        return response;
    }
}
