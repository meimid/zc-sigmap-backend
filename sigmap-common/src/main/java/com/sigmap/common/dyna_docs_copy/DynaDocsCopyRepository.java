package com.sigmap.common.dyna_docs_copy;

import com.sigmap.common.modified_dyna_docs_for_dac.ModifiedDynaDocs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DynaDocsCopyRepository extends JpaRepository<DynaDocsCopy, Integer> {

  @Query("""
          SELECT ddc
          FROM DynaDocsCopy ddc
          WHERE ddc.status = 'ACTIF'
            AND ddc.version = (
                  SELECT MAX(ddc2.version)
                  FROM DynaDocsCopy ddc2
                  WHERE ddc2.dac.dacId = ddc.dac.dacId
                    AND ddc2.dac.dacVer = ddc.dac.dacVer
                    AND ddc2.dynaDocs.id = ddc.dynaDocs.id
                    AND ddc2.status = 'ACTIF'
            )
            AND ddc.dac.dacId = :dacId
            AND ddc.dac.dacVer = :dacVer
            AND ddc.dynaDocs.typeMarche.id = :typeMarche
            AND ddc.dynaDocs.modePassation.id = :modePassation
            AND ddc.dynaDocs.dynaDocsModel.dynaDocsModelId = :modelId
      """)
  List<DynaDocsCopy> findActiveLastVersionByCriteria(
      @Param("dacId") Integer dacId,
      @Param("dacVer") Integer dacVer,
      @Param("typeMarche") Integer typeMarche,
      @Param("modePassation") Integer modePassation,
      @Param("modelId") Integer modelId);

  @Query("""
          SELECT ddc
          FROM DynaDocsCopy ddc
          WHERE ddc.status = 'ACTIF'
            AND ddc.version = (
                  SELECT MAX(ddc2.version)
                  FROM DynaDocsCopy ddc2
                  WHERE ddc2.dac.dacId = ddc.dac.dacId
                    AND ddc2.dac.dacVer = ddc.dac.dacVer
                    AND ddc2.dynaDocs.id = ddc.dynaDocs.id
                    AND ddc2.dynaDocs.version = ddc.dynaDocs.version
                    AND ddc2.status = 'ACTIF'
            )
            AND ddc.dac.dacId = :dacId
            AND ddc.dac.dacVer = :dacVer
                  ORDER BY ddc.ordre ASC
      """)
  List<DynaDocsCopy> findActiveLastVersionByDac(
      @Param("dacId") Integer dacId,
      @Param("dacVer") Integer dacVer);

  @Query("SELECT ddc FROM DynaDocsCopy ddc WHERE ddc.dynaDocs.id = :dynaDocsId AND ddc.dynaDocs.version = :dynaDocsVer And ddc.status = 'ACTIF' AND ddc.isDeleted = false and ddc.dac.dacId = :dacId and ddc.dac.dacVer = :dacVer" +
          " AND ddc.version = (SELECT MAX(ddc2.version) FROM DynaDocsCopy ddc2 WHERE ddc2.dac.dacId = ddc.dac.dacId AND ddc2.dac.dacVer = ddc.dac.dacVer AND ddc2.dynaDocs.id = ddc.dynaDocs.id AND ddc2.dynaDocs.version = ddc.dynaDocs.version AND ddc2.status = 'ACTIF')")
  Optional<DynaDocsCopy> findByIdDynaDocsIdAndDynaDocsVer (@Param("dynaDocsId") Integer dynaDocsId, @Param("dynaDocsVer") Integer dynaDocsVer, @Param("dacId") Integer dacId, @Param("dacVer") Integer dacVer);

  /*
   * @Query("SELECT ddc FROM DynaDocsCopy ddc WHERE ddc.id = :id AND ddc.dac.dacId = :dacId AND ddc.dac.dacVer = :dacVer And ddc.dynaDocs.id = :dynaDocsId And ddc.dynaDocs.version = :dynaDocsVer And ddc.status = 'ACTIF' AND ddc.isDeleted = false"
   * )
   * Optional<DynaDocsCopy> findOneById (@Param("dacId") Integer
   * dacId, @Param("dacVer") Integer dacVer, @Param("dynaDocsId") Integer
   * dynaDocsId, @Param("dynaDocsVer") Integer dynaDocsVer);
   */

  @Query("SELECT ddc FROM DynaDocsCopy ddc WHERE ddc.id = :id AND ddc.isDeleted = false")
  Optional<DynaDocsCopy> findOneById(@Param("id") Integer id);
}
