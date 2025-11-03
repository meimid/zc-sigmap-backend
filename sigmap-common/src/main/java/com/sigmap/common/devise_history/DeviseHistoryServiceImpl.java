package com.sigmap.common.devise_history;

import com.sigmap.common.devise_history.response.DeviseHistoryResponse;
import com.sigmap.common.keycloakAdmin.dto.UserDTO;
import com.sigmap.common.keycloakAdmin.service.KeycloakAdminService;
import com.sigmap.common.monnaie.Monnaie;
import com.sigmap.common.security.Authentication;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;

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
        try {
            // Parse the input date string in ISO 8601 format (e.g., 2025-11-26T15:14:10.000Z)
            // The 'X' is for ISO 8601 time zone
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date parsedDate = sdf.parse(date);
            Calendar targetDate = Calendar.getInstance();
            targetDate.setTime(parsedDate);

            // Add date range specification to find records where targetDate is between dateDebut and dateFin
            spec = spec.and((root, query, criteriaBuilder) -> {
                // The target date should be >= dateDebut AND (<= dateFin OR dateFin is null)
                return criteriaBuilder.and(
                    criteriaBuilder.lessThanOrEqualTo(root.get("dateDebut"), targetDate),
                    criteriaBuilder.or(
                        criteriaBuilder.greaterThanOrEqualTo(root.get("dateFin"), targetDate),
                        criteriaBuilder.isNull(root.get("dateFin"))
                    )
                );
            });

            Page<DeviseHistory> result = deviseHistoryRepository.findAll(spec, pageable);
            // Return the result (will be empty if no records found)
            return result;
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid date format. Please use format exmp: 2025-11-26T15:14:10.000Z ", e);
        }
    }

    private String getUserConnectedId(){
        Optional<String> currentUserId = Authentication.getCurrentUserId();
        return currentUserId.orElse("System");
    }

    private String getUserConnectedName(String userId){
        UserDTO keycloakUser = keycloakAdminService.getUserById(userId);
        return keycloakUser.getFirstName()+ " "+keycloakUser.getLastName();
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