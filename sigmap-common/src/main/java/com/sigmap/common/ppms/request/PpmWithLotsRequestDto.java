package com.sigmap.common.ppms.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sigmap.common.realisations.request.RealisationBaseRequestDto;
import com.sigmap.common.realisations.request.RealisationWithLotsRequestDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for PPM requests that explicitly include Realisation information with Lots.
 * This class extends PpmBaseRequestDto but uses composition rather than inheritance
 * to handle the RealisationWithLotsRequestDto objects.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PpmWithLotsRequestDto extends PpmBaseRequestDto {

    /**
     * List of Realisations with Lots associated with this PPM.
     * This is a transient field not directly mapped to the parent class.
     * The @JsonProperty annotation allows this field to be populated from the JSON "realisations" field
     * while avoiding collision with the parent's realisations field.
     */
    private List<RealisationWithLotsRequestDto> realisationsWithLots;

    /**
     * Document files to be uploaded and associated with the PPM.
     * This field is used when creating or updating PPMs with document uploads.
     */
    private List<MultipartFile> ppmFiles;

    /**
     * Sets realisations with lots and updates the parent class's realisations field.
     *
     * @param realisationsWithLots the realisations with lots to set
     */
    public void setRealisationsWithLots(List<RealisationWithLotsRequestDto> realisationsWithLots) {
        this.realisationsWithLots = realisationsWithLots;

        // Update the parent class's realisations field
        if (realisationsWithLots != null) {
            List<RealisationBaseRequestDto> baseRealisations = new ArrayList<>();
            baseRealisations.addAll(realisationsWithLots);
            super.setRealisations(baseRealisations);
        } else {
            super.setRealisations(null);
        }
    }

    /**
     * This method is used by Jackson to set the realisations field when deserializing JSON.
     * It will correctly handle the case when the realisations array is used instead of realisationsWithLots.
     */
    @JsonProperty("realisations")
    public void setRealisationsFromJson(List<RealisationWithLotsRequestDto> realisations) {
        setRealisationsWithLots(realisations);
    }

    /**
     * Gets the realisations with lots.
     * If not explicitly set, converts from the parent class's realisations field.
     *
     * @return the realisations with lots
     */
    public List<RealisationWithLotsRequestDto> getRealisationsWithLots() {
        if (realisationsWithLots == null && super.getRealisations() != null) {
            // This is a best-effort conversion and may lose lot information
            List<RealisationWithLotsRequestDto> result = new ArrayList<>();
            for (RealisationBaseRequestDto baseDto : super.getRealisations()) {
                if (baseDto instanceof RealisationWithLotsRequestDto) {
                    result.add((RealisationWithLotsRequestDto) baseDto);
                } else {
                    RealisationWithLotsRequestDto withLotsDto = new RealisationWithLotsRequestDto();
                    // Copy all fields from base to withLots
                    withLotsDto.setId(baseDto.getId());
                    withLotsDto.setVersion(baseDto.getVersion());
                    withLotsDto.setRealisationEnvisagees(baseDto.getRealisationEnvisagees());
                    withLotsDto.setService(baseDto.getService());
                    withLotsDto.setDirection(baseDto.getDirection());
                    withLotsDto.setSourceFinancement(baseDto.getSourceFinancement());
//                    withLotsDto.setMonais(baseDto.getMonais());
                    withLotsDto.setMonnaie(baseDto.getMonnaie());
                    withLotsDto.setTypeMarcher(baseDto.getTypeMarcher());
//                    withLotsDto.setModeSelection(baseDto.getModeSelection());
                    withLotsDto.setModePassation(baseDto.getModePassation());
                    withLotsDto.setDatePrevLps(baseDto.getDatePrevLps());
                    withLotsDto.setDatePrevAc(baseDto.getDatePrevAc());
                    withLotsDto.setDatePrevDp(baseDto.getDatePrevDp());
                    withLotsDto.setDatePrevAp(baseDto.getDatePrevAp());
                    withLotsDto.setBudgetEstime(baseDto.getBudgetEstime());
                    withLotsDto.setIsPublished(baseDto.getIsPublished());
                    withLotsDto.setPublishedAt(baseDto.getPublishedAt());
                    withLotsDto.setSort(baseDto.getSort());
                    withLotsDto.setDocIds(baseDto.getDocIds());
                    withLotsDto.setLots(baseDto.getLots());

                    withLotsDto.setRealisationFiles(baseDto.getRealisationFiles());
                    result.add(withLotsDto);
                }
            }
            return result;
        }
        return realisationsWithLots;
    }
} 