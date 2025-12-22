package com.sigmap.sigmap_webmaster.controller;

import com.sigmap.common.dac.response.DacDetailsWebsiteResponse;
import com.sigmap.common.dac.response.DacWebsiteResponse;
import com.sigmap.common.societe.exception.SocieteNotFound;
import com.sigmap.common.societe_dac.SocieteDac;
import com.sigmap.common.societe_dac.SocieteDacService;
import com.sigmap.common.societe_dac.mappers.SocieteDacMapper;
import com.sigmap.common.societe_dac.request.SocieteDacRequestForCreate;
import com.sigmap.common.societe_dac.response.SocieteDacResponse;
import com.sigmap.common.docs.Docs;
import com.sigmap.common.docs.DocsService;
import com.sigmap.common.docs.Request.AlfrescoIdRequest;
import com.sigmap.common.docs.exeptions.DocNotFound;
import com.sigmap.common.docs.exeptions.DocsNotFoundException;
import com.sigmap.sigmap_webmaster.services.WebMasterService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * Controller for Espace Societe related operations.
 */
@RestController
@RequestMapping("/api/espace-societe")
@AllArgsConstructor
@Slf4j
public class EspaceSocieteController {

    private final WebMasterService webMasterService;
    private final SocieteDacService societeDacService;
    private final DocsService docsService;

    /**
     * Get published DACs for the authenticated user based on their associated societies.
     *
     * @param pageable pagination info
     * @param jwt      the authenticated token
     * @return page of DAC responses
     */
    @GetMapping("/my-dacs")
    public ResponseEntity<Page<DacWebsiteResponse>> getMyPublishedDacs(Pageable pageable, @AuthenticationPrincipal Jwt jwt) {
        log.debug("REST request to get published DACs for authenticated user in Espace Societe");

        if (jwt == null) {
            log.warn("Missing JWT authentication principal");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String userId = jwt.getSubject();
        log.debug("Fetching DACs for user: {}", userId);

        Page<DacWebsiteResponse> dacs = webMasterService.findPublishedDacsForUser(userId, pageable);

        return ResponseEntity.ok(dacs);
    }


    @PostMapping("/uploadJustifPayment/{dacId}")
    public ResponseEntity<Void> uploadJustifPayment (@PathVariable Integer dacId, @RequestParam("file") MultipartFile file, @AuthenticationPrincipal Jwt jwt) throws IOException {
        log.debug("REST request to get published DACs for authenticated user in Espace Societe");

        if (jwt == null) {
            log.warn("Missing JWT authentication principal");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String userId = jwt.getSubject();
        log.debug("Fetching DACs for user: {}", userId);

        SocieteDac societeDac =societeDacService.updateForShortListing(dacId, file, userId);

        return ResponseEntity.ok().build();
    }


    /**
     * Get details of a specific published DAC for the authenticated user.
     *
     * @param dacId the ID of the DAC
     * @param jwt   the authenticated token
     * @return the DAC details
     */
    @GetMapping("/my-dacs/{dacId}")
    public ResponseEntity<DacDetailsWebsiteResponse> findMyPublishedDacById(@PathVariable Integer dacId, @AuthenticationPrincipal Jwt jwt) {
        log.debug("REST request to get published DAC details for user: {} and DAC: {}", jwt != null ? jwt.getSubject() : "unknown", dacId);

        if (jwt == null) {
            log.warn("Missing JWT authentication principal");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String userId = jwt.getSubject();
        try {
            DacDetailsWebsiteResponse details = webMasterService.findMyPublishedDacById(dacId, userId);
            return ResponseEntity.ok(details);
        } catch (DocNotFound e) {
            log.warn("DAC not found or not accessible: {}", dacId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

    }

    @PostMapping("/download/dac")
    public ResponseEntity<Resource> downloadDocumentDacShortlist(@RequestBody AlfrescoIdRequest alfrescoId, @AuthenticationPrincipal Jwt jwt) throws IOException, DocNotFound {
        if (jwt == null) {
            log.warn("Missing JWT authentication principal");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String userId = jwt.getSubject();
        Docs docInfo = docsService.findByAlfescoId(alfrescoId.getId())
                .orElseThrow(() -> new DocsNotFoundException(alfrescoId.getId()));

        byte[] fileContent = webMasterService.downloadDocumentDacShortlist(alfrescoId.getId(), userId);
        ByteArrayResource resource = new ByteArrayResource(fileContent);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + docInfo.getName() + "\"");
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        return ResponseEntity.ok().headers(headers)
                .contentLength(fileContent.length)
                .contentType(MediaType.parseMediaType(docInfo.getContentType()))
                .body(resource);
    }

    @PostMapping(value="/createSociteDac",consumes = {"multipart/form-data"})
    @Operation(summary = "Créer une nouvelle société DAC")
    public ResponseEntity<Void> createSocieteDac(@ModelAttribute SocieteDacRequestForCreate request, @AuthenticationPrincipal Jwt jwt) throws SocieteNotFound, IOException {
        log.debug("Création d'une nouvelle société DAC: {}", request);

        if (jwt == null) {
            log.warn("Missing JWT authentication principal");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String userId = jwt.getSubject();
        log.debug("Fetching DACs for user: {}", userId);

        SocieteDac createdSocieteDac = societeDacService.create(request,userId);
        SocieteDacResponse response = SocieteDacMapper.INSTANCE.map(createdSocieteDac);
        return ResponseEntity.ok().build();
    }
}
