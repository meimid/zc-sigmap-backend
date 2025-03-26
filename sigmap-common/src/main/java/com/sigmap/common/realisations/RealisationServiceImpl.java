package com.sigmap.common.realisations;

import com.sigmap.common.lots.LotRepository;
import com.sigmap.common.paas.PaaRepository;
import com.sigmap.common.ppms.PpmRepository;
import com.sigmap.common.realisations.request.RealisationRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

public class RealisationServiceImpl implements RealisationService{
    private final RealisationRepository realisationRepository;
    private final PpmRepository ppmRepository;
    private final PaaRepository paaRepository;
    private final LotRepository lotRepository;
//    private final DocsRepository docsRepository;

    public RealisationServiceImpl(
            RealisationRepository realisationRepository,
            PpmRepository ppmRepository,
            PaaRepository paaRepository,
            LotRepository lotRepository
           ) {
        this.realisationRepository = realisationRepository;
        this.ppmRepository = ppmRepository;
        this.paaRepository = paaRepository;
        this.lotRepository = lotRepository;

    }

    @Override
    public List<Realisation> findAll() {
        Specification<Realisation> spec = RealisationSpecifications.deleted(false);
        return realisationRepository.findAll(spec);
    }

    @Override
    public Page<Realisation> findAll(Specification<Realisation> spec, Pageable pageable) {
        return realisationRepository.findAll(spec, pageable);
    }

    @Override
    public Page<Realisation> findAllDeleted(Specification<Realisation> spec, Pageable pageable) {
        spec = RealisationSpecifications.deleted(true);
        return realisationRepository.findAll(spec, pageable);
    }

    @Override
    public Page<Realisation> findAllDeleted(Pageable pageable) {
        Specification<Realisation> spec = RealisationSpecifications.deleted(true);
        return realisationRepository.findAll(spec, pageable);
    }

    @Override
    public Optional<Realisation> findDeletedById(RealisationId id) {
        Specification<Realisation> spec = RealisationSpecifications.id(id).and(RealisationSpecifications.deleted(true));
        return realisationRepository.findOne(spec);
    }

    @Override
    public Optional<Realisation> findOne(Specification<Realisation> specification) {
        return realisationRepository.findOne(specification);
    }

    @Override
    public Optional<Realisation> findById(RealisationId id) {
        return realisationRepository.findById(id);
    }

    @Override
    public Realisation save(Realisation realisation) {
        return realisationRepository.save(realisation);
    }

    @Override
    public Realisation create(RealisationRequest request) {
        Realisation realisation = new Realisation();
        realisation.setRealisationId(request.getRealisationId());
        realisation.setRealisationVer(request.getRealisationVer());
        realisation.setRealisationEnvisagees(request.getRealisationEnvisagees());
        realisation.setServiceId(request.getServiceId());
        realisation.setDirectionId(request.getDirectionId());
        realisation.setSourceFinancementId(request.getSourceFinancementId());
        realisation.setMonaisId(request.getMonaisId());
        realisation.setTypeMarcherId(request.getTypeMarcherId());
        realisation.setModeSelectionId(request.getModeSelectionId());
        realisation.setDatePrevLps(request.getDatePrevLps());
        realisation.setDatePrevAc(request.getDatePrevAc());
        realisation.setDatePrevDp(request.getDatePrevDp());
        realisation.setDatePrevAp(request.getDatePrevAp());
        realisation.setBudgetEstime(request.getBudgetEstime());
        realisation.setIsPublished(request.getIsPublished());
        realisation.setPublishedAt(request.getPublishedAt());
        realisation.setSort(request.getSort());

//        // Handle ppms relationship
//        if (request.getPpmIds() != null && !request.getPpmIds().isEmpty()) {
//            List<Ppm> ppms = new java.util.ArrayList<>();
//            for (PpmId ppmId : request.getPpmIds()) { // Assuming Ppm uses PpmId composite key
//                Ppm ppm = ppmRepository.findById(ppmId)
//                        .orElseThrow(() -> new RuntimeException("Ppm not found: " + ppmId));
//                ppms.add(ppm);
//            }
//            realisation.setPpms(ppms);
//        }
//
//        // Handle paas relationship
//        if (request.getPaaIds() != null && !request.getPaaIds().isEmpty()) {
//            List<Paa> paas = new java.util.ArrayList<>();
//            for (PaaId paaId : request.getPaaIds()) { // Assuming Paa uses PaaId composite key
//                Paa paa = paaRepository.findById(paaId)
//                        .orElseThrow(() -> new RuntimeException("Paa not found: " + paaId));
//                paas.add(paa);
//            }
//            realisation.setPaas(paas);
//        }
//
//        // Handle lots relationship
//        if (request.getLotIds() != null && !request.getLotIds().isEmpty()) {
//            Set<Lot> lots = new HashSet<>();
//            for (LotsId lotId : request.getLotIds()) { // Assuming Lot uses LotsId composite key
//                Lot lot = lotRepository.findById(lotId)
//                        .orElseThrow(() -> new RuntimeException("Lot not found: " + lotId));
//                lots.add(lot);
//            }
//            realisation.setLots(lots);
//        }
//
//        // Handle docs relationship
//        if (request.getDocIds() != null && !request.getDocIds().isEmpty()) {
//            List<Docs> docs = new java.util.ArrayList<>();
//            for (Integer docId : request.getDocIds()) { // Docs uses single Integer ID
//                Docs doc = docsRepository.findById(docId)
//                        .orElseThrow(() -> new RuntimeException("Doc not found: " + docId));
//                docs.add(doc);
//            }
//            realisation.setDocs(docs);
//        }

        return realisationRepository.save(realisation);
    }

    @Override
    public void delete(RealisationId id) {
        realisationRepository.softDelete(id);
    }
}