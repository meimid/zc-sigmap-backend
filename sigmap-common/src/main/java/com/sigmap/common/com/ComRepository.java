package com.sigmap.common.com;
import com.sigmap.common.repository.SoftDeleteRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ComRepository extends JpaRepository<Com, Integer>,
        JpaSpecificationExecutor<Com>,
        SoftDeleteRepository<Com, Integer> {}
