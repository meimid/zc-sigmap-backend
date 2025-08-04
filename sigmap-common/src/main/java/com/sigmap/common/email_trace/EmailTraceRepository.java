package com.sigmap.common.email_trace;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailTraceRepository extends JpaRepository<EmailTrace, Integer>, JpaSpecificationExecutor<EmailTrace> {
}
