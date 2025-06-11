package com.sigmap.sigmap_agpm.exeptions;


import com.github.dockerjava.api.exception.NotFoundException;
import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.agpm.exceptions.*;
import com.sigmap.common.docs.exeptions.DocNotFound;
import com.sigmap.common.globalExceptions.BadRequest;
import com.sigmap.common.ref_params.exceptions.ReferenceNotFound;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Locale;
import java.util.stream.Collectors;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    private Locale getLocal (){
        return LocaleContextHolder.getLocale();
    }



    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse error = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred");
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BadRequest.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequest ex) {
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }


    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AgpmNotAssociatedWithUserAcException.class)
    public ResponseEntity<ErrorResponse> handleAgpmNotAssociatedWithUserAc(AgpmNotAssociatedWithUserAcException ex) {
        ErrorResponse error = new ErrorResponse(HttpStatus.FORBIDDEN.value(), ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
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

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(AgpmAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> agpmAlreadyExistsException(AgpmAlreadyExistsException ex) {
        String message = messageSource.getMessage("agpm.alreadyexists",new Object[] { ex.getId(),ex.getVersion()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(AgpmNotNull.class)
    public ResponseEntity<ErrorResponse> agpmNotNullException(AgpmNotNull ex) {
        String message = messageSource.getMessage("agpm.notnull",new Object[] { }, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(AcNotFound.class)
    public ResponseEntity<ErrorResponse> handleAcNotFoundException(AcNotFound ex) {
        String message = messageSource.getMessage("ac.notfound",new Object[] {String.join((CharSequence) ",", ex.getId().stream().map(String::valueOf).collect(Collectors.toList())) } , getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(DocNotFound.class)
    public ResponseEntity<ErrorResponse> handleDocNotFoundException(DocNotFound ex) {
        String message = messageSource.getMessage("doc.notfound",new Object[] {String.join((CharSequence) ",", ex.getId().stream().map(String::valueOf).collect(Collectors.toList())) } , getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(AgpmDefaultStatus.class)
    public ResponseEntity<ErrorResponse> handleAgpmDefaultStatusException(AgpmDefaultStatus ex) {
        String message = messageSource.getMessage("agpm.default.status.notfound",new Object[] {} , getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(AgpmSoftDelete.class)
    public ResponseEntity<ErrorResponse> handleAgpmDefaultStatusException(AgpmSoftDelete ex) {
        String message = messageSource.getMessage("agpm.softdelete",new Object[] {} , getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ReferenceNotFound.class)
    public ResponseEntity<ErrorResponse> referenceNotFoundException(ReferenceNotFound ex) {
        String message = messageSource.getMessage("reference.notfound",new Object[] {String.join((CharSequence) ",", ex.getReference().stream().map(String::valueOf).collect(Collectors.toList())) } , getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidIdAndVersionAgpm.class)
    public ResponseEntity<ErrorResponse> invalidIdAndVersionAgpmException(InvalidIdAndVersionAgpm ex) {
        String message = messageSource.getMessage("agpm.positif",new Object[] {
                String.join((CharSequence) ",", ex.getId().stream().map(String::valueOf).collect(Collectors.toList())),
                String.join((CharSequence) ",", ex.getVersion().stream().map(String::valueOf).collect(Collectors.toList()))},getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    @ExceptionHandler(MaxUploadSizeFile.class)
    public ResponseEntity<ErrorResponse> MaxUploadSizeFileException(MaxUploadSizeFile ex) {
        String message = messageSource.getMessage("doc.maxSize",new Object[] {ex.getMaxFileUploadSize()},getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.PAYLOAD_TOO_LARGE.value(), message);
        return new ResponseEntity<>(error, HttpStatus.PAYLOAD_TOO_LARGE);
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AgpmInvalidStatusException.class)
    public ResponseEntity<ErrorResponse> handleAgpmInvalidStatusException(AgpmInvalidStatusException ex) {
        String message = messageSource.getMessage("agpm.invalid.status", new Object[] {
                String.join((CharSequence) ",", ex.getAgpmIds().stream().map(String::valueOf).collect(Collectors.toList())),
                String.join((CharSequence) ",", ex.getAgpmVersions().stream().map(String::valueOf).collect(Collectors.toList())),
                ex.getCurrentStatus() }, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.FORBIDDEN.value(), message);
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    // New Handler for NotFoundException
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException ex) {
        String message = messageSource.getMessage("general.notfound", new Object[] { ex.getMessage() }, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    // New Handler for SecurityException
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ErrorResponse> handleSecurityException(SecurityException ex) {
        String message = messageSource.getMessage("security.unauthorized", new Object[] { ex.getMessage() }, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.FORBIDDEN.value(), message);
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }


}
