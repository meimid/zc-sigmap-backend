package com.sigmap.common.societe_blak_list;

import com.sigmap.common.societe.exception.SocieteNotFound;
import com.sigmap.common.societe_blak_list.exception.BlackListNotFound;
import com.sigmap.common.societe_blak_list.exception.InvalidBlackListDecision;
import com.sigmap.common.societe_blak_list.request.SocieteBlackListRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

public interface SocieteBlackListService {

    List<SocieteBlackList> findAll();

    List<SocieteBlackList> findAll(Specification<SocieteBlackList> specification);

    Page<SocieteBlackList> findAll(Specification<SocieteBlackList> specification, Pageable pageable);

    Optional<SocieteBlackList> findOne(Specification<SocieteBlackList> spec);

    SocieteBlackList findById(Integer id, Integer societeId) throws BlackListNotFound;
    SocieteBlackList findDeletedById(Integer id, Integer societeId) throws BlackListNotFound;


    SocieteBlackList create(SocieteBlackListRequest request)
            throws SocieteNotFound, InvalidBlackListDecision;

    SocieteBlackList update(Integer id,SocieteBlackListRequest request)
            throws BlackListNotFound, InvalidBlackListDecision, SocieteNotFound;

    void delete(Integer id, Integer societeId) throws BlackListNotFound;

}
