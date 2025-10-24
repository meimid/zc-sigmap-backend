package com.sigmap.sigmap_website_auth.exeptions;

import lombok.Data;

@Data
public class ErrorResponse {
  private int status;
  private String message;

  public ErrorResponse() {}

  public ErrorResponse(int status, String message) {
    this.status = status;
    this.message = message;
  }
}

