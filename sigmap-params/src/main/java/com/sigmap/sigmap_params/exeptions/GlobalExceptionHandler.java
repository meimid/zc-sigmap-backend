package com.sigmap.sigmap_params.exeptions;

import com.sigmap.common.ModeDePassationSelection.exception.DuplicateTypeMarcherIdsException;
import com.sigmap.common.ModeDePassationSelection.exception.ModeDePassationSelectionAlredyExist;
import com.sigmap.common.ModeDePassationSelection.exception.ModeDePassationSelectionNotFound;
import com.sigmap.common.ModeDePassationSelection.exception.RequiredValue;
import com.sigmap.common.Participant.exception.ParticipantNotFound;
import com.sigmap.common.ac.exception.*;
import com.sigmap.common.adresse.exceptions.AdresseNotFound;
import com.sigmap.common.authority.exeption.AuthorityNotFound;
import com.sigmap.common.com.exception.*;
import com.sigmap.common.compte_bancaire.exception.CompteBancaireNotFound;
import com.sigmap.common.contact.exception.ContactDeleted;
import com.sigmap.common.contact.exception.ContactNotFound;
import com.sigmap.common.dac.exeption.DacNotFound;
import com.sigmap.common.direction.exception.*;
import com.sigmap.common.docs.exeptions.DocDeleted;
import com.sigmap.common.globalExceptions.BadRequest;
import com.sigmap.common.keycloakAdmin.exception.UserDoesNotHavePermission;
import com.sigmap.common.lots.exception.LotNotFound;
import com.sigmap.common.lots.exception.UpdateDeletedLot;
import com.sigmap.common.menu_item.exception.MenuItemAlreadyExistsExeption;
import com.sigmap.common.menu_item.exception.MenuItemNotFound;
import com.sigmap.common.menu_item.exception.MenuItemSoftDeleteExeption;
import com.sigmap.common.menu_item.exception.MenuItemUpdateExeption;
import com.sigmap.common.monnaie.exceptions.MonnaieCodeAlreadyExist;
import com.sigmap.common.monnaie.exceptions.MonnaieNotFound;
import com.sigmap.common.paas.exceptions.PaaNotFound;
import com.sigmap.common.parametrage_process_dac.exeptions.ParametrageProcessDacNotFound;
import com.sigmap.common.ppms.exception.PpmNotFound;
import com.sigmap.common.process_verbal_reunion.exception.ProcessVerbalReunionNotFound;
import com.sigmap.common.realisations.exceptions.RealisationAlreadyExistsException;
import com.sigmap.common.realisations.exceptions.RealisationNotFoundException;
import com.sigmap.common.realisations.exceptions.RealisationsMinimumRequirement;
import com.sigmap.common.realisations.exceptions.UpdateDeletedRealisation;
import com.sigmap.common.ref_params.exceptions.*;
import com.sigmap.common.ref_types.exceptions.*;
import com.sigmap.common.reunion.exception.DuplicationData;
import com.sigmap.common.reunion.exception.ReunionDate;
import com.sigmap.common.reunion.exception.ReunionNotFound;
import com.sigmap.common.reunion.exception.ReunionStatusException;
import com.sigmap.common.societe.exception.SocieteHasAssociatedCompteBancaire;
import com.sigmap.common.societe.exception.SocieteHasAssociatedSocieteAddress;
import com.sigmap.common.societe.exception.SocieteNotFound;
import com.sigmap.common.societe_address.exception.SocieteAdresseNotFound;
import com.sigmap.common.societe_contact.exception.SocieteContactAlreadyExist;
import com.sigmap.common.societe_contact.exception.SocieteContactNotFound;
import com.sigmap.common.user_com.exception.UserComNotFound;
import com.sigmap.common.user_com.exception.UserResponsibleForCom;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.security.authorization.AuthorizationDeniedException;
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
        String message = messageSource.getMessage("reftype.notfound",new Object[] {ex.getCode()} , getLocal());
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

    // @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    // @ExceptionHandler(Exception.class)
    // public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
    //     ErrorResponse error = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
    //             "An unexpected error occurred");
    //     return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    // }

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
/*    // Handler for MaxUploadSizeExceededException (from Spring's MultipartResolver)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        String message = messageSource.getMessage("doc.maxSize", new Object[] {}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.PAYLOAD_TOO_LARGE.value(), message);
        return new ResponseEntity<>(error, HttpStatus.PAYLOAD_TOO_LARGE);
    }*/

    @ResponseStatus(HttpStatus.GONE)
    @ExceptionHandler(AcDeleted.class)
    public ResponseEntity<ErrorResponse> acDeletedException(AcDeleted ex) {
        String message = messageSource.getMessage("ac.deleted",new Object[] {String.join((CharSequence) ",", ex.getId().stream().map(String::valueOf).collect(Collectors.toList())) } , getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.GONE.value(), message);
        return new ResponseEntity<>(error, HttpStatus.GONE);
    }

    @ResponseStatus(HttpStatus.GONE)
    @ExceptionHandler(ComDeleted.class)
    public ResponseEntity<ErrorResponse> comDeletedException(ComDeleted ex) {
        String message = messageSource.getMessage("com.deleted",new Object[] {String.join((CharSequence) ",", ex.getId().stream().map(String::valueOf).collect(Collectors.toList())) } , getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.GONE.value(), message);
        return new ResponseEntity<>(error, HttpStatus.GONE);
    }
    @ResponseStatus(HttpStatus.GONE)
    @ExceptionHandler(RefTypeDeleted.class)
    public ResponseEntity<ErrorResponse> RefTypeDeletedException(RefTypeDeleted ex) {
        String message = messageSource.getMessage("refType.deleted",new Object[] {ex.getId() } , getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.GONE.value(), message);
        return new ResponseEntity<>(error, HttpStatus.GONE);
    }

    @ResponseStatus(HttpStatus.GONE)
    @ExceptionHandler(RefParamDeleted.class)
    public ResponseEntity<ErrorResponse> RefParamDeletedException(RefParamDeleted ex) {
        String message = messageSource.getMessage("refparam.deleted",new Object[] {ex.getId() } , getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.GONE.value(), message);
        return new ResponseEntity<>(error, HttpStatus.GONE);
    }

    @ResponseStatus(HttpStatus.GONE)
    @ExceptionHandler(ContactDeleted.class)
    public ResponseEntity<ErrorResponse> ContactDeletedException(ContactDeleted ex) {
        String message = messageSource.getMessage("contact.deleted",new Object[] {ex.getId() } , getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.GONE.value(), message);
        return new ResponseEntity<>(error, HttpStatus.GONE);
    }

    @ResponseStatus(HttpStatus.GONE)
    @ExceptionHandler(DocDeleted.class)
    public ResponseEntity<ErrorResponse> DocDeletedException(DocDeleted ex) {
        String message = messageSource.getMessage("doc.deleted",new Object[] {ex.getId() } , getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.GONE.value(), message);
        return new ResponseEntity<>(error, HttpStatus.GONE);
    }
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(SocieteContactAlreadyExist.class)
    public ResponseEntity<ErrorResponse> societeContactAlreadyExistException(SocieteContactAlreadyExist ex) {
        String message = messageSource.getMessage("societe.societeContactAlreadyExist",new Object[] {ex.getSocieteId(), ex.getContactId()},getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(HasAssociatedPpm.class)
    public ResponseEntity<ErrorResponse> RefPramHasAssociatedPpmException(HasAssociatedPpm ex) {
        String message = messageSource.getMessage("refparam.hasAssociatedPpm",new Object[] {ex.getId()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.CONFLICT.value(), message);
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(HasAssociatedPaa.class)
    public ResponseEntity<ErrorResponse> RefPramHasAssociatedPaaException(HasAssociatedPaa ex) {
        String message = messageSource.getMessage("refparam.hasAssociatedPaa",new Object[] {ex.getId()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.CONFLICT.value(), message);
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(HasAssociatedAgpm.class)
    public ResponseEntity<ErrorResponse> RefPramHasAssociatedAgpmException(HasAssociatedAgpm ex) {
        String message = messageSource.getMessage("refparam.hasAssociatedAgpm",new Object[] {ex.getId()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.CONFLICT.value(), message);
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(HasAssociatedRefRealisation.class)
    public ResponseEntity<ErrorResponse> RefPramHasAssociatedRefRealisationException(HasAssociatedRefRealisation ex) {
        String message = messageSource.getMessage("refparam.hasAssociatedRealisation",new Object[] {ex.getId()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.CONFLICT.value(), message);
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(AcHasAssociatedAgpm.class)
    public ResponseEntity<ErrorResponse> AcHasAssociatedAgpmException(AcHasAssociatedAgpm ex) {
        String message = messageSource.getMessage("ac.hasAssociatedAgpm",new Object[] {ex.getId()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.CONFLICT.value(), message);
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(AcHasAssociatedPaa.class)
    public ResponseEntity<ErrorResponse> AcHasAssociatedAgpmException(AcHasAssociatedPaa ex) {
        String message = messageSource.getMessage("ac.hasAssociatedPaa",new Object[] {ex.getId()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.CONFLICT.value(), message);
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(AcHasAssociatedPpm.class)
    public ResponseEntity<ErrorResponse> AcHasAssociatedAgpmException(AcHasAssociatedPpm ex) {
        String message = messageSource.getMessage("ac.hasAssociatedPpm",new Object[] {ex.getId()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.CONFLICT.value(), message);
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(AcNomAlreadyExist.class)
    public ResponseEntity<ErrorResponse> AcNomAlreedyExistException(AcNomAlreadyExist ex) {
        String message = messageSource.getMessage("ac.acNomAlreadyExist",new Object[] {ex.getNom()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.CONFLICT.value(), message);
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(AcAttributeRequire.class)
    public ResponseEntity<ErrorResponse> AcAttibuteRequireException(AcAttributeRequire ex) {
        String message = messageSource.getMessage("ac.attributeRequire",new Object[] {ex.getNom()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(DirectionNotFound.class)
    public ResponseEntity<ErrorResponse> DirectionNotFoundException(DirectionNotFound ex) {
        String message = messageSource.getMessage("direction.directionNotFound",new Object[] {ex.getDirectionId(),ex.getAcId()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(DirectionAlreadyExist.class)
    public ResponseEntity<ErrorResponse> DirectionAlreadyExistException(DirectionAlreadyExist ex) {
        String message = messageSource.getMessage("direction.directionAlreadyExist",new Object[] {ex.getLibelle()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(LibelleRequired.class)
    public ResponseEntity<ErrorResponse> LibelleRequiredException(LibelleRequired ex) {
        String message = messageSource.getMessage("direction.libelleRequired",new Object[] {}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(DirectionNotBelongToAc.class)
    public ResponseEntity<ErrorResponse> DirectionNotBelongToAcException(DirectionNotBelongToAc ex) {
        String message = messageSource.getMessage("direction.directionNotBelongToAc",new Object[] {ex.getId(),ex.getAcId()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(DirectionAssociatedWithTwoAcs.class)
    public ResponseEntity<ErrorResponse> DirectionAssociatedWithTwoAcsException(DirectionAssociatedWithTwoAcs ex) {
        String message = messageSource.getMessage("direction.associatedWithTwoAcs",new Object[] {}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.CONFLICT.value(), message);
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(AcAttributeRequiredAndValid.class)
    public ResponseEntity<ErrorResponse> AcAttributeRequiredAndValidException(AcAttributeRequiredAndValid ex) {
        String message = messageSource.getMessage("ac.requiredAndValid",new Object[] {ex.getNom()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(UserComNotFound.class)
    public ResponseEntity<ErrorResponse> UserComNotFoundException(UserComNotFound ex) {
        String message = messageSource.getMessage("com.userComNotFound",new Object[] {ex.getUserId(),ex.getComId()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(UserResponsibleForCom.class)
    public ResponseEntity<ErrorResponse> UserResponsibleException(UserResponsibleForCom ex) {
        String message = messageSource.getMessage("com.userResponsibleException",new Object[] {ex.getUserId(),ex.getComId()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.CONFLICT.value(), message);
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(RefParamHasAssociatedAc.class)
    public ResponseEntity<ErrorResponse> refParamHasAssociatedAcException(RefParamHasAssociatedAc ex) {
        String message = messageSource.getMessage("refparam.hasAssociatedAc",new Object[] {ex.getId()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.CONFLICT.value(), message);
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }
    @ExceptionHandler(AuthorizationDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<ErrorResponse> handleAuthorizationDeniedException(AuthorizationDeniedException ex) {
        ErrorResponse error = new ErrorResponse(HttpStatus.FORBIDDEN.value(), "Accès refusé (autorisation manquante)");
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }
    @ExceptionHandler(AcAccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<ErrorResponse> handleAcAccessDenied(AcAccessDeniedException ex) {
        ErrorResponse error = new ErrorResponse(HttpStatus.FORBIDDEN.value(), ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(MonnaieCodeAlreadyExist.class)
    public ResponseEntity<ErrorResponse> MonnaieCodeAlreadyExistException(MonnaieCodeAlreadyExist ex) {
        String message = messageSource.getMessage("monnaie.codeAlreadyExist",new Object[] {ex.getCode()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.CONFLICT.value(), message);
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ModeDePassationSelectionNotFound.class)
    public ResponseEntity<ErrorResponse> ModeDePassationSelectionNotFoundException(ModeDePassationSelectionNotFound ex) {
        String message = messageSource.getMessage("ModeDePassationSelectionNotFound.notFound", new Object[]{ex.getId()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);

    }
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(DuplicateTypeMarcherIdsException.class)
    public ResponseEntity<ErrorResponse> DuplicateCategorieIdsException(DuplicateTypeMarcherIdsException ex) {
        String message = messageSource.getMessage("duplicate.typeMarcher",new Object[] {String.join((CharSequence) ",", ex.getId().stream().map(String::valueOf).collect(Collectors.toList())) } , getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ModeDePassationSelectionAlredyExist.class)
    public ResponseEntity<ErrorResponse> ModeDePassationSelectionAlredyExistException(ModeDePassationSelectionAlredyExist ex) {
        String message = messageSource.getMessage("ModeDePassationSelection.alredyExist",new Object[] {ex.getLibelle()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(RequiredValue.class)
    public ResponseEntity<ErrorResponse> RequiredLibelleException(RequiredValue ex) {
        String message = messageSource.getMessage("ModeDePassationSelection.requiredlibelle",new Object[] {}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(MonnaieNotFound.class)
    public ResponseEntity<ErrorResponse> MonnaieCodeAlreadyExistException(MonnaieNotFound ex) {
        String message = messageSource.getMessage("monnaie.notfound",new Object[] {ex.getId()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(AdresseNotFound.class)
    public ResponseEntity<ErrorResponse> AdresseNotFoundException(AdresseNotFound ex) {
        String message = messageSource.getMessage("adresse.notfound",new Object[] {ex.getId()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(AcHasAssociatedAc.class)
    public ResponseEntity<ErrorResponse> AcHasAssociatedAcException(AcHasAssociatedAc ex) {
        String message = messageSource.getMessage("ac.hasAssociatedAc",new Object[] {ex.getId()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.CONFLICT.value(), message);
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(HasAssociatedAdress.class)
    public ResponseEntity<ErrorResponse> HasAssociatedAdresseException(HasAssociatedAdress ex) {
        String message = messageSource.getMessage("ref.hasAssociatedAddress",new Object[] {ex.getId()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.CONFLICT.value(), message);
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(AcAlreadyHaveCom.class)
    public ResponseEntity<ErrorResponse> AcAlreadyHaveComException(AcAlreadyHaveCom ex) {
        String message = messageSource.getMessage("ac.hasAlreadyHaveCom",new Object[] {ex.getId()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.CONFLICT.value(), message);
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ReunionNotFound.class)
    public ResponseEntity<ErrorResponse> ReunionNotFoundException(ReunionNotFound ex) {
        String message = messageSource.getMessage("reunion.notfound",new Object[] {ex.getId()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ParticipantNotFound.class)
    public ResponseEntity<ErrorResponse> ParticipantNotFoundException(ParticipantNotFound ex) {
        String message = messageSource.getMessage("participant.notfound",new Object[] {ex.getId()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(PermanentComAlreadyExist.class)
    public ResponseEntity<ErrorResponse> PermanentComAlreadyExistException(PermanentComAlreadyExist ex) {
        String message = messageSource.getMessage("com.permanentComAlreadyExist",new Object[] {ex.getCode()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(RefParamAlredyExist.class)
    public ResponseEntity<ErrorResponse> RefParamAlredyExistExistException(RefParamAlredyExist ex) {
        String message = messageSource.getMessage("refparam.referenceAlreadyExist",new Object[] {ex.getCode()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(CNCMPPNotFound.class)
    public ResponseEntity<ErrorResponse> CNCMPPNotFoundException(CNCMPPNotFound ex) {
        String message = messageSource.getMessage("com.cncpp_notfound",new Object[] {}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ReunionStatusException.class)
    public ResponseEntity<ErrorResponse> ReunionStatusException(ReunionStatusException ex) {
        String message = messageSource.getMessage("reunion.brouillon",new Object[] {ex.getStatus()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ReunionDate.class)
    public ResponseEntity<ErrorResponse> ReunionDateException() {
        String message = messageSource.getMessage("reunion.date",new Object[] {}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(ComHasAssociatedUser.class)
    public ResponseEntity<ErrorResponse> ComHasAssociatedUserException(ComHasAssociatedUser ex) {
        String message = messageSource.getMessage("com.hasAssociatedUser",new Object[] {ex.getId()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.CONFLICT.value(), message);
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(AcHasAssociatedUser.class)
    public ResponseEntity<ErrorResponse> AcHasAssociatedUserException(AcHasAssociatedUser ex) {
        String message = messageSource.getMessage("ac.hasAssociatedUser",new Object[] {ex.getId()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.CONFLICT.value(), message);
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(UserDoesNotHavePermission.class)
    public ResponseEntity<ErrorResponse> UserDoesNotHavePermissionException(UserDoesNotHavePermission ex) {
        String message = messageSource.getMessage("user.doesNotHavePermission",new Object[] {}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), message);
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ProcessVerbalReunionNotFound.class)
    public ResponseEntity<ErrorResponse> ProcessVerbalReunionNotFoundException(ProcessVerbalReunionNotFound ex) {
        String message = messageSource.getMessage("pv.pvnotfound",new Object[] {}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(DuplicationData.class)
    public ResponseEntity<ErrorResponse> DuplicationDataException() {
        String message = messageSource.getMessage("reunion.duplicationData",new Object[] {}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(DacNotFound.class)
    public ResponseEntity<ErrorResponse> DacNotFoundException(DacNotFound ex) {
        String message = messageSource.getMessage("dac.notfound",new Object[] {ex.getDacId(),ex.getDacVersion()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ParametrageProcessDacNotFound.class)
    public ResponseEntity<ErrorResponse> parametrageProcessDacNotFoundException(ParametrageProcessDacNotFound ex) {
        String message = messageSource.getMessage("parametrage.process.dac",new Object[] {ex.getTypeMarcherId(),ex.getModePassationId()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
}

