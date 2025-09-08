package com.sigmap.sigmap_webmaster.exeptions;

import com.sigmap.common.agpm.exceptions.AgpmNotFound;
import com.sigmap.common.dac.exeption.*;
import java.util.Locale;
import java.util.stream.Collectors;

import com.sigmap.common.paas.exceptions.PaaIdNotFound;
import com.sigmap.common.paas.exceptions.PaaNotFound;
import com.sigmap.common.ppms.exception.PpmIdNotFound;
import com.sigmap.common.ppms.exception.PpmNotFound;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
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
  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(PpmNotFound.class)
  public ResponseEntity<ErrorResponse> ppmNotFoundException(PpmNotFound ex) {
    String message = messageSource.getMessage("ppm.notfound",new Object[] {
                    String.join((CharSequence) ",", ex.getId().stream().map(String::valueOf).collect(Collectors.toList())),
                    String.join((CharSequence) ",", ex.getVersion().stream().map(String::valueOf).collect(Collectors.toList())) },
            getLocal());
    ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
    return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);

  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(PpmIdNotFound.class)
  public ResponseEntity<ErrorResponse> PpmIdNotFoundException(PpmIdNotFound ex) {
    String message = messageSource.getMessage("ppm.notFoundId",new Object[] {ex.getPpmId()},getLocal());
    ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
    return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(PaaNotFound.class)
  public ResponseEntity<ErrorResponse> paaNotFoundException(PaaNotFound ex) {
    String message = messageSource.getMessage("paa.notfound", new Object[]{
                    String.join((CharSequence) ",", ex.getId().stream().map(String::valueOf).collect(Collectors.toList())),
                    String.join((CharSequence) ",", ex.getVersion().stream().map(String::valueOf).collect(Collectors.toList()))},
            getLocal());
    ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
    return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(PaaIdNotFound.class)
  public ResponseEntity<ErrorResponse> PaaIdNotFoundException(PaaIdNotFound ex) {
    String message = messageSource.getMessage("paa.notFoundId", new Object[]{ex.getPaaId()}, getLocal());
    ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
    return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(AgpmNotFound.class)
  public ResponseEntity<ErrorResponse> agpmNotFoundException(AgpmNotFound ex) {
    String message = messageSource.getMessage("agpm.notfound",new Object[] {
                    String.join((CharSequence) ",", ex.getId().stream().map(String::valueOf).collect(Collectors.toList())),
                    String.join((CharSequence) ",", ex.getVersion().stream().map(String::valueOf).collect(Collectors.toList())) },
            getLocal());
    ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
    return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
  }
}
