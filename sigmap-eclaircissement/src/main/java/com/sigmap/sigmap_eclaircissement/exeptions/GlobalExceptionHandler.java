package com.sigmap.sigmap_eclaircissement.exeptions;

import com.github.dockerjava.api.exception.NotFoundException;
import com.sigmap.common.agpm.exceptions.MaxUploadSizeFile;
import com.sigmap.common.globalExceptions.BadRequest;
import com.sigmap.common.lots.exception.LotBudgetConsistencyException;
import com.sigmap.common.lots.exception.LotBudgetExceedsRealisationException;
import com.sigmap.common.model_paa.exceptions.PaaModelShouldExiste;
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



}