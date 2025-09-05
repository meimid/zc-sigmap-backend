package com.sigmap.common.dac_docs_details;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

@Service
public class DacDocsDetailsSequenceGenerator {
  private static final String SEQUENCE_NAME = "dac_docs_details_row_id_seq";

  @PersistenceContext private EntityManager entityManager;

  public Integer getNextRowId() {
    Long nextId =
        (Long)
            entityManager
                .createNativeQuery("SELECT NEXTVAL(" + SEQUENCE_NAME + ")")
                .getSingleResult();
    return nextId.intValue();
  }
} 