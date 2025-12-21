package com.sigmap.sigmap_webmaster.controller;

import com.sigmap.common.dac.response.DacWebsiteResponse;
import com.sigmap.common.societe_dac.SocieteDac;
import com.sigmap.common.societe_dac.SocieteDacService;
import com.sigmap.common.societe_dac.response.SocieteDacResponse;
import com.sigmap.sigmap_webmaster.services.WebMasterService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

}
