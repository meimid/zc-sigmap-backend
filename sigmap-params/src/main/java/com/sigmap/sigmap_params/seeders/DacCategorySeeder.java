package com.sigmap.sigmap_params.seeders;

import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.ref_params.RefParamService;
import com.sigmap.common.ref_params.RefParamSpecifications;
import com.sigmap.common.ref_types.RefType;
import com.sigmap.common.ref_types.RefTypeService;
import com.sigmap.common.ref_types.RefTypeSpecifications;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

/**
 * Seeder that runs on startup to check/populate system ref type (dac -> category) and it's params.
 */
@Component
@Slf4j
public class DacCategorySeeder implements ApplicationListener<ContextRefreshedEvent> {
  private final RefParamService refParamService;
  private final RefTypeService refTypeService;

  public static final Integer DAC_CATEGORY_REF_TYPE_ID = 50;
  public static final String DAC_CATEGORY_REF_TYPE_CODE = "DAC_CATEGORY";
  public static final String DAC_CATEGORY_REF_TYPE_DESCRIPTION = "Catégorie DAC";

  public static enum DacCategory {
    TRAVAUX("Travaux", "Catégorie - travaux");

    private final String code;
    private final String description;

    DacCategory(String code, String description) {
      this.code = code;
      this.description = description;
    }

    public String getCode() {
      return code;
    }

    public String getDescription() {
      return description;
    }
  }

  public DacCategorySeeder(RefParamService refParamService, RefTypeService refTypeService) {
    this.refParamService = refParamService;
    this.refTypeService = refTypeService;
  }

  @Override
  public void onApplicationEvent(ContextRefreshedEvent event) {
    log.info("Running dac category seeder...");
    RefType refType =
        refTypeService
            .findOne(RefTypeSpecifications.id(DAC_CATEGORY_REF_TYPE_ID))
            .orElse(new RefType());

    // If ref type is not found, create it
    if (refType.getId() == null) {
      refType.setId(DAC_CATEGORY_REF_TYPE_ID);
      refType.setDescription(DAC_CATEGORY_REF_TYPE_DESCRIPTION);
      refType.setRefCode(DAC_CATEGORY_REF_TYPE_CODE);
      refType.setSystemType(true);

      refTypeService.save(refType);
      log.info("Created ref type: {}", refType);
    } else {
      log.info("Ref type found: {}", refType);
    }

    for (DacCategory dacCategory : DacCategory.values()) {
      Specification<RefParam> spec =
          Specification.allOf(
              RefParamSpecifications.code(dacCategory.name()),
              RefParamSpecifications.refTypeId(DAC_CATEGORY_REF_TYPE_ID));

      RefParam refParam = refParamService.findOne(spec).orElse(null);

      if (refParam != null) {
        log.info("Ref param found: {}", refParam);
        continue;
      }

      log.info("Creating ref param: {}", dacCategory.name());
      RefParam newRefParam = new RefParam();
      newRefParam.setCode(dacCategory.getCode());
      newRefParam.setDescription(dacCategory.getDescription());
      newRefParam.setRefType(refType);

      refParamService.save(newRefParam);
      log.info("Created ref param: {}", newRefParam);
    }
  }
}
