package com.sigmap.common.utils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

@Service
public class SequenceGeneratorService {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Returns the next value from the given database sequence.
     *
     * @param sequenceName The name of the database sequence
     * @return The next value as Integer
     */
    public Integer getNextId(String sequenceName) {
        Long nextId = (Long) entityManager
                .createNativeQuery("SELECT NEXTVAL(" + sequenceName + ")")
                .getSingleResult();
        return nextId.intValue();
    }
}
