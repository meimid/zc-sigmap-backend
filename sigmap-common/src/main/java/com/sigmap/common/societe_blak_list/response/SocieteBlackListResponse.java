package com.sigmap.common.societe_blak_list.response;

import com.sigmap.common.config.BlackList;
import com.sigmap.common.response.BaseEntityResponse;
import com.sigmap.common.societe.response.SocieteResponseDto;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SocieteBlackListResponse {

    private Integer id;
    private SocieteResponseDto societe;
    private String nom;
    private String nif;
    private String pays;
    private String reference;
    private BlackList natureDecision;
    private LocalDateTime dateEffet;
    private LocalDateTime delai;
    private BaseEntityResponse metadata;
}
