package com.sigmap.sigmap_webmaster.exeptions;

import com.sigmap.common.dac.exeption.*;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private final MessageSource messageSource;

  public GlobalExceptionHandler(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  private Locale getLocal() {
    return LocaleContextHolder.getLocale();
  }
}
