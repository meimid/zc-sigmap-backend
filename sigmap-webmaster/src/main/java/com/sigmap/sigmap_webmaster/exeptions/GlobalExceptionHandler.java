package com.sigmap.sigmap_webmaster.exeptions;

import com.sigmap.common.agpm.exceptions.AgpmNotFound;
import com.sigmap.common.dac.exeption.*;
import java.util.Locale;
import java.util.stream.Collectors;

import com.sigmap.common.paas.exceptions.PaaIdNotFound;
import com.sigmap.common.paas.exceptions.PaaNotFound;
import com.sigmap.common.ppms.exception.PpmIdNotFound;
import com.sigmap.common.ppms.exception.PpmNotFound;
import com.sigmap.common.societe.exception.*;
import com.sigmap.common.societe_address.exception.AddressIdRequiredException;
import com.sigmap.common.societe_address.exception.SocieteAddressAlreadyExist;
import com.sigmap.common.societe_address.exception.TypeIdRequiredException;
import com.sigmap.common.user_societe.exception.*;
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

  @ResponseStatus(HttpStatus.CONFLICT)
  @ExceptionHandler(DuplicateActiviteIds.class)
  public ResponseEntity<ErrorResponse> DuplicateActiviteIdsException(DuplicateActiviteIds ex) {
    String message = messageSource.getMessage("duplicate.activite",new Object[] {String.join((CharSequence) ",", ex.getId().stream().map(String::valueOf).collect(Collectors.toList())) } , getLocal());
    ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(UserSocieteNotFound.class)
  public ResponseEntity<ErrorResponse> handleUserSocieteNotFound(UserSocieteNotFound ex) {
    String message = messageSource.getMessage("userSociete.notFound", new Object[]{}, getLocal());
    ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
    return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
  }

  @ResponseStatus(HttpStatus.CONFLICT)
  @ExceptionHandler(UserSocieteAlreadyExist.class)
  public ResponseEntity<ErrorResponse> handleUserSocieteAlreadyExist(UserSocieteAlreadyExist ex) {
    String message = messageSource.getMessage("userSociete.alreadyExist", new Object[]{}, getLocal());
    ErrorResponse error = new ErrorResponse(HttpStatus.CONFLICT.value(), message);
    return new ResponseEntity<>(error, HttpStatus.CONFLICT);
  }

  @ResponseStatus(HttpStatus.CONFLICT)
  @ExceptionHandler(HasAssociatedUserSociete.class)
  public ResponseEntity<ErrorResponse> handleHasAssociatedUserSociete(HasAssociatedUserSociete ex) {
    String message = messageSource.getMessage("userSociete.hasAssociated", new Object[]{}, getLocal());
    ErrorResponse error = new ErrorResponse(HttpStatus.CONFLICT.value(), message);
    return new ResponseEntity<>(error, HttpStatus.CONFLICT);
  }

  @ResponseStatus(HttpStatus.CONFLICT)
  @ExceptionHandler(UserAlreadyRegisteredToOtherCompany.class)
  public ResponseEntity<ErrorResponse> handleUserSocieteAlreadyToOtherCompany(UserAlreadyRegisteredToOtherCompany ex) {
    String message = messageSource.getMessage("userSociete.alreadyToOtherCompany", new Object[]{}, getLocal());
    ErrorResponse error = new ErrorResponse(HttpStatus.CONFLICT.value(), message);
    return new ResponseEntity<>(error, HttpStatus.CONFLICT);
  }
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(UserIsResponsableForThisUserSociete.class)
  public ResponseEntity<ErrorResponse> handleUserIsResponsableForThisUserSociete(UserIsResponsableForThisUserSociete ex) {
    String message = messageSource.getMessage("userSociete.isResponsableForThisSociete", new Object[]{}, getLocal());
    ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(UserNeedToBeActive.class)
  public ResponseEntity<ErrorResponse> userNeedToBeActiveException(UserNeedToBeActive ex) {
    String message = messageSource.getMessage("userSociete.userNeedToBeActive", null, getLocal());
    ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(SocieteNeedToBeValidate.class)
  public ResponseEntity<ErrorResponse> societeNeedToBeValidateException(SocieteNeedToBeValidate ex) {
    String message = messageSource.getMessage("userSociete.societeNeedToBeValidate", null, getLocal());
    ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(NifIsRequiredException.class)
  public ResponseEntity<ErrorResponse> handleNifIsRequiredException(NifIsRequiredException ex) {
    String message = messageSource.getMessage("societe.nif.required", null, getLocal());
    ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(DateNifIsRequiredException.class)
  public ResponseEntity<ErrorResponse> handleDateNifIsRequiredException(DateNifIsRequiredException ex) {
    String message = messageSource.getMessage("societe.dateNif.required", null, getLocal());
    ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(NumCnssIsRequiredException.class)
  public ResponseEntity<ErrorResponse> handleNumCnssIsRequiredException(NumCnssIsRequiredException ex) {
    String message = messageSource.getMessage("societe.numCnss.required", null, getLocal());
    ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(TypeIsRequiredException.class)
  public ResponseEntity<ErrorResponse> handleTypeIsRequiredException(TypeIsRequiredException ex) {
    String message = messageSource.getMessage("societe.type.required", null, getLocal());
    ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(PaysIsRequiredException.class)
  public ResponseEntity<ErrorResponse> handlePaysIsRequiredException(PaysIsRequiredException ex) {
    String message = messageSource.getMessage("societe.pays.required", null, getLocal());
    ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(CategorieSoumissionnaireIsRequiredException.class)
  public ResponseEntity<ErrorResponse> handleCategorieSoumissionnaireIsRequiredException(CategorieSoumissionnaireIsRequiredException ex) {
    String message = messageSource.getMessage("societe.categorieSoumissionnaire.required", null, getLocal());
    ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(ActiviteIsRequiredException.class)
  public ResponseEntity<ErrorResponse> handleActiviteIsRequiredException(ActiviteIsRequiredException ex) {
    String message = messageSource.getMessage("societe.activite.required", null, getLocal());
    ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(CantResponsableNonActif.class)
  public ResponseEntity<ErrorResponse> cantResponsableNonActifException(CantResponsableNonActif ex) {
    String message = messageSource.getMessage("userSociete.cantResponsableNonActif", null, getLocal());
    ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(UserIdRequiredException.class)
  public ResponseEntity<ErrorResponse> handleUserIdRequired(UserIdRequiredException ex) {
    String message = messageSource.getMessage("user.id.required", null, getLocal());
    ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }

  // 🔹 Societe ID required
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(SocieteIdRequiredException.class)
  public ResponseEntity<ErrorResponse> handleSocieteIdRequired(SocieteIdRequiredException ex) {
    String message = messageSource.getMessage("societe.id.required", null, getLocal());
    ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }

  // 🔹 Address ID required
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(AddressIdRequiredException.class)
  public ResponseEntity<ErrorResponse> handleAddressIdRequired(AddressIdRequiredException ex) {
    String message = messageSource.getMessage("address.id.required", null, getLocal());
    ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }

  // 🔹 Type ID required
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(TypeIdRequiredException.class)
  public ResponseEntity<ErrorResponse> handleTypeIdRequired(TypeIdRequiredException ex) {
    String message = messageSource.getMessage("type.id.required", null, getLocal());
    ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }
  @ResponseStatus(HttpStatus.CONFLICT)
  @ExceptionHandler(SocieteAddressAlreadyExist.class)
  public ResponseEntity<ErrorResponse> handleSocieteAddressAlreadyExist(SocieteAddressAlreadyExist ex) {
    String message = messageSource.getMessage("societe.address.already.exist", null, getLocal());
    ErrorResponse error = new ErrorResponse(HttpStatus.CONFLICT.value(), message);
    return new ResponseEntity<>(error, HttpStatus.CONFLICT);
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(AddressRequiredException.class)
  public ResponseEntity<ErrorResponse> handleAddressRequiredException(AddressRequiredException ex) {
    String message = messageSource.getMessage("societe.address.required", null, getLocal());
    ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(CodePostalRequiredException.class)
  public ResponseEntity<ErrorResponse> handleCodePostalRequiredException(CodePostalRequiredException ex) {
    String message = messageSource.getMessage("societe.codepostal.required", null, getLocal());
    ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(TelIsRequiredException.class)
  public ResponseEntity<ErrorResponse> handleTelIsRequiredException(TelIsRequiredException ex) {
    String message = messageSource.getMessage("societe.tel.required", null, getLocal());
    ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(EmailRequiredException.class)
  public ResponseEntity<ErrorResponse> handleEmailRequiredException(EmailRequiredException ex) {
    String message = messageSource.getMessage("societe.email.required", null, getLocal());
    ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(NameIsRequiredException.class)
  public ResponseEntity<ErrorResponse> handleNameIsRequiredException(NameIsRequiredException ex) {
    String message = messageSource.getMessage("societe.nom.required", null, getLocal());
    ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }

}
