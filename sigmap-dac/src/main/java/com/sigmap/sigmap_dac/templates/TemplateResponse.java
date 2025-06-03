package com.sigmap.sigmap_dac.templates;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateResponse {
  private String templateName;

  private String fileName;

  private String mimeType;

  private Long size;
}
