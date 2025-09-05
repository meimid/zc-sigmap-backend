package com.sigmap.common.ModeDePassationSelection;

import com.sigmap.common.repository.SoftDeleteRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;


@Repository
public interface ModeDePassationSelectionRepository extends JpaRepository<ModeDePassationSelection, Integer>,
        JpaSpecificationExecutor<ModeDePassationSelection>,
        SoftDeleteRepository<ModeDePassationSelection, Integer> {
}
