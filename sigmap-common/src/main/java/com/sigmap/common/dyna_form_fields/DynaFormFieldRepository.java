package com.sigmap.common.dyna_form_fields;

import com.sigmap.common.dyna_form_fields.DynaFormField;
import com.sigmap.common.dyna_form_fields.DynaFormFieldId;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DynaFormFieldRepository
        extends JpaRepository<DynaFormField, DynaFormFieldId>, JpaSpecificationExecutor<DynaFormField> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           update DynaFormField t
              set t.isDeleted = true,
                  t.deletedAt = CURRENT_TIMESTAMP,
                  t.deletedBy = :deletedBy
            where t.dynaFormId = :formId
              and t.dynaFormVer = :formVer
              and t.dynaFormFieldsId = :fieldId
           """)
    int softDelete(@Param("formId") Integer formId,
                   @Param("formVer") Integer formVer,
                   @Param("fieldId") Integer fieldId,
                   @Param("deletedBy") String deletedBy);

    @Query("""
           select f
             from DynaFormField f
            where f.dynaFormId  = :formId
              and f.dynaFormVer = :formVer
              and f.isDeleted   = false
         order by coalesce(f.order, 2147483647), f.dynaFormFieldsId
           """)
    List<DynaFormField> findActiveByFormAndVersion(@Param("formId") Integer formId,
                                                   @Param("formVer") Integer formVer);

    @Query("""
       select f
         from DynaFormField f
        where f.dynaFormId  = :formId
          and f.dynaFormVer = :formVer
          and f.isDeleted   = false
     order by coalesce(f.order, 2147483647), f.dynaFormFieldsId
       """)
    List<DynaFormField> findByFormVersion(@Param("formId") Integer formId,
                                          @Param("formVer") Integer formVer);

    @Query("SELECT f.dynaFormFieldsId FROM DynaFormField f " +
            "WHERE f.dynaFormId = :formId AND f.dynaFormVer = :formVer AND f.isDeleted = false")
    List<Integer> findIdsByForm(@Param("formId") Integer formId, @Param("formVer") Integer formVer);


}
