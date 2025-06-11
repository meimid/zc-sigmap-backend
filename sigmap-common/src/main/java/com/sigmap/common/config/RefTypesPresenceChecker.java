package com.sigmap.common.config;

import com.sigmap.common.ref_types.RefType;
import com.sigmap.common.ref_types.RefTypeService;
import com.sigmap.common.ref_types.RefTypeSpecifications;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RefTypesPresenceChecker implements ApplicationListener<ContextRefreshedEvent> {
  private final RefTypeService refTypeService;

  public RefTypesPresenceChecker(RefTypeService refTypeService) {
    this.refTypeService = refTypeService;
  }

  @Override
  public void onApplicationEvent(ContextRefreshedEvent event) {
    log.info("Checking presence of required ref types ....");

    for (RefTypes refType : RefTypes.values()) {
      log.info("Checking presence of ref type {}", refType.name());
      Specification<RefType> spec = RefTypeSpecifications.code(refType);

      List<RefType> refTypeByCode = refTypeService.findAllWithSystemType(spec);

      if (refTypeByCode.size() > 0) {
        log.info("Ref type {} already exists", refType.name());
        continue;
      }

      log.info("Ref type {} does not exist, creating it", refType.name());

      RefType refTypeToCreate = new RefType();
      refTypeToCreate.setRefCode(refType.name());
      refTypeToCreate.setDescription(refType.name());
      refTypeToCreate.setSystemType(false);
      refTypeService.save(refTypeToCreate);
    }
  }
}
