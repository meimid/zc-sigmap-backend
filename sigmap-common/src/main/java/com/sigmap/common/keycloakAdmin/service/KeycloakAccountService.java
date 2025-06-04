package com.sigmap.common.keycloakAdmin.service;

import com.sigmap.common.ac.Ac;
import com.sigmap.common.com.Com;
import com.sigmap.common.keycloakAdmin.dto.UserAcDTO;
import com.sigmap.common.keycloakAdmin.dto.UserComDTO;
import com.sigmap.common.keycloakAdmin.dto.UserDTO;
import com.sigmap.common.user_ac.UserAcRepository;
import com.sigmap.common.user_com.UserComRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;


@Service
public class KeycloakAccountService {
  /*  private final Logger logger = LoggerFactory.getLogger(KeycloakAccountService.class);

    @Autowired
    private KeycloakTokenService tokenService;

    @Value("${keycloak.server-url}")
    private String keycloakBaseUrl;

    @Value("${keycloak.realm}")
    private String realm;

    private final RestTemplate restTemplate = new RestTemplate();*/



}
