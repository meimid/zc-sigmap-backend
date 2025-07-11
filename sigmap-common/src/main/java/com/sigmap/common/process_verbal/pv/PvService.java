package com.sigmap.common.process_verbal.pv;

import com.sigmap.common.ppms.Ppm;
import com.sigmap.common.ppms.PpmService;
import com.sigmap.common.process_verbal.pv.dto.MembreCommision;
import com.sigmap.common.process_verbal.pv.dto.MembreCommisionDTO;
import com.sigmap.common.pv_docs.PvDocsRepository;
import com.sigmap.common.pv_membre_present.PvMembrePresentRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class PvService {


    private final PvMembrePresentRepository pvMembrePresentRepository;
    private final PpmService ppmService;

    private final PvDocsRepository pvDocsRepository;


    public List<MembreCommision> membreCommision(Integer ppmId, Integer ppmVersion, String processVerbalId) {

        List<MembreCommisionDTO> membreCommisionsDTOList = pvMembrePresentRepository.getAllMembreCommision(ppmId, ppmVersion, processVerbalId);

        List<MembreCommision> membreCommisionsList = new ArrayList<>(membreCommisionsDTOList.stream().map(MembreCommision::new).toList());

        return membreCommisionsList;
    }

    public String getAcName(Integer ppmId, Integer ppmVersion) {

        Ppm ppm = null;
        try {
            ppm = ppmService.findById(ppmId, ppmVersion).orElse(null);
        } catch (Exception e) {
            ppm = null;
        }
        String acNom = (ppm != null && ppm.getAc() != null) ? ppm.getAc().getNom() : null;

        return acNom;

    }

    // returns the list of all documents name by this parametre ppmId, ppmVersion, processVerbalId

    List<String> getPvDocsNames(Integer ppmId, Integer ppmVersion, String processVerbalId) {
        return pvDocsRepository.findDocumentNamesByPpmAndProcessVerbal(ppmId, ppmVersion, processVerbalId);
    }

}
