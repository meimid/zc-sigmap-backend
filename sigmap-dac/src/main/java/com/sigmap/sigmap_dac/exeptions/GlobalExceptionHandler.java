package com.sigmap.sigmap_dac.exeptions;

import com.sigmap.common.dac.exeption.*;
import com.sigmap.common.globalExceptions.BadRequest;
import java.util.Locale;

import com.sigmap.common.ppms.exception.PpmIdNotFound;
import com.sigmap.common.realisations.exceptions.RealisationNotFoundException;
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

  // @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  // @ExceptionHandler(Exception.class)
  // public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
  //     ErrorResponse error = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
  //             "An unexpected error occurred");
  //     return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
  // }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(BadRequest.class)
  public ResponseEntity<ErrorResponse> handleBadRequest(BadRequest ex) {
    ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(TemplateFileNameNotFound.class)
  public ResponseEntity<ErrorResponse> templateFileNameNotFoundExeption(
      TemplateFileNameNotFound ex) {
    String message =
        messageSource.getMessage(
            "dac.template.notFound", new Object[] {ex.getDacId(), ex.getDacVersion()}, getLocal());
    ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
    return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(RequiredDacIDAndVersion.class)
  public ResponseEntity<ErrorResponse> requiredDacIDAndVersionException(
      RequiredDacIDAndVersion ex) {
    String message =
        messageSource.getMessage("dac.required.idAndVersion", new Object[] {}, getLocal());
    ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
    return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(RequiredPath.class)
  public ResponseEntity<ErrorResponse> RequiredPathException(RequiredPath ex) {
    String message = messageSource.getMessage("dac.required.paths", new Object[] {}, getLocal());
    ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
    return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(LoadDocumentExeption.class)
  public ResponseEntity<ErrorResponse> RequiredPathException(LoadDocumentExeption ex) {
    String message =
        messageSource.getMessage("dac.failed.LoadDocuments", new Object[] {}, getLocal());
    ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
    return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(InputFileDocument.class)
  public ResponseEntity<ErrorResponse> inputFileDocumentException(InputFileDocument ex) {
    String message =
        messageSource.getMessage("dac.failed.inputFileDocument", new Object[] {}, getLocal());
    ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
    return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(SaveTempDocument.class)
  public ResponseEntity<ErrorResponse> saveTempDocumentException(SaveTempDocument ex) {
    String message =
        messageSource.getMessage("dac.failed.saveTempFile", new Object[] {}, getLocal());
    ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
    return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(PpmIdNotFound.class)
  public ResponseEntity<ErrorResponse> ppmIdNotFoundExeption(PpmIdNotFound ex) {
    String message = messageSource.getMessage( "ppm.notFound", new Object[] {ex.getPpmId(), ex.getPpmver()}, getLocal());
    ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
    return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(RealisationNotFoundException.class)
  public ResponseEntity<ErrorResponse> realisationNotFoundException(RealisationNotFoundException ex) {
    String message = messageSource.getMessage("realisation.notfound",new Object[] {ex.getId(),ex.getVersion()}, getLocal());
    ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
    return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
  }
}
