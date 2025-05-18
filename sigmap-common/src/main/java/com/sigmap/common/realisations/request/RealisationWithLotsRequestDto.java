package com.sigmap.common.realisations.request;

import com.sigmap.common.lots.response.LotResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

/**
 * DTO for Realisation requests that explicitly include Lot information.
 * This class extends RealisationBaseRequestDto but ensures that Lot information
 * is properly included in serialization/deserialization.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RealisationWithLotsRequestDto extends RealisationBaseRequestDto {
    
    /**
     * Set of Lot responses associated with this Realisation.
     * This field overrides the one in the parent class to ensure it's not ignored during JSON processing.
     */
    private Set<LotResponse> lots;

    /**
     * List of files to be uploaded for this Realisation.
     * Explicitly declare this field to ensure it's properly carried through inheritance.
     */
    private List<MultipartFile> realisationFiles;

} 