package com.sigmap.common.societe_blak_list.response;

import com.sigmap.common.config.BlackList;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SocieteBlackListResponseDto {
    private Integer id;
    private Integer societeId;
    private String nom;
    private String nif;
    private String pays;
    private BlackList natureDecision;
    private String reference;
    private LocalDateTime dateEffet;
    private LocalDateTime delai;
}
