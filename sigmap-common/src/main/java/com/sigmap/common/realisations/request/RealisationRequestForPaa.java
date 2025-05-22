package com.sigmap.common.realisations.request;

import lombok.Data;

/**
 * A simplified version of RealisationRequest specifically for creating Realisations
 * within a PAA. This DTO doesn't include ID and version fields since they will be 
 * generated during creation.
 */
@Data
public class RealisationRequestForPaa extends RealisationBaseRequestDto {


} 