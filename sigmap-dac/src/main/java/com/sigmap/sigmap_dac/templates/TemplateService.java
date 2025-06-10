package com.sigmap.sigmap_dac.templates;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface TemplateService {
  void uploadTemplate(MultipartFile file);

  byte[] downloadTemplate(String templateName);

  boolean exists(String templateName);

  TemplateResponse getTemplate(String templateName);

  List<TemplateResponse> getTemplates();

  void deleteTemplate(String templateName);
}
