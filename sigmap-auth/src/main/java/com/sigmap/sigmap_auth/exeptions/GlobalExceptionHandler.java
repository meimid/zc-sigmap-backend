package com.sigmap.sigmap_auth.exeptions;

import com.sigmap.common.ac.exception.AcHasAssociatedPpm;
import com.sigmap.common.globalExceptions.BadRequest;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import com.sigmap.common.keycloakAdmin.exception.UserHasAssociatedAc;
import com.sigmap.common.keycloakAdmin.exception.UserHasAssociatedCom;
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

  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
    ErrorResponse error =
        new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An unexpected error occurred");
    return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(BadRequest.class)
  public ResponseEntity<ErrorResponse> handleBadRequest(BadRequest ex) {
    ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }
  @ResponseStatus(HttpStatus.CONFLICT)
  @ExceptionHandler(UserHasAssociatedAc.class)
  public ResponseEntity<ErrorResponse> UserHasAssociatedAcException(UserHasAssociatedAc ex) {
    String message = messageSource.getMessage("user.hasAssociatedAc",new Object[] {ex.getNom()}, getLocal());
    ErrorResponse error = new ErrorResponse(HttpStatus.CONFLICT.value(), message);
    return new ResponseEntity<>(error, HttpStatus.CONFLICT);
  }
  @ResponseStatus(HttpStatus.CONFLICT)
  @ExceptionHandler(UserHasAssociatedCom.class)
  public ResponseEntity<ErrorResponse> UserHasAssociatedComException(UserHasAssociatedCom ex) {
    String message = messageSource.getMessage("user.hasAssociatedCom",new Object[] {ex.getNom()}, getLocal());
    ErrorResponse error = new ErrorResponse(HttpStatus.CONFLICT.value(), message);
    return new ResponseEntity<>(error, HttpStatus.CONFLICT);
  }
}
