package com.sigmap.common.dac;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface DacShortlistRepository extends JpaRepository<DacShortlist, Integer> {

    @Query("SELECT s FROM DacShortlist s WHERE s.isDeleted = false")
    List<DacShortlist> findByDacIdAndDacVer(Integer dacId, Integer dacVer);

    @Modifying
    @Transactional
    @Query("UPDATE DacShortlist s SET s.isDeleted = true, s.deletedAt = CURRENT_TIMESTAMP, s.deletedBy = ?#{principal.subject} WHERE s IN ?1")
    void softDelete(List<DacShortlist> list);
}
