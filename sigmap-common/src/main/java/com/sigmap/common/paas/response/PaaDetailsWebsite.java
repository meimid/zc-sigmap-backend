package com.sigmap.common.paas.response;

import com.sigmap.common.realisations.response.RealisationPaaWebSite;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaaDetailsWebsite {
    private PaaWebsiteResponse paa;
    private Page<RealisationPaaWebSite> realisations;
}
