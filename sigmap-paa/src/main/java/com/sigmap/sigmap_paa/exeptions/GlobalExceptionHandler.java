package com.sigmap.sigmap_paa.exeptions;

import com.github.dockerjava.api.exception.NotFoundException;
import com.sigmap.common.agpm.exceptions.MaxUploadSizeFile;
import com.sigmap.common.globalExceptions.BadRequest;
import com.sigmap.common.lots.exception.LotBudgetConsistencyException;
import com.sigmap.common.lots.exception.LotBudgetExceedsRealisationException;
import com.sigmap.common.paas.exceptions.*;
import com.sigmap.common.realisations.exceptions.RealisationBudgetExceedsAcSeuilException;
import com.sigmap.common.realisations.exceptions.RealisationsMinimumRequirement;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;



@RestControllerAdvice
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    private Locale getLocal() {
        return LocaleContextHolder.getLocale();
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BadRequest.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequest ex) {
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
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

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(PaaAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> paaNotFoundException(PaaAlreadyExistsException ex) {
        String message = messageSource.getMessage("app.alreadyexists", new Object[]{ex.getId(), ex.getVersion()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }


    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(RealisationsMinimumRequirement.class)
    public ResponseEntity<ErrorResponse> realisationsMinimumRequirementException(RealisationsMinimumRequirement ex) {
        String message = messageSource.getMessage("realisation.some.not.found", new Object[]{}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    @ExceptionHandler(MaxUploadSizeFile.class)
    public ResponseEntity<ErrorResponse> MaxUploadSizeFileException(MaxUploadSizeFile ex) {
        String message = messageSource.getMessage("doc.maxSize", new Object[]{ex.getMaxFileUploadSize()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.PAYLOAD_TOO_LARGE.value(), message);
        return new ResponseEntity<>(error, HttpStatus.PAYLOAD_TOO_LARGE);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(RealisationBudgetExceedsAcSeuilException.class)
    public ResponseEntity<ErrorResponse> handleRealisationBudgetExceedsAcSeuil(RealisationBudgetExceedsAcSeuilException ex) {
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PaaInvalidStateException.class)
    public ResponseEntity<Map<String, Object>> handlePaaInvalidStateException(PaaInvalidStateException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", 400);
        response.put("message", ex.getMessage());
        response.put("id", ex.getId());
        response.put("version", ex.getVersion());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PaaCreationLimitExceededException.class)
    public ResponseEntity<Map<String, Object>> handlePaaCreationLimitExceededException(PaaCreationLimitExceededException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", 400);
        response.put("message", ex.getMessage());
        response.put("id", ex.getId());
        response.put("version", ex.getVersion());
        response.put("limit", ex.getLimit());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(PaaCurrentYearNotFound.class)
    public ResponseEntity<ErrorResponse> MaxUploadSizeFileException(PaaCurrentYearNotFound ex) {
        String message = messageSource.getMessage("paa.notFound.CuurentYear", new Object[]{ex.getCurrentYear()}, getLocal());
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

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(PaaNotAssociatedWithUserAcException.class)
    public ResponseEntity<ErrorResponse> handlePaaNotAssociatedWithUserAc(PaaNotAssociatedWithUserAcException ex) {
        ErrorResponse error = new ErrorResponse(HttpStatus.FORBIDDEN.value(), ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException ex) {
        String message = messageSource.getMessage("general.notfound", new Object[] { ex.getMessage() }, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ErrorResponse> handleSecurityException(SecurityException ex) {
        String message = messageSource.getMessage("security.unauthorized", new Object[] { ex.getMessage() }, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.FORBIDDEN.value(), message);
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(AllAcsHavePaaException.class)
    public ResponseEntity<ErrorResponse> handleAllAcsHavePaaException(AllAcsHavePaaException ex) {
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(LotBudgetConsistencyException.class)
    public ResponseEntity<ErrorResponse> handleLotBudgetConsistencyException(LotBudgetConsistencyException ex) {
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(LotBudgetExceedsRealisationException.class)
    public ResponseEntity<ErrorResponse> handleLotBudgetExceedsRealisationException(LotBudgetExceedsRealisationException ex) {
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}