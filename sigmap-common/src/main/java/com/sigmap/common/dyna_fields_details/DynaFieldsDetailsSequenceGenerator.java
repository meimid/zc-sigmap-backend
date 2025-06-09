package com.sigmap.common.dyna_fields_details;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

@Service
public class DynaFieldsDetailsSequenceGenerator {
  private static final String SEQUENCE_NAME = "dyna_fields_details_id_seq";

  @PersistenceContext private EntityManager entityManager;

  public Integer getNextId() {
    Long nextId =
        (Long)
            entityManager
                .createNativeQuery("SELECT NEXTVAL(" + SEQUENCE_NAME + ")")
                .getSingleResult();
    return nextId.intValue();
  }
}
