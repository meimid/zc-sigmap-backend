package com.sigmap.sigmap_params.exeptions;

import com.sigmap.common.ac.exception.DuplicateAcIdsException;
import com.sigmap.common.com.exception.DuplicateComIdsException;
import com.sigmap.common.globalExceptions.BadRequest;
import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.com.exception.ComNotFound;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Locale;
import java.util.stream.Collectors;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    private Locale getLocal (){
        return LocaleContextHolder.getLocale();
    }
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(AcNotFound.class)
    public ResponseEntity<ErrorResponse> handleAcNotFoundException(AcNotFound ex) {
        String message = messageSource.getMessage("ac.notfound",new Object[] {String.join((CharSequence) ",", ex.getId().stream().map(String::valueOf).collect(Collectors.toList())) } , getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(RefParamNotFound.class)
    public ResponseEntity<ErrorResponse> handleRefParamNotFoundException(RefParamNotFound ex) {
        String message = messageSource.getMessage("ref.notfound",new Object[] {String.join((CharSequence) ",", ex.getId().stream().map(String::valueOf).collect(Collectors.toList())) } , getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ComNotFound.class)
    public ResponseEntity<ErrorResponse> handleComNotFoundException(ComNotFound ex) {

        String message = messageSource.getMessage("com.notfound", new Object[] {String.join((CharSequence) ",", ex.getId().stream().map(String::valueOf).collect(Collectors.toList())) } , getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(DuplicateComIdsException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateComIdsException(DuplicateComIdsException ex) {
        String message = messageSource.getMessage("duplicate.com",new Object[] {String.join((CharSequence) ",", ex.getId().stream().map(String::valueOf).collect(Collectors.toList())) } , getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(DuplicateAcIdsException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateAcIdsException(DuplicateAcIdsException ex) {
        String message = messageSource.getMessage("duplicate.ac",new Object[] {String.join((CharSequence) ",", ex.getId().stream().map(String::valueOf).collect(Collectors.toList())) } , getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadRequest.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequest ex) {
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse error = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred");
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
