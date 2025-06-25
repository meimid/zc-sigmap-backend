package com.sigmap.sigmap_ppm.exeptions;

import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.agpm.exceptions.MaxUploadSizeFile;
import com.sigmap.common.globalExceptions.BadRequest;
import com.sigmap.common.lots.exception.LotNotFound;
import com.sigmap.common.lots.exception.UpdateDeletedLot;
import com.sigmap.common.paas.exceptions.AllAcsHavePaaException;
import com.sigmap.common.paas.exceptions.PaaCreationLimitExceededException;
import com.sigmap.common.paas.exceptions.PaaIdNotFound;
import com.sigmap.common.paas.exceptions.PaaNotFound;
import com.sigmap.common.ppms.exception.*;
import com.sigmap.common.process_verbal.exception.ProcessVerbalNotFound;
import com.sigmap.common.realisations.exceptions.RealisationAlreadyExistsException;
import com.sigmap.common.realisations.exceptions.RealisationNotFoundException;
import com.sigmap.common.realisations.exceptions.RealisationsMinimumRequirement;
import com.sigmap.common.realisations.exceptions.UpdateDeletedRealisation;
import com.sigmap.common.ref_params.exceptions.RefParamNotFound;
import com.sigmap.common.ref_params.exceptions.ReferenceNotFound;
import com.sigmap.common.ref_types.exceptions.RefTypeNotFound;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    private Locale getLocal (){
        return LocaleContextHolder.getLocale();
    }





    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(RealisationNotFoundException.class)
    public ResponseEntity<ErrorResponse> realisationNotFoundException(RealisationNotFoundException ex) {
        String message = messageSource.getMessage("realisation.notfound",new Object[] {ex.getId(),ex.getVersion()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(PpmNotAssociatedWithUserAcException.class)
    public ResponseEntity<ErrorResponse> handlePpmNotAssociatedWithUserAc(PpmNotAssociatedWithUserAcException ex) {
        ErrorResponse error = new ErrorResponse(HttpStatus.FORBIDDEN.value(), ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(PpmCreationLimitExceededException.class)
    public ResponseEntity<Map<String, Object>> handlePpmCreationLimitExceededException(PpmCreationLimitExceededException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", 400);
        response.put("message", ex.getMessage());
        response.put("id", ex.getId());
        response.put("version", ex.getVersion());
        response.put("limit", ex.getLimit());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }


    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(RealisationAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> realisationAlreadyExistsException(RealisationAlreadyExistsException ex) {
        String message = messageSource.getMessage("realisation.alreadyexists",new Object[] {ex.getId(),ex.getVersion()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(RealisationsMinimumRequirement.class)
    public ResponseEntity<ErrorResponse> realisationsMinimumRequirementException(RealisationsMinimumRequirement ex) {
        String message = messageSource.getMessage("realisation.minimum.requirement",new Object[] {}, getLocal());
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

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(PaaNotFound.class)
    public ResponseEntity<ErrorResponse> paaNotFoundException(PaaNotFound ex) {
        String message = messageSource.getMessage("paa.notfound",new Object[] {
                        String.join((CharSequence) ",", ex.getId().stream().map(String::valueOf).collect(Collectors.toList())),
                        String.join((CharSequence) ",", ex.getVersion().stream().map(String::valueOf).collect(Collectors.toList())) },
                getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
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
    @ExceptionHandler(LotNotFound.class)
    public ResponseEntity<ErrorResponse> LotNotFoundException(LotNotFound ex) {
        String message = messageSource.getMessage("lot.notfound",new Object[] {
                        String.join((CharSequence) ",", ex.getId().stream().map(String::valueOf).collect(Collectors.toList())),
                        String.join((CharSequence) ",", ex.getVersion().stream().map(String::valueOf).collect(Collectors.toList())) },
                getLocal());
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

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(UpdateDeletedPpm.class)
    public ResponseEntity<ErrorResponse> updateDeletedPpmException(UpdateDeletedPpm ex) {
        String message = messageSource.getMessage("ppm.update.is.deleted",new Object[] {String.join((CharSequence) ",", ex.getId().stream().map(String::valueOf).collect(Collectors.toList())) } , getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(UpdateDeletedLot.class)
    public ResponseEntity<ErrorResponse> updateDeletedLotException(UpdateDeletedLot ex) {
        String message = messageSource.getMessage("lot.update.is.deleted",new Object[] {
                        String.join((CharSequence) ",", ex.getId().stream().map(String::valueOf).collect(Collectors.toList())),
                        String.join((CharSequence) ",", ex.getVersion().stream().map(String::valueOf).collect(Collectors.toList())) },
                getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(UpdateDeletedRealisation.class)
    public ResponseEntity<ErrorResponse> updateDeletedRealisationException(UpdateDeletedRealisation ex) {
        String message = messageSource.getMessage("realisation.minimum.requirement",new Object[] {}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

 /*   @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse error = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred");
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }*/

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BadRequest.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequest ex) {
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(RefParamNotFound.class)
    public ResponseEntity<ErrorResponse> handleRefParamNotFoundException(RefParamNotFound ex) {
        String message = messageSource.getMessage("refparam.notfound",new Object[] {String.join((CharSequence) ",", ex.getId().stream().map(String::valueOf).collect(Collectors.toList())) } , getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(RefTypeNotFound.class)
    public ResponseEntity<ErrorResponse> refTypeNotFoundException(RefTypeNotFound ex) {
        String message = messageSource.getMessage("reftype.notfound",new Object[] {ex.getCode()} , getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    @ExceptionHandler(MaxUploadSizeFile.class)
    public ResponseEntity<ErrorResponse> MaxUploadSizeFileException(MaxUploadSizeFile ex) {
        String message = messageSource.getMessage("doc.maxSize",new Object[] {ex.getMaxFileUploadSize()},getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.PAYLOAD_TOO_LARGE.value(), message);
        return new ResponseEntity<>(error, HttpStatus.PAYLOAD_TOO_LARGE);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidPpmStatusException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPpmStatusException(InvalidPpmStatusException ex) {
        String message = ex.getMessage();
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(PpmIdNotFound.class)
    public ResponseEntity<ErrorResponse> PpmIdNotFoundException(PpmIdNotFound ex) {
        String message = messageSource.getMessage("ppm.notFoundId",new Object[] {ex.getPpmId()},getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MissingComIdException.class)
    public ResponseEntity<ErrorResponse> handleMissingComIdException(MissingComIdException ex) {
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MissingAcIdException.class)
    public ResponseEntity<ErrorResponse> handleMissingAcIdException(MissingAcIdException ex) {
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(UserNotAssociatedWithAcException.class)
    public ResponseEntity<ErrorResponse> handleUserNotAssociatedWithAcException(UserNotAssociatedWithAcException ex) {
        ErrorResponse error = new ErrorResponse(HttpStatus.FORBIDDEN.value(), ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(UserNotAssociatedWithComException.class)
    public ResponseEntity<ErrorResponse> handleUserNotAssociatedWithComException(UserNotAssociatedWithComException ex) {
        ErrorResponse error = new ErrorResponse(HttpStatus.FORBIDDEN.value(), ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(AllAcsHavePpmException.class)
    public ResponseEntity<ErrorResponse> handleAllAcsHavePpmException(AllAcsHavePpmException ex) {
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ProcessVerbalNotFound.class)
    public ResponseEntity<ErrorResponse> ProcessVerbalNotFoundException(ProcessVerbalNotFound ex) {
        String message = messageSource.getMessage("ppm.ProcessVerbalNotFound",new Object[] {ex.getPpmId(),ex.getPpmId(),ex.getProcessVerbalId()},getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
}
