package com.sigmap.common.keycloakAdmin.service;

import com.sigmap.common.security.Authentication;
import com.sigmap.common.user_ac.UserAc;
import com.sigmap.common.user_ac.UserAcRepository;
import com.sigmap.common.user_com.UserComRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class CurrentUserService {

    @Autowired
    private UserAcRepository userAcRepository;

    @Autowired
    private UserComRepository userComRepository;

    public List<Integer> getCurrentUserAcIds() {
        return Authentication.getCurrentUserId()
                .map(userId -> userAcRepository.findAcIdsByUserIdAndIsDeletedFalse(userId))
                .orElse(Collections.emptyList());
    }

    public List<UserAc> getCurrentUserAc() {
        return Authentication.getCurrentUserId()
                .map(userId -> userAcRepository.findAcByUserIdAndIsDeletedFalse(userId))
                .orElse(Collections.emptyList());
    }

    public List<Integer> getCurrentUserComIds() {
        return Authentication.getCurrentUserId()
                .map(userId -> userComRepository.findComIdsByUserIdAndIsDeletedFalse(userId))
                .orElse(Collections.emptyList());
    }

}
