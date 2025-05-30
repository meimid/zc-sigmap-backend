package com.sigmap.common.dyna_docs;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface DynaDocsRepository extends JpaRepository<DynaDocs, Integer> {
    List<DynaDocs> findAllByIdIn(Set<DynaDocID> ids);
}

