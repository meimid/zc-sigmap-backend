package com.sigmap.sigmap_dac.templates;

import lombok.Data;

@Data
public class TemplateResponse {
  private String templateName;

  private String mimeType;

  private Integer size;
}
