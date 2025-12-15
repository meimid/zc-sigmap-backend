package com.sigmap.sigmap_params.exeptions;

import com.sigmap.common.ModeDePassationSelection.exception.DuplicateTypeMarcherIdsException;
import com.sigmap.common.ModeDePassationSelection.exception.ModeDePassationSelectionAlredyExist;
import com.sigmap.common.ModeDePassationSelection.exception.ModeDePassationSelectionNotFound;
import com.sigmap.common.ModeDePassationSelection.exception.RequiredValue;
import com.sigmap.common.Participant.exception.EmailAlreadyReservedForAotherUser;
import com.sigmap.common.Participant.exception.MultiplePresidentsNotAllowedException;
import com.sigmap.common.Participant.exception.ParticipantNotFound;
import com.sigmap.common.Participant.exception.PresidentFonctionRequiredException;
import com.sigmap.common.ac.exception.*;
import com.sigmap.common.activite.exception.*;
import com.sigmap.common.adresse.exceptions.AdresseNotFound;
import com.sigmap.common.authority.exeption.AuthorityNotFound;
import com.sigmap.common.com.exception.*;
import com.sigmap.common.compte_bancaire.exception.CompteBancaireNotFound;
import com.sigmap.common.contact.exception.ContactDeleted;
import com.sigmap.common.contact.exception.ContactNotFound;
import com.sigmap.common.dac.exeption.DacIdRequired;
import com.sigmap.common.dac.exeption.DacNotFound;
import com.sigmap.common.dac_doc_soumission.exception.DacDocSoumissionAlreadyExist;
import com.sigmap.common.dac_doc_soumission.exception.DacDocSoumissionNotFound;
import com.sigmap.common.dac_doc_soumission.exception.DocSoumissionIdRequired;
import com.sigmap.common.dac_dyna_form.exceptions.DacDynaFormNotFoundException;
import com.sigmap.common.direction.exception.*;
import com.sigmap.common.doc_soumission.exception.*;
import com.sigmap.common.doc_soumission.exception.DocSoumissionNotFound;
import com.sigmap.common.doc_soumission.exception.InvalidTypeDossierException;
import com.sigmap.common.doc_soumission.exception.NomRequired;
import com.sigmap.common.doc_soumission.exception.TypeDossierRequired;
import com.sigmap.common.docs.exeptions.DocDeleted;
import com.sigmap.common.docs.exeptions.DocNotFound;
import com.sigmap.common.dyna_docs_model.exeptions.DynaDocsModelAlreadyUsedExeption;
import com.sigmap.common.dyna_docs_model.exeptions.DynaDocsModelNotFoundExeption;
import com.sigmap.common.dyna_form.exceptions.DynaFormNotFound;
import com.sigmap.common.globalExceptions.BadRequest;
import com.sigmap.common.keycloakAdmin.exception.UserDoesNotHavePermission;
import com.sigmap.common.keycloak_website_admin.exception.CreateUserFailed;
import com.sigmap.common.keycloak_website_admin.exception.EmailOrUserNameAlreadyUsedForAnotherUser;
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
import com.sigmap.common.process_verbal_reunion.exception.*;
import com.sigmap.common.realisations.exceptions.RealisationAlreadyExistsException;
import com.sigmap.common.realisations.exceptions.RealisationNotFoundException;
import com.sigmap.common.realisations.exceptions.RealisationsMinimumRequirement;
import com.sigmap.common.realisations.exceptions.UpdateDeletedRealisation;
import com.sigmap.common.ref_params.exceptions.*;
import com.sigmap.common.ref_types.exceptions.*;
import com.sigmap.common.reunion.exception.*;
import com.sigmap.common.seuil_general.exception.SeuilGeneralNotFound;
import com.sigmap.common.seuil_general.exception.SeuilWithTypeMarcheExiste;
import com.sigmap.common.societe.exception.*;
import com.sigmap.common.societe_contact.exception.SocieteContactAlreadyExist;
import com.sigmap.common.societe_contact.exception.SocieteContactNotFound;
import com.sigmap.common.type_agrement.exception.OrganismeDeDeliveranceAgrementsRequired;
import com.sigmap.common.type_agrement.exception.TypeAgrementAlreadyExist;
import com.sigmap.common.type_agrement.exception.TypeAgrementNotFound;
import com.sigmap.common.type_agrement.exception.WrongType;
import com.sigmap.common.user_com.exception.UserComNotFound;
import com.sigmap.common.user_com.exception.UserResponsibleForCom;
import com.sigmap.common.user_commission_historique.exception.*;
import com.sigmap.common.user_societe.exception.*;
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
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(AcNotFound.class)
    public ResponseEntity<ErrorResponse> handleAcNotFoundException(AcNotFound ex) {
        String message = messageSource.getMessage("ac.notfound",new Object[] {String.join((CharSequence) ",", ex.getId().stream().map(String::valueOf).collect(Collectors.toList())) } , getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(RefParamNotFound.class)
    public ResponseEntity<ErrorResponse> handleRefParamNotFoundException(RefParamNotFound ex) {
        String message = messageSource.getMessage("refparam.notfound",new Object[] {String.join((CharSequence) ",", ex.getId().stream().map(String::valueOf).collect(Collectors.toList())) } , getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ReferenceNotFound.class)
    public ResponseEntity<ErrorResponse> referenceNotFoundException(ReferenceNotFound ex) {
        String message = messageSource.getMessage("reference.notfound",new Object[] {String.join((CharSequence) ",", ex.getReference().stream().map(String::valueOf).collect(Collectors.toList())) } , getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(RefTypeNotFound.class)
    public ResponseEntity<ErrorResponse> refTypeNotFoundException(RefTypeNotFound ex) {
        String message = messageSource.getMessage("reftype.notfound",new Object[] {ex.getCode()} , getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(RefTypeAlreadyExist.class)
    public ResponseEntity<ErrorResponse> refTypeAlreadyExistException(RefTypeAlreadyExist ex) {
        String message = messageSource.getMessage("reftype.alreadyexists",new Object[] {ex.getId()} , getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(RefTypePositiveIdAndNotEmpty.class)
    public ResponseEntity<ErrorResponse> refTypePositiveIdAndNotEmptyException(RefTypePositiveIdAndNotEmpty ex) {
        String message = messageSource.getMessage("reftype.positive.and.not.empty",new Object[] {} , getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ComNotFound.class)
    public ResponseEntity<ErrorResponse> handleComNotFoundException(ComNotFound ex) {

        String message = messageSource.getMessage("com.notfound", new Object[] {String.join((CharSequence) ",", ex.getId().stream().map(String::valueOf).collect(Collectors.toList())) } , getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(DuplicateComIdsException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateComIdsException(DuplicateComIdsException ex) {
        String message = messageSource.getMessage("duplicate.com",new Object[] {String.join((CharSequence) ",", ex.getId().stream().map(String::valueOf).collect(Collectors.toList())) } , getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(DuplicateAcIdsException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateAcIdsException(DuplicateAcIdsException ex) {
        String message = messageSource.getMessage("duplicate.ac",new Object[] {String.join((CharSequence) ",", ex.getId().stream().map(String::valueOf).collect(Collectors.toList())) } , getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
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

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(CompteBancaireNotFound.class)
    public ResponseEntity<ErrorResponse> compteBancaireNotFoundException(CompteBancaireNotFound ex) {
        String message = messageSource.getMessage("compte.bancaire.notfound",new Object[] {String.join((CharSequence) ",", ex.getId().stream().map(String::valueOf).collect(Collectors.toList())) } , getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(SocieteNotFound.class)
    public ResponseEntity<ErrorResponse> societeNotFoundException(SocieteNotFound ex) {
        String message = messageSource.getMessage("societe.notfound",new Object[] {String.join((CharSequence) ",", ex.getId().stream().map(String::valueOf).collect(Collectors.toList())) } , getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ContactNotFound.class)
    public ResponseEntity<ErrorResponse> ContactNotFoundException(ContactNotFound ex) {
        String message = messageSource.getMessage("contact.notfound",new Object[] {String.join((CharSequence) ",", ex.getId().stream().map(String::valueOf).collect(Collectors.toList())) } , getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MenuItemNotFound.class)
    public ResponseEntity<ErrorResponse> menuItemNotFoundException(MenuItemNotFound ex) {
        String message = messageSource.getMessage("menuitem.notfound",new Object[] {String.join((CharSequence) ",", ex.getId().stream().map(String::valueOf).collect(Collectors.toList())) } , getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MenuItemSoftDeleteExeption.class)
    public ResponseEntity<ErrorResponse> menuItemSoftDeleteExeption(MenuItemSoftDeleteExeption ex) {
        String message = messageSource.getMessage("menuitem.softdelete",new Object[] {ex.getNumber(),ex.getId()} , getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MenuItemAlreadyExistsExeption.class)
    public ResponseEntity<ErrorResponse> authorityNotFoundException(MenuItemAlreadyExistsExeption ex) {
        String message = messageSource.getMessage("menuitem.alreadyexists",new Object[] {String.join((CharSequence) ",", ex.getId().stream().map(String::valueOf).collect(Collectors.toList())) } , getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MenuItemUpdateExeption.class)
    public ResponseEntity<ErrorResponse> authorityNotFoundException(MenuItemUpdateExeption ex) {
        String message = messageSource.getMessage("menuitem.update.error",new Object[] {ex.getId() } , getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(AuthorityNotFound.class)
    public ResponseEntity<ErrorResponse> authorityNotFoundException(AuthorityNotFound ex) {
        String message = messageSource.getMessage("authority.notfound",new Object[] {String.join((CharSequence) ",", ex.getAuthority().stream().map(String::valueOf).collect(Collectors.toList())) } , getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(LotNotFound.class)
    public ResponseEntity<ErrorResponse> LotNotFoundException(LotNotFound ex) {
        String message = messageSource.getMessage("lot.notfound",new Object[] {
                String.join((CharSequence) ",", ex.getId().stream().map(String::valueOf).collect(Collectors.toList())),
                String.join((CharSequence) ",", ex.getVersion().stream().map(String::valueOf).collect(Collectors.toList())) },
                getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(RealisationNotFoundException.class)
    public ResponseEntity<ErrorResponse> realisationNotFoundException(RealisationNotFoundException ex) {
        String message = messageSource.getMessage("realisation.notfound",new Object[] {ex.getId(),ex.getVersion()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(RealisationAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> realisationAlreadyExistsException(RealisationAlreadyExistsException ex) {
        String message = messageSource.getMessage("realisation.alreadyexists",new Object[] {ex.getId(),ex.getVersion()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(RealisationsMinimumRequirement.class)
    public ResponseEntity<ErrorResponse> realisationsMinimumRequirementException(RealisationsMinimumRequirement ex) {
        String message = messageSource.getMessage("realisation.minimum.requirement",new Object[] {}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(UpdateDeletedRealisation.class)
    public ResponseEntity<ErrorResponse> updateDeletedRealisationException(UpdateDeletedRealisation ex) {
        String message = messageSource.getMessage("realisation.minimum.requirement",new Object[] {}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(UpdateDeletedLot.class)
    public ResponseEntity<ErrorResponse> updateDeletedLotException(UpdateDeletedLot ex) {
        String message = messageSource.getMessage("lot.update.is.deleted",new Object[] {
                        String.join((CharSequence) ",", ex.getId().stream().map(String::valueOf).collect(Collectors.toList())),
                        String.join((CharSequence) ",", ex.getVersion().stream().map(String::valueOf).collect(Collectors.toList())) },
                        getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(PaaNotFound.class)
    public ResponseEntity<ErrorResponse> paaNotFoundException(PaaNotFound ex) {
        String message = messageSource.getMessage("paa.notfound",new Object[] {
                        String.join((CharSequence) ",", ex.getId().stream().map(String::valueOf).collect(Collectors.toList())),
                        String.join((CharSequence) ",", ex.getVersion().stream().map(String::valueOf).collect(Collectors.toList())) },
                        getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(PpmNotFound.class)
    public ResponseEntity<ErrorResponse> ppmNotFoundException(PpmNotFound ex) {
        String message = messageSource.getMessage("ppm.notfound",new Object[] {
                        String.join((CharSequence) ",", ex.getId().stream().map(String::valueOf).collect(Collectors.toList())),
                        String.join((CharSequence) ",", ex.getVersion().stream().map(String::valueOf).collect(Collectors.toList())) },
                getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
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

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(SocieteContactNotFound.class)
    public ResponseEntity<ErrorResponse> SocieteContactNotFoundException(SocieteContactNotFound ex) {
        String message = messageSource.getMessage("societe.societeContactNotFound", new Object[]{ex.getSocieteId(), ex.getContactId()}, getLocal());

        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
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
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(SocieteContactAlreadyExist.class)
    public ResponseEntity<ErrorResponse> societeContactAlreadyExistException(SocieteContactAlreadyExist ex) {
        String message = messageSource.getMessage("societe.societeContactAlreadyExist",new Object[] {ex.getSocieteId(), ex.getContactId()},getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
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
    @ExceptionHandler(AttributeRequire.class)
    public ResponseEntity<ErrorResponse> AcAttibuteRequireException(AttributeRequire ex) {
        String message = messageSource.getMessage("ac.attributeRequire",new Object[] {ex.getNom()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(DirectionNotFound.class)
    public ResponseEntity<ErrorResponse> DirectionNotFoundException(DirectionNotFound ex) {
        String message = messageSource.getMessage("direction.directionNotFound",new Object[] {ex.getDirectionId(),ex.getAcId()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
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

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(UserComNotFound.class)
    public ResponseEntity<ErrorResponse> UserComNotFoundException(UserComNotFound ex) {
        String message = messageSource.getMessage("com.userComNotFound",new Object[] {ex.getUserId(),ex.getComId()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
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

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ModeDePassationSelectionNotFound.class)
    public ResponseEntity<ErrorResponse> ModeDePassationSelectionNotFoundException(ModeDePassationSelectionNotFound ex) {
        String message = messageSource.getMessage("ModeDePassationSelectionNotFound.notFound", new Object[]{ex.getId()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);

    }
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(DuplicateTypeMarcherIdsException.class)
    public ResponseEntity<ErrorResponse> DuplicateCategorieIdsException(DuplicateTypeMarcherIdsException ex) {
        String message = messageSource.getMessage("duplicate.typeMarcher",new Object[] {String.join((CharSequence) ",", ex.getId().stream().map(String::valueOf).collect(Collectors.toList())) } , getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
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
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MonnaieNotFound.class)
    public ResponseEntity<ErrorResponse> MonnaieCodeAlreadyExistException(MonnaieNotFound ex) {
        String message = messageSource.getMessage("monnaie.notfound",new Object[] {ex.getId()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(AdresseNotFound.class)
    public ResponseEntity<ErrorResponse> AdresseNotFoundException(AdresseNotFound ex) {
        String message = messageSource.getMessage("adresse.notfound",new Object[] {ex.getId()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
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

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ReunionNotFound.class)
    public ResponseEntity<ErrorResponse> ReunionNotFoundException(ReunionNotFound ex) {
        String message = messageSource.getMessage("reunion.notfound",new Object[] {ex.getId()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ParticipantNotFound.class)
    public ResponseEntity<ErrorResponse> ParticipantNotFoundException(ParticipantNotFound ex) {
        String message = messageSource.getMessage("participant.notfound",new Object[] {ex.getId()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
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
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(CNCMPPNotFound.class)
    public ResponseEntity<ErrorResponse> CNCMPPNotFoundException(CNCMPPNotFound ex) {
        String message = messageSource.getMessage("com.cncpp_notfound",new Object[] {}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ReunionStatusException.class)
    public ResponseEntity<ErrorResponse> ReunionStatusException(ReunionStatusException ex) {
        String message = messageSource.getMessage("reunion.brouillon",new Object[] {ex.getStatus()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(DateBefore.class)
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

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ProcessVerbalReunionNotFound.class)
    public ResponseEntity<ErrorResponse> ProcessVerbalReunionNotFoundException(ProcessVerbalReunionNotFound ex) {
        String message = messageSource.getMessage("pv.pvnotfound",new Object[] {}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(DuplicationData.class)
    public ResponseEntity<ErrorResponse> DuplicationDataException() {
        String message = messageSource.getMessage("reunion.duplicationData",new Object[] {}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(DacNotFound.class)
    public ResponseEntity<ErrorResponse> DacNotFoundException(DacNotFound ex) {
        String message = messageSource.getMessage("dac.notfound",new Object[] {ex.getDacId(),ex.getDacVersion()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ParametrageProcessDacNotFound.class)
    public ResponseEntity<ErrorResponse> parametrageProcessDacNotFoundException(ParametrageProcessDacNotFound ex) {
        String message = messageSource.getMessage("parametrage.process.dac",new Object[] {ex.getTypeMarcherId(),ex.getModePassationId()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(ParticipantPresenceConflict.class)
    public ResponseEntity<ErrorResponse> ParticipantPresenceConflictException() {
        String message = messageSource.getMessage("reunion.participants_absense",new Object[] {}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.CONFLICT.value(), message);
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ActiviteNotFound.class)
    public ResponseEntity<ErrorResponse> ActiviteNotFoundException(ActiviteNotFound ex) {
        String message = messageSource.getMessage("activite.notFound", new Object[]{ex.getId()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);

    }
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(DuplicateCategoriesActivitesException.class)
    public ResponseEntity<ErrorResponse> DuplicateCategorieActivitesIdsException(DuplicateCategoriesActivitesException ex) {
        String message = messageSource.getMessage("activite.duplicate",new Object[] {String.join((CharSequence) ",", ex.getId().stream().map(String::valueOf).collect(Collectors.toList())) } , getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ActiviteAlreadyExist.class)
    public ResponseEntity<ErrorResponse> ActiviteAlreadyExistException(ActiviteAlreadyExist ex) {
        String message = messageSource.getMessage("activite.alreadyExist",new Object[] {ex.getLibelle()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(RequiredLibelle.class)
    public ResponseEntity<ErrorResponse> RequiredLibelleActiviteException(RequiredLibelle ex) {
        String message = messageSource.getMessage("activite.requiredlibelle",new Object[] {}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(HasAssociatedActivite.class)
    public ResponseEntity<ErrorResponse> HasAssociatedRefParamsException(HasAssociatedActivite ex) {
        String message = messageSource.getMessage("refparam.hasAssociatedActivite",new Object[] {ex.getId()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.CONFLICT.value(), message);
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(CategoriesActiviteRequired.class)
    public ResponseEntity<ErrorResponse> RequiredCategoriesActiviteException(CategoriesActiviteRequired ex) {
        String message = messageSource.getMessage("activite.categoriesActiviteRequired",new Object[] {}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }








    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(DacIdRequired.class)
    public ResponseEntity<ErrorResponse> DacIdRequiredException() {
        String message = messageSource.getMessage("dac.IdRequired",new Object[] {}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(DocSoumissionIdRequired.class)
    public ResponseEntity<ErrorResponse> DocSoumissionIdRequiredException() {
        String message = messageSource.getMessage("docSoumission.IdRequired",new Object[] {}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(DacDocSoumissionNotFound.class)
    public ResponseEntity<ErrorResponse> DocSoumissionNotFoundException() {
        String message = messageSource.getMessage("dacDocSoumission.notFound", new Object[]{}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);

    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(DacDocSoumissionAlreadyExist.class)
    public ResponseEntity<ErrorResponse> DacDocSoumissionAlreadyExistException(DacDocSoumissionAlreadyExist ex) {
        String message = messageSource.getMessage("dacDocSoumission.alreadyExist",new Object[] {ex.getDacId(),ex.getDacVersion(),ex.getDocSoumissionId()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(TypeAgrementNotFound.class)
    public ResponseEntity<ErrorResponse> TypeAgrementNotFoundFoundException(TypeAgrementNotFound ex) {
        String message = messageSource.getMessage("typeAgrement.notFound", new Object[]{ex.getId()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);

    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(TypeAgrementAlreadyExist.class)
    public ResponseEntity<ErrorResponse> TypeAgrementAlreadyExistException(TypeAgrementAlreadyExist ex) {
        String message = messageSource.getMessage("typeAgrement.alreadyExist",new Object[] {ex.getLibelle()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(OrganismeDeDeliveranceAgrementsRequired.class)
    public ResponseEntity<ErrorResponse> RequiredOrganismeDeDeliveranceAgrementsRequiredException(OrganismeDeDeliveranceAgrementsRequired ex) {
        String message = messageSource.getMessage("typeAgrement.organismeDeDeliveranceAgrementsRequired",new Object[] {}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(RefTypeIdNotFound.class)
    public ResponseEntity<ErrorResponse> RefTypeNotFoundException(RefTypeIdNotFound ex) {
        String message = messageSource.getMessage("refType.notFound", new Object[]{ex.getId()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);

    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(WrongType.class)
    public ResponseEntity<ErrorResponse> WrongTypeException(WrongType ex) {
        String message = messageSource.getMessage("refType.WrongType", new Object[]{}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);

    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(HasDacDocSoumission.class)
    public ResponseEntity<ErrorResponse> HasDacDocSoumissionException() {
        String message = messageSource.getMessage("docSoumission.hasDacDocSoumission",new Object[] {}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.CONFLICT.value(), message);
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(DocSoumissionNotFound.class)
    public ResponseEntity<ErrorResponse> DocSoumissionNotFoundException(DocSoumissionNotFound ex) {
        String message = messageSource.getMessage("docSoumission.notFound", new Object[]{ex.getId()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);

    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(NomRequired.class)
    public ResponseEntity<ErrorResponse> RequiredNomDocSoumissionException() {
        String message = messageSource.getMessage("docSoumission.nomRequired",new Object[] {}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(TypeDossierRequired.class)
    public ResponseEntity<ErrorResponse> TypeDossierRequiredDocSoumissionException() {
        String message = messageSource.getMessage("docSoumission.typeDossierRequired",new Object[] {}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidTypeDossierException.class)
    public ResponseEntity<ErrorResponse> TypeDossierRequiredDocSoumissionException(InvalidTypeDossierException ex) {
        String message = messageSource.getMessage("docSoumission.invalidTypeDossierException",new Object[] {}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(DacDynaFormNotFoundException.class)
    public ResponseEntity<ErrorResponse> DacDynaFormNotFoundException(DacDynaFormNotFoundException ex) {
        String message = messageSource.getMessage(
                "dacDynaForm.notFound",
                new Object[]{ex.getMessage()},
                getLocal()
        );
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }


    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(HasAssociatedSociete.class)
    public ResponseEntity<ErrorResponse> hasAssociatedSocieteException() {
        String message = messageSource.getMessage("activite.hasAssociatedSociete",new Object[] {}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.CONFLICT.value(), message);
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
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

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(DynaDocsModelNotFoundExeption.class)
    public ResponseEntity<ErrorResponse> DynaDocsModelNotFoundExeptionException(DynaDocsModelNotFoundExeption ex) {
        String message = messageSource.getMessage("dynaDocsModel.notFound",new Object[] {ex.getDynaDocsModelId()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ResponseStatus(HttpStatus.IM_USED)
    @ExceptionHandler(DynaDocsModelAlreadyUsedExeption.class)
    public ResponseEntity<ErrorResponse> DynaDocsModelAlreadyUsedExeptionException(DynaDocsModelAlreadyUsedExeption ex) {
        String message = messageSource.getMessage("dynaDocsModel.alreadyUsed",new Object[] {ex.getDynaDocsModelId()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.IM_USED.value(), message);
        return new ResponseEntity<>(error, HttpStatus.IM_USED);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(DynaFormNotFound.class)
    public ResponseEntity<ErrorResponse> DynaFormNotFoundException(DynaFormNotFound ex) {
        String message = messageSource.getMessage(
                "dynaForm.notfound",
                new Object[]{
                        String.join(",", ex.getId().stream().map(String::valueOf).toList()),
                        String.join(",", ex.getVersion().stream().map(String::valueOf).toList())
                },
                getLocal()
        );
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }


    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ResponsableRequired.class)
    public ResponseEntity<ErrorResponse> handleResponsableRequired(ResponsableRequired ex) {
        String message = messageSource.getMessage(
                "responsable.required",
                null,
                getLocal()
        );
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(UserNotFound.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFound ex) {
        String message = messageSource.getMessage(
                "user.notfound",
                new Object[]{ex.getUserId()},
                getLocal()
        );

        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }


    /** Handle when creation fails */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(CreateUserComHitoriqueException.class)
    public ResponseEntity<ErrorResponse> handleCreateUserComHistoriqueException() {
        String message = messageSource.getMessage("usercommissionhistorique.create.error",
                new Object[]{}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /** Handle when commission id is missing */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ComIdRequired.class)
    public ResponseEntity<ErrorResponse> handleComIdRequiredException(ComIdRequired ex) {
        String message = messageSource.getMessage("usercommissionhistorique.comid.required",
                null, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /** Handle when historique not found */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(UserCommissionHistoriqueNotFound.class)
    public ResponseEntity<ErrorResponse> handleUserCommissionHistoriqueNotFound(UserCommissionHistoriqueNotFound ex) {
        String message = messageSource.getMessage("usercommissionhistorique.notfound",
                new Object[]{ex.getId()}, getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(SwitchStatueException.class)
    public ResponseEntity<ErrorResponse> handleSwitchStatueException(SwitchStatueException ex) {
        String message = messageSource.getMessage(
                "usercommissionhistorique.switchstatus.error",
                new Object[]{ex.getUserId()},
                getLocal()
        );
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);}
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ActiveResponsableRequired.class)
    public ResponseEntity<ErrorResponse> handleActiveResponsableRequired() {
        String message = messageSource.getMessage(
                "responsable.activeRequired",
                new Object[]{},
                getLocal()
        );

        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(EmailOrUserNameAlreadyUsedForAnotherUser.class)
    public ResponseEntity<ErrorResponse> handleEmailOrUserNameAlreadyUsedForAnotherUser() {
        String message = messageSource.getMessage(
                "email.username.already.used",
                new Object[]{},
                getLocal()
        );
        ErrorResponse error = new ErrorResponse(HttpStatus.CONFLICT.value(), message);
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(CreateUserFailed.class)
    public ResponseEntity<ErrorResponse> handleCreateUserFailed() {

        String message = messageSource.getMessage(
                "user.create.failed",
                new Object[]{},
                getLocal()
        );

        ErrorResponse error = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), message);
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(UserNotFoundInKeycloak.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundInKeycloak(UserNotFoundInKeycloak ex) {
        String message = messageSource.getMessage(
                "user.notFoundInKeycloak",
                new Object[]{ex.getUserId()},
                getLocal()
        );

        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(KeycloakUserError.class)
    public ResponseEntity<ErrorResponse> handleKeycloakUserError(KeycloakUserError ex) {
        String message = messageSource.getMessage(
                "keycloak.user.error",
                new Object[]{ex.getDetails()},
                getLocal()
        );

        ErrorResponse error = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), message);
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(SocieteNifAlreadyRelatedToOtherSociete.class)
    public ResponseEntity<ErrorResponse> handleSocieteNifAlreadyRelatedToOtherSociete(SocieteNifAlreadyRelatedToOtherSociete ex) {
        String message = messageSource.getMessage(
                "societe.nifAlreadyExists",
                new Object[]{ex.getNif()},
                getLocal()
        );

        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(SocieteNumCnssAlreadyRelatedToOtherSociete.class)
    public ResponseEntity<ErrorResponse> handleSocieteNumCnssAlreadyRelatedToOtherSociete(
            SocieteNumCnssAlreadyRelatedToOtherSociete ex) {

        String message = messageSource.getMessage(
                "societe.numCnssAlreadyExists",
                new Object[]{ex.getNumCnss()},
                getLocal()
        );

        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(SocieteEmailAlreadyRelatedToOtherSociete.class)
    public ResponseEntity<ErrorResponse> handleSocieteEmailAlreadyRelatedToOtherSociete(
            SocieteEmailAlreadyRelatedToOtherSociete ex) {

        String message = messageSource.getMessage(
                "societe.emailAlreadyExists",
                new Object[]{ex.getEmail()},
                getLocal()
        );

        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(FutureDateNotAllowedException.class)
    public ResponseEntity<ErrorResponse> handleFutureDateNotAllowed(FutureDateNotAllowedException ex) {
        String message = messageSource.getMessage(
                "future.date.not.allowed",
                new Object[]{},
                getLocal()
        );
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(PresidentFonctionRequiredException.class)
    public ResponseEntity<ErrorResponse> handlePresidentFonctionRequired() {

        // fetch translated message from messages.properties
        String message = messageSource.getMessage(
                "participant.function.president.required",
                null,
                getLocal()
        );

        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MultiplePresidentsNotAllowedException.class)
    public ResponseEntity<ErrorResponse> handleMultiplePresidents() {
        String message = messageSource.getMessage(
                "reunion.president.only.one",
                null,
                getLocal()
        );
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message));
    }
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ParticipantPresidentPresenceRequired.class)
    public ResponseEntity<ErrorResponse> ParticipantPresidentPresenceRequiredException () {
        String message = messageSource.getMessage(
                "participant.president.required",
                new Object[] {},
                getLocal()
        );

        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                message
        );

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(NoteNotAllowedException.class)
    public ResponseEntity<ErrorResponse> handleNoteOnlyWhenMeetingEnCoursException() {

        String message = messageSource.getMessage(
                "note.only.encours",
                new Object[] {},
                getLocal()
        );

        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                message
        );

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(EmailAlreadyReservedForAotherUser.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyReservedForAnotherUser(EmailAlreadyReservedForAotherUser ex) {
        String message = messageSource.getMessage(
                "email.already.reserved.for.another.user",
                new Object[]{ex.getEmail()},
                getLocal()
        );

        ErrorResponse error = new ErrorResponse(HttpStatus.CONFLICT.value(), message);
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ExtraitFileRequiredException.class)
    public ResponseEntity<ErrorResponse> handleExtraitFileRequired(ExtraitFileRequiredException ex) {
        String message = messageSource.getMessage(
                "reunion.extrait.file.required",
                new Object[]{ex.getReunionId()},
                getLocal()
        );
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ReunionComTypeInvalidException.class)
    public ResponseEntity<ErrorResponse> handleComTypeInvalid(ReunionComTypeInvalidException ex) {
        String message = messageSource.getMessage(
                "reunion.comtype.invalid",
                new Object[]{ex.getReunionId(), ex.getComTypeCode()},
                getLocal()
        );
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ReunionPvMissingException.class)
    public ResponseEntity<ErrorResponse> handlePvMissing(ReunionPvMissingException ex) {
        String message = messageSource.getMessage(
                "reunion.pv.missing",
                new Object[]{ex.getReunionId()},
                getLocal()
        );
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ReunionPpmNotFoundOrNoDecisionException.class)
    public ResponseEntity<ErrorResponse> handlePpmNotFoundOrNoDecision(ReunionPpmNotFoundOrNoDecisionException ex) {
        String message = messageSource.getMessage(
                "reunion.ppm.notInReunionOrNoDecision",
                new Object[]{ex.getReunionId(), ex.getPpmId()},
                getLocal()
        );
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ReunionPpmNoAcException.class)
    public ResponseEntity<ErrorResponse> handlePpmNoAc(ReunionPpmNoAcException ex) {
        String message = messageSource.getMessage(
                "reunion.ppm.noAc",
                new Object[]{ex.getReunionId(), ex.getPpmId()},
                getLocal()
        );
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ReunionAcNotEligibleForExtraitException.class)
    public ResponseEntity<ErrorResponse> handleAcNotEligible(ReunionAcNotEligibleForExtraitException ex) {
        String message = messageSource.getMessage(
                "reunion.ac.notEligibleForExtrait",
                new Object[]{ex.getReunionId(), ex.getAcId()},
                getLocal()
        );
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(ReunionExtraitAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleExtraitAlreadyExists(ReunionExtraitAlreadyExistsException ex) {
        String message = messageSource.getMessage(
                "reunion.extrait.alreadyExists",
                new Object[]{ex.getPvId(), ex.getAcId()},
                getLocal()
        );
        ErrorResponse error = new ErrorResponse(HttpStatus.CONFLICT.value(), message);
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }


    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(SeuilGeneralNotFound.class)
    public ResponseEntity<ErrorResponse> handleExtraitAlreadyExists(SeuilGeneralNotFound ex) {
        String message = messageSource.getMessage("seuil.general.not.found",new Object[]{ex.getId()},
                getLocal()
        );
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(SeuilWithTypeMarcheExiste.class)
    public ResponseEntity<ErrorResponse> seuilWithTypeMarcheExists(SeuilWithTypeMarcheExiste ex) {
        String message = messageSource.getMessage("seuil.general.with.typeMarche.existe",new Object[]{ex.getId()},
                getLocal()
        );
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(DocNotFound.class)
    public ResponseEntity<ErrorResponse> handleDocNotFoundException(DocNotFound ex) {
        String message = messageSource.getMessage("doc.notfound",new Object[] {String.join((CharSequence) ",", ex.getId().stream().map(String::valueOf).collect(Collectors.toList())) } , getLocal());
        ErrorResponse error = new ErrorResponse(HttpStatus.NOT_FOUND.value(), message);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
}

