package com.sigmap.common.societe_blak_list;

import com.sigmap.common.config.BlackList;
import com.sigmap.common.societe.Societe;
import com.sigmap.common.societe.SocieteService;
import com.sigmap.common.societe.exception.SocieteNotFound;
import com.sigmap.common.societe_blak_list.exception.BlackListNotFound;
import com.sigmap.common.societe_blak_list.exception.DelaiRequired;
import com.sigmap.common.societe_blak_list.exception.InvalidBlackListDecision;
import com.sigmap.common.societe_blak_list.exception.SocieteAlreadyBlackListed;
import com.sigmap.common.societe_blak_list.mappers.SocieteBlackListMapper;
import com.sigmap.common.societe_blak_list.request.SocieteBlackListRequest;
import com.sigmap.common.utils.SequenceGeneratorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class SocieteBlackListServiceImpl implements SocieteBlackListService {

    private final SocieteBlackListRepository repository;
    private final SocieteService societeService;
    private final SequenceGeneratorService sequenceGeneratorService;


    public SocieteBlackListServiceImpl(SocieteBlackListRepository repository, SocieteService societeService, SequenceGeneratorService sequenceGeneratorService) {
        this.repository = repository;
        this.societeService = societeService;
        this.sequenceGeneratorService = sequenceGeneratorService;
    }

    @Override
    public List<SocieteBlackList> findAll() {
        Specification<SocieteBlackList> spec =
                SocieteBlackListSpecifications.deleted(false);
        return repository.findAll(spec);
    }

    @Override
    public List<SocieteBlackList> findAll(Specification<SocieteBlackList> specification) {
        specification= SocieteBlackListSpecifications.deleted(false).and(specification);
        return repository.findAll(specification);
    }

    @Override
    public Page<SocieteBlackList> findAll(Specification<SocieteBlackList> specification, Pageable pageable) {
        specification= SocieteBlackListSpecifications.deleted(false).and(specification);
        return repository.findAll(specification, pageable);
    }

    @Override
    public Optional<SocieteBlackList> findOne(Specification<SocieteBlackList> spec) {
        return repository.findOne(spec);    }

    @Override
    public SocieteBlackList findById(Integer id, Integer societeId) throws BlackListNotFound {
        societeService.findById(societeId);
        Specification<SocieteBlackList> spec = Specification.where(
                SocieteBlackListSpecifications.id(id)
                        .and(SocieteBlackListSpecifications.societeId(societeId))
                        .and(SocieteBlackListSpecifications.deleted(false))
        );
        return findOne(spec).orElseThrow(()->new BlackListNotFound("",id));
    }
    @Override
    public SocieteBlackList findDeletedById(Integer id, Integer societeId) throws BlackListNotFound {
        Specification<SocieteBlackList> spec = Specification.where(
                SocieteBlackListSpecifications.id(id)
                        .and(SocieteBlackListSpecifications.societeId(societeId))
                .and(SocieteBlackListSpecifications.deleted(true))
        );
        return findOne(spec).orElseThrow(()->new BlackListNotFound("",id));
    }

    @Override
    public SocieteBlackList create(Integer societeId,SocieteBlackListRequest request) throws SocieteNotFound, InvalidBlackListDecision {
        if(request.getNatureDecision().equals(BlackList.TEMPORIARE)&&request.getDelai()==null){
            throw new DelaiRequired("");
        }
        if(request.getNatureDecision().equals(BlackList.DEFINTIVE)&&request.getDateEffet()!=null){
            request.setDelai(null);
        }
        Specification<SocieteBlackList> duplicateSpec =
                SocieteBlackListSpecifications.societeId(societeId)
                        .and(SocieteBlackListSpecifications.deleted(false));
        if (findOne(duplicateSpec).isPresent()) {
            throw new SocieteAlreadyBlackListed("");
        }

        Societe societe = societeService.findById(societeId);
        SocieteBlackList societeBlackList= SocieteBlackListMapper.INSTANCE.map(request);
        societeBlackList.setId(sequenceGeneratorService.getNextId("societe_black_list_id_seq"));
        societeBlackList.setSocieteId(societeId);
        societeBlackList.setSociete(societe);
        SocieteBlackList saved= repository.save(societeBlackList);
        return  saved;
    }

    @Override
    public SocieteBlackList update(Integer id, Integer societeId, SocieteBlackListRequest request) throws BlackListNotFound, InvalidBlackListDecision, SocieteNotFound {
        if(request.getNatureDecision().equals(BlackList.TEMPORIARE)&&request.getDelai()==null){
            throw new DelaiRequired("");
        }
        if(request.getNatureDecision().equals(BlackList.DEFINTIVE)&&request.getDateEffet()!=null){
            request.setDelai(null);
        }
        SocieteBlackList societeBlackList= findById(id, societeId);
        SocieteBlackListMapper.INSTANCE.updateFromRequest(request,societeBlackList);
        return  repository.save(societeBlackList);

    }

    @Override
    public void delete(Integer id, Integer societeId) throws BlackListNotFound {
        repository.softDelete(id,societeId);

    }
}