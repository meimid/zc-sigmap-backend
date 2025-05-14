package com.sigmap.common.dyna_docs;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DynaDocsRepository extends JpaRepository<DynaDocs, Integer> {}

