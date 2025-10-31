package com.sigmap.common.devise_history;

import com.sigmap.common.devise_history.response.DeviseHistoryResponse;
import com.sigmap.common.monnaie.Monnaie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface DeviseHistoryService {

    public DeviseHistory save(DeviseHistory deviseHistory);
    Page<DeviseHistory> findAll(Specification<DeviseHistory> spec, Pageable pageable);
    Page<DeviseHistory> findAllByCurrencyId(Integer deviseId,Specification<DeviseHistory> spec, Pageable pageable);
    public void createHistory (Monnaie request);
    public void updateHistoryFin (Monnaie request , Integer id);
    public void updateHistoryDebut (Monnaie request , Integer id);
    public void deleteHistory (Monnaie request , Integer id);
    public DeviseHistoryResponse entityToResponse (DeviseHistory entity);
}
