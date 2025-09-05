package com.sigmap.common.ppms.response;

import com.sigmap.common.realisations.response.RealisationPpmWebsite;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PpmDetailsWebsite {
    PpmWebsiteResponse ppm;
    Page<RealisationPpmWebsite> realisations;
}
