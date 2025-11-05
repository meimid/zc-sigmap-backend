package com.sigmap.common.devise_history;

import com.sigmap.common.devise_history.response.DeviseHistoryResponse;
import com.sigmap.common.keycloakAdmin.dto.UserDTO;
import com.sigmap.common.keycloakAdmin.service.KeycloakAdminService;
import com.sigmap.common.monnaie.Monnaie;
import com.sigmap.common.security.Authentication;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Slf4j
@AllArgsConstructor
public class DeviseHistoryServiceImpl implements DeviseHistoryService {

   private final DeviseHistoryRepository deviseHistoryRepository;
   private final KeycloakAdminService keycloakAdminService;


    @Override
    public DeviseHistory save(DeviseHistory deviseHistory) {
        return deviseHistoryRepository.save(deviseHistory);
    }

    @Override
    public Page<DeviseHistory> findAll(Specification<DeviseHistory> spec, Pageable pageable) {
        return deviseHistoryRepository.findAll(spec, pageable);
    }

    @Override
    public Page<DeviseHistory> findAllByCurrencyId(String date, Specification<DeviseHistory> spec, Pageable pageable) {
        if (date == null || date.trim().isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }
        
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
            spec = spec.and(DeviseHistorySpecifications.dateBetween(targetDate));

            return deviseHistoryRepository.findAll(spec, pageable);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid date format. Please use format: yyyy-MM-dd", e);
        }
    }

    private String getUserConnectedId(){
        Optional<String> currentUserId = Authentication.getCurrentUserId();
        return currentUserId.orElse("System");
    }

    private String getUserConnectedName(String userId){
        try {
            UserDTO keycloakUser = keycloakAdminService.getUserById(userId);

            if (keycloakUser == null) {
                return "user not found";
            }

            return keycloakUser.getFirstName() + " " + keycloakUser.getLastName();

        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("404")) {
                log.warn("User with ID {} not found in Keycloak.", userId);
                return "user not found";
            }
            log.error("Error fetching user {} from Keycloak: {}", userId, e.getMessage());
            return "user not found";
        }
    }

    private Integer getMaxId (Integer deviseId){
        return deviseHistoryRepository.findMaxId(deviseId);
    }


    private DeviseHistory getLastOne ( Integer deviseId){
        return deviseHistoryRepository.findLastOne(getMaxId(deviseId),deviseId);
    }

    @Override
    public void createHistory (Monnaie request){
        DeviseHistory history = new DeviseHistory();
        history.setDeviseId(request.getId());
        history.setCode(request.getCode());
        history.setDescription(request.getDescription());
        history.setTaux(request.getTaux());
        history.setDateDebut(Calendar.getInstance());
        history.setChangedBy(getUserConnectedId());
        history.setAction("CREATE");
        save(history);
    }

    @Override
    public void updateHistoryFin (Monnaie request , Integer id){
        // get getLastOne deviseHistory
        DeviseHistory history = getLastOne(id);
        if (history == null){

            DeviseHistory newHistory = new DeviseHistory();
            newHistory.setDeviseId(id);
            newHistory.setCode(request.getCode());
            newHistory.setDescription(request.getDescription());
            newHistory.setTaux(request.getTaux());
            newHistory.setDateFin(Calendar.getInstance());
            newHistory.setChangedBy(getUserConnectedId());
            newHistory.setAction("UPDATE");
            save(newHistory);

        }else {
            history.setDateFin(Calendar.getInstance());
            history.setChangedBy(getUserConnectedId());
            save(history);
        }

    }

    @Override
    public void updateHistoryDebut (Monnaie request , Integer id){
        DeviseHistory history = new DeviseHistory();
        history.setDeviseId(id);
        history.setCode(request.getCode());
        history.setDescription(request.getDescription());
        history.setTaux(request.getTaux());
        history.setDateDebut(Calendar.getInstance());
        history.setChangedBy(getUserConnectedId());
        history.setAction("UPDATE");
        save(history);
    }


    @Override
    public void deleteHistory (Monnaie request , Integer id){
        DeviseHistory history = new DeviseHistory();
        history.setDeviseId(id);
        history.setCode(request.getCode());
        history.setDescription(request.getDescription());
        history.setTaux(request.getTaux());
        history.setDateDebut(Calendar.getInstance());
        history.setDateFin(Calendar.getInstance());
        history.setChangedBy(getUserConnectedId());
        history.setAction("DELETE");
        save(history);
    }

    @Override
    public DeviseHistoryResponse entityToResponse (DeviseHistory entity){

        if (entity == null){
            return null;
        }

        DeviseHistoryResponse response = new DeviseHistoryResponse();

        response.setId(entity.getId());
        response.setDeviseId(entity.getDeviseId());
        response.setCode(entity.getCode());
        response.setDescription(entity.getDescription());
        response.setTaux(entity.getTaux());
        response.setDateDebut(entity.getDateDebut());
        response.setDateFin(entity.getDateFin());
        response.setAction(entity.getAction());
        response.setChangedBy(getUserConnectedName(entity.getChangedBy()));

        return response;

    }
}