package com.sigmap.sigmap_params.exeptions;

import com.sigmap.common.ac.exception.AcHasAssociatedCom;
import com.sigmap.common.ac.exception.DuplicateAcIdsException;
import com.sigmap.common.authority.exeption.AuthorityNotFound;
import com.sigmap.common.com.exception.ComHasAssociatedAc;
import com.sigmap.common.com.exception.DuplicateComIdsException;
import com.sigmap.common.compte_bancaire.exception.CompteBancaireNotFound;
import com.sigmap.common.contact.exception.ContactNotFound;
import com.sigmap.common.globalExceptions.BadRequest;
import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.com.exception.ComNotFound;
import com.sigmap.common.lots.exception.LotNotFound;
import com.sigmap.common.lots.exception.UpdateDeletedLot;
import com.sigmap.common.menu_item.exception.MenuItemAlreadyExistsExeption;
import com.sigmap.common.menu_item.exception.MenuItemNotFound;
import com.sigmap.common.menu_item.exception.MenuItemSoftDeleteExeption;
import com.sigmap.common.menu_item.exception.MenuItemUpdateExeption;
import com.sigmap.common.paas.exceptions.PaaNotFound;
import com.sigmap.common.ppms.exception.PpmNotFound;
import com.sigmap.common.realisations.exceptions.RealisationAlreadyExistsException;
import com.sigmap.common.realisations.exceptions.RealisationNotFoundException;
import com.sigmap.common.realisations.exceptions.RealisationsMinimumRequirement;
import com.sigmap.common.realisations.exceptions.UpdateDeletedRealisation;
import com.sigmap.common.ref_params.exceptions.*;
import com.sigmap.common.ref_types.exceptions.HasAssociatedRefParams;
import com.sigmap.common.ref_types.exceptions.RefTypeAlreadyExist;
import com.sigmap.common.ref_types.exceptions.RefTypeNotFound;
import com.sigmap.common.ref_types.exceptions.RefTypePositiveIdAndNotEmpty;
import com.sigmap.common.societe.exception.SocieteHasAssociatedCompteBancaire;
import com.sigmap.common.societe.exception.SocieteHasAssociatedSocieteAddress;
import com.sigmap.common.societe.exception.SocieteNotFound;
import com.sigmap.common.societe_address.exception.SocieteAdresseNotFound;
import com.sigmap.common.societe_contact.exception.SocieteContactNotFound;
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
        String message = messageSource.getMessage("refparam.notfound",new Object[] {String.join((CharSequence) ",", ex.getId().stream().map(String::valueOf).collect(Collectors.toList())) } , getLocal());
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
    @ExceptionHandler(RefTypeNotFound.class)
    public ResponseEntity<ErrorResponse> refTypeNotFoundException(RefTypeNotFound ex) {
        String message = messageSource.getMessage("reftype.notfound",new Object[] {ex.getId()} , getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(RefTypeAlreadyExist.class)
    public ResponseEntity<ErrorResponse> refTypeAlreadyExistException(RefTypeAlreadyExist ex) {
        String message = messageSource.getMessage("reftype.alreadyexists",new Object[] {ex.getId()} , getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(RefTypePositiveIdAndNotEmpty.class)
    public ResponseEntity<ErrorResponse> refTypePositiveIdAndNotEmptyException(RefTypePositiveIdAndNotEmpty ex) {
        String message = messageSource.getMessage("reftype.positive.and.not.empty",new Object[] {} , getLocal());
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

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(CompteBancaireNotFound.class)
    public ResponseEntity<ErrorResponse> compteBancaireNotFoundException(CompteBancaireNotFound ex) {
        String message = messageSource.getMessage("compte.bancaire.notfound",new Object[] {String.join((CharSequence) ",", ex.getId().stream().map(String::valueOf).collect(Collectors.toList())) } , getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(SocieteNotFound.class)
    public ResponseEntity<ErrorResponse> societeNotFoundException(SocieteNotFound ex) {
        String message = messageSource.getMessage("societe.notfound",new Object[] {String.join((CharSequence) ",", ex.getId().stream().map(String::valueOf).collect(Collectors.toList())) } , getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(SocieteAdresseNotFound.class)
    public ResponseEntity<ErrorResponse> societeAdresseNotFoundException(SocieteAdresseNotFound ex) {
        String message = messageSource.getMessage("societeAdresse.notfound",new Object[] {String.join((CharSequence) ",", ex.getId().stream().map(String::valueOf).collect(Collectors.toList())) } , getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ContactNotFound.class)
    public ResponseEntity<ErrorResponse> ContactNotFoundException(ContactNotFound ex) {
        String message = messageSource.getMessage("contact.notfound",new Object[] {String.join((CharSequence) ",", ex.getId().stream().map(String::valueOf).collect(Collectors.toList())) } , getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(MenuItemNotFound.class)
    public ResponseEntity<ErrorResponse> menuItemNotFoundException(MenuItemNotFound ex) {
        String message = messageSource.getMessage("menuitem.notfound",new Object[] {String.join((CharSequence) ",", ex.getId().stream().map(String::valueOf).collect(Collectors.toList())) } , getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(MenuItemSoftDeleteExeption.class)
    public ResponseEntity<ErrorResponse> menuItemSoftDeleteExeption(MenuItemSoftDeleteExeption ex) {
        String message = messageSource.getMessage("menuitem.softdelete",new Object[] {ex.getNumber(),ex.getId()} , getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(MenuItemAlreadyExistsExeption.class)
    public ResponseEntity<ErrorResponse> authorityNotFoundException(MenuItemAlreadyExistsExeption ex) {
        String message = messageSource.getMessage("menuitem.alreadyexists",new Object[] {String.join((CharSequence) ",", ex.getId().stream().map(String::valueOf).collect(Collectors.toList())) } , getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(MenuItemUpdateExeption.class)
    public ResponseEntity<ErrorResponse> authorityNotFoundException(MenuItemUpdateExeption ex) {
        String message = messageSource.getMessage("menuitem.update.error",new Object[] {ex.getId() } , getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(AuthorityNotFound.class)
    public ResponseEntity<ErrorResponse> authorityNotFoundException(AuthorityNotFound ex) {
        String message = messageSource.getMessage("authority.notfound",new Object[] {String.join((CharSequence) ",", ex.getAuthority().stream().map(String::valueOf).collect(Collectors.toList())) } , getLocal());
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
    @ExceptionHandler(RealisationNotFoundException.class)
    public ResponseEntity<ErrorResponse> realisationNotFoundException(RealisationNotFoundException ex) {
        String message = messageSource.getMessage("realisation.notfound",new Object[] {ex.getId(),ex.getVersion()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
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
    @ExceptionHandler(UpdateDeletedRealisation.class)
    public ResponseEntity<ErrorResponse> updateDeletedRealisationException(UpdateDeletedRealisation ex) {
        String message = messageSource.getMessage("realisation.minimum.requirement",new Object[] {}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
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
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(HasAssociatedRefParams.class)
    public ResponseEntity<ErrorResponse> HasAssociatedRefParamsException(HasAssociatedRefParams ex) {
        String message = messageSource.getMessage("refType.hasAssociatedRefParams",new Object[] {ex.getId()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.CONFLICT.value(), message);
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(RefParamHasAssociatedCom.class)
    public ResponseEntity<ErrorResponse> RefPramHasAssociatedComException(RefParamHasAssociatedCom ex) {
        String message = messageSource.getMessage("refparam.hasAssociatedCom",new Object[] {ex.getId()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.CONFLICT.value(), message);
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(RefParamHasAssociatedCompteBancaire.class)
    public ResponseEntity<ErrorResponse> RefPramHasAssociatedCompteBancaireException(RefParamHasAssociatedCompteBancaire ex) {
        String message = messageSource.getMessage("refparam.hasAssociatedCompteBancaire",new Object[] {ex.getId()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.CONFLICT.value(), message);
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(SocieteHasAssociatedCompteBancaire.class)
    public ResponseEntity<ErrorResponse> SocieteHasAssociatedCompteBancaireException(SocieteHasAssociatedCompteBancaire ex) {
        String message = messageSource.getMessage("societe.hasAssociatedCompteBancaire",new Object[] {ex.getId()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.CONFLICT.value(), message);
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(RefParamHasAssociatedSociete.class)
    public ResponseEntity<ErrorResponse> RefParamhasAssociatedSocieteException(RefParamHasAssociatedSociete ex) {
        String message = messageSource.getMessage("refparam.hasAssociatedSociete",new Object[] {ex.getId()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.CONFLICT.value(), message);
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(RefParamHasAssociatedSocieteAddress.class)
    public ResponseEntity<ErrorResponse> RefParamHasAssociatedSocieteAddressException(RefParamHasAssociatedSocieteAddress ex) {
        String message = messageSource.getMessage("refparam.hasAssociatedSocieteAddress",new Object[] {ex.getId()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.CONFLICT.value(), message);
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(SocieteHasAssociatedSocieteAddress.class)
    public ResponseEntity<ErrorResponse> SocieteHasAssociatedSocieteAddressException(SocieteHasAssociatedSocieteAddress ex) {
        String message = messageSource.getMessage("societe.hasAssociatedSocieteAddress",new Object[] {ex.getId()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.CONFLICT.value(), message);
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(ComHasAssociatedAc.class)
    public ResponseEntity<ErrorResponse> ComHasAssociatedAcException(ComHasAssociatedAc ex) {
        String message = messageSource.getMessage("com.hasAssociatedAc",new Object[] {ex.getId()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.CONFLICT.value(), message);
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(AcHasAssociatedCom.class)
    public ResponseEntity<ErrorResponse> AcHasAssociatedComException(AcHasAssociatedCom ex) {
        String message = messageSource.getMessage("ac.hasAssociatedcom",new Object[] {ex.getId()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.CONFLICT.value(), message);
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(SocieteContactNotFound.class)
    public ResponseEntity<ErrorResponse> SocieteContactNotFoundException(SocieteContactNotFound ex) {
        String message = messageSource.getMessage("societe.societeContactNotFound", new Object[]{ex.getSocieteId(), ex.getContactId()}, getLocal());

        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
}
