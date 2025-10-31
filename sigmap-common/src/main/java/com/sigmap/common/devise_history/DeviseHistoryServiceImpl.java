package com.sigmap.common.devise_history;

import com.sigmap.common.monnaie.Monnaie;
import com.sigmap.common.security.Authentication;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class DeviseHistoryServiceImpl implements DeviseHistoryService {

   private final DeviseHistoryRepository deviseHistoryRepository;


    @Override
    public DeviseHistory save(DeviseHistory deviseHistory) {
        return deviseHistoryRepository.save(deviseHistory);
    }

    @Override
    public Page<DeviseHistory> findAll(Specification<DeviseHistory> spec, Pageable pageable) {
        return deviseHistoryRepository.findAll(spec, pageable);
    }

    @Override
    public Page<DeviseHistory> findAllByCurrencyId(Integer deviseId, Specification<DeviseHistory> spec, Pageable pageable) {
        spec = spec.and(DeviseHistorySpecifications.deviseId(deviseId));

        return deviseHistoryRepository.findAll(spec, pageable);
    }

    private String getUserConnectedId(){
        Optional<String> currentUserId = Authentication.getCurrentUserId();
        return currentUserId.orElse("System");
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

}