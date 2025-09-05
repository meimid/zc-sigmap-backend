package com.sigmap.sigmap_dac.templates;

import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class TemplateServiceImpl implements TemplateService {

  @Value("${templateDac.dac.inputDocPath}")
  private String basePath;

  @Override
  public void uploadTemplate(MultipartFile file) {

    log.info("Uploading template file: {}", file.getOriginalFilename());

    if (exists(file.getOriginalFilename())) {
      log.error("Template already exists: {}", file.getOriginalFilename());
      throw new RuntimeException("Template already exists: " + file.getOriginalFilename());
    }

    try {
      Path templatePath = Paths.get(basePath, file.getOriginalFilename());

      OutputStream outputStream = Files.newOutputStream(templatePath, StandardOpenOption.CREATE);

      IOUtils.copy(file.getInputStream(), outputStream);

      // Files.copy(file.toPath(), templatePath);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public byte[] downloadTemplate(String templateName) {
    if (!exists(templateName)) {
      throw new RuntimeException("Template does not exist: " + templateName);
    }

    Path templatePath = Paths.get(basePath, templateName);

    try {
      return Files.readAllBytes(templatePath);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Check if the template exists.
   *
   * @param templateName the template name
   * @return true if the template exists, false otherwise
   */
  @Override
  public boolean exists(String templateName) {
    try {
      Path templatePath = Paths.get(basePath, templateName);
      return Files.exists(templatePath, LinkOption.NOFOLLOW_LINKS);
    } catch (Exception e) {
      log.error("Error checking if template exists: {}", e.getMessage());
      return false;
    }
  }

  @Override
  public TemplateResponse getTemplate(String templateName) {
    if (!exists(templateName)) {
      throw new RuntimeException("Template does not exist: " + templateName);
    }

    Path templatePath = Paths.get(basePath, templateName);

    File file = new File(templatePath.toString());

    return map(file);
  }

  @Override
  public List<TemplateResponse> getTemplates() {
    List<TemplateResponse> templateResponses = new ArrayList<>();

    File templateDir = new File(basePath);

    File[] files = templateDir.listFiles();

    log.info("Found {} template files", files.length);

    for (File file : files) {
      templateResponses.add(map(file));
    }

    return templateResponses;
  }

  /**
   * Map a file to a template response.
   *
   * @param file the file to map
   * @return the template response
   */
  private TemplateResponse map(File file) {

    TemplateResponse templateResponse = new TemplateResponse();

    String mimeType = null;

    try {
      mimeType = Files.probeContentType(Paths.get(file.getPath()));
    } catch (Exception e) {
      log.error("Error getting mime type: {}", e.getMessage());
      mimeType = "application/octet-stream";
    }

    templateResponse.setTemplateName(file.getName());
    templateResponse.setFileName(file.getName());
    templateResponse.setMimeType(mimeType);
    templateResponse.setSize(file.length());
    templateResponse.setLastModified(file.lastModified());

    return templateResponse;
  }

  @Override
  public void deleteTemplate(String templateName) {
    if (!exists(templateName)) {
      return;
    }

    Path templatePath = Paths.get(basePath, templateName);

    try {
      Files.delete(templatePath);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
