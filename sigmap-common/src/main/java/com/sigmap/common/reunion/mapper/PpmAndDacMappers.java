package com.sigmap.common.reunion.mapper;

import com.sigmap.common.ac.exception.AcNotFound;
import com.sigmap.common.dac.Dac;
import com.sigmap.common.dac.DacService;
import com.sigmap.common.reunionDac.request.ReunionDacRequest;
import com.sigmap.common.ppms.Ppm;
import com.sigmap.common.ppms.PpmService;
import com.sigmap.common.ppms.exception.PpmNotFound;
import com.sigmap.common.reunionPpm.request.ReunionPpmRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;
@Component
public class PpmAndDacMappers {
    @Autowired
    private PpmService ppmService;
    @Autowired
    DacService dacService;
    protected Set<Ppm> ppmRequestsToPpms(Set<ReunionPpmRequest> ppms) {
        if (ppms == null) {
            return null;
        }
        return ppms.stream()
                .map(req -> {
                    try {
                        return ppmService.findByIdPpm(req.getId(), req.getVersion()).orElseThrow(/* PpmNotFoundException */);
                    } catch (PpmNotFound e) {
                        throw new RuntimeException(e);
                    } catch (AcNotFound e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toSet());
    }

    protected Set<Dac> dacRequestsToDacs(Set<ReunionDacRequest> dacs) {
        if (dacs == null) {
            return null;
        }
        return dacs.stream()
                .map(req -> dacService.findById(req.getDacId(), req.getDacVer()).orElseThrow(()->new RuntimeException("There is a error of retrieving the dac id: " + req.getDacId())))
                .collect(Collectors.toSet());
    }
}
