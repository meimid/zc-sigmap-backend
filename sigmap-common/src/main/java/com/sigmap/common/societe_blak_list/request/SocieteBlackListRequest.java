package com.sigmap.common.societe_blak_list.request;

import com.sigmap.common.config.BlackList;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SocieteBlackListRequest {
    private Integer societeId;
    private BlackList natureDecision;
    private String reference;
    private LocalDateTime dateEffet;
    private LocalDateTime delai;
}