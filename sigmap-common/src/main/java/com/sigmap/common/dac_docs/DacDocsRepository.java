package com.sigmap.common.dac_docs;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DacDocsRepository extends JpaRepository<DacDocs, DacDocsId>, JpaSpecificationExecutor<DacDocs> {
  @Query("SELECT d FROM DacDocs d WHERE d.id.dacId = :dacId AND d.id.dacVer = :dacVer")
  List<DacDocs> findByDacIdAndDacVer(@Param("dacId") Integer dacId, @Param("dacVer") Integer dacVer);



    @Query(" SELECT d FROM DacDocs d WHERE d.id.dacId = :dacId AND d.id.dacVer = :dacVer " +
            " AND d.id.dynaDocsId = :dynaDocsId AND d.id.dynaDocsVer = :dynaDocsVer " +
            " AND d.id.dynaFieldsId = :dynaFieldsId ")
    Optional<DacDocs> findDacDocsBYPKs (@Param("dacId") Integer dacId,
                                            @Param("dacVer") Integer dacVer,
                                            @Param("dynaDocsId") Integer dynaDocsId,
                                            @Param("dynaDocsVer") Integer dynaDocsVer,
                                            @Param("dynaFieldsId") Integer dynaFieldsId);

    @Query(" SELECT d FROM DacDocs d WHERE d.id.dacId = :dacId AND d.id.dacVer = :dacVer ")
    List<DacDocs> findDacByID (@Param("dacId") Integer dacId,
                         @Param("dacVer") Integer dacVer);

  @Query(value = "SELECT ddd.row_id, dfd.column_name, ddd.field_value FROM dac_docs_details ddd JOIN dyna_fields_details dfd ON ddd.dyna_fields_details_id = dfd.dyna_fields_details_id WHERE ddd.dac_id = :dacId AND ddd.dac_ver = :dacVer AND ddd.dyna_docs_id = :dynaDocsId AND ddd.dyna_docs_ver = :dynaDocsVer AND ddd.dyna_fields_id = :dynaFieldsId ORDER BY ddd.row_id, dfd.column_pos", nativeQuery = true)
  List<Object[]> findTableRowsRaw(@Param("dacId") Integer dacId,
                                  @Param("dacVer") Integer dacVer,
                                  @Param("dynaDocsId") Integer dynaDocsId,
                                  @Param("dynaDocsVer") Integer dynaDocsVer,
                                  @Param("dynaFieldsId") Integer dynaFieldsId);

  // Default method to convert raw rows to List<Map<String, String>>
  default List<java.util.Map<String, String>> findTableRowsForField(Integer dacId, Integer dacVer, Integer dynaDocsId, Integer dynaDocsVer, Integer dynaFieldsId) {
      List<Object[]> rawRows = findTableRowsRaw(dacId, dacVer, dynaDocsId, dynaDocsVer, dynaFieldsId);
      java.util.Map<Integer, java.util.Map<String, String>> rowMap = new java.util.LinkedHashMap<>();
      for (Object[] row : rawRows) {
          Integer rowId = ((Number) row[0]).intValue();
          String columnName = (String) row[1];
          String fieldValue = (String) row[2];
          rowMap.computeIfAbsent(rowId, k -> new java.util.LinkedHashMap<>()).put(columnName, fieldValue);
      }
      return new java.util.ArrayList<>(rowMap.values());
  }
}
