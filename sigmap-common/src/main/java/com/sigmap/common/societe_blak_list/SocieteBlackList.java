package com.sigmap.common.societe_blak_list;

import com.sigmap.common.config.BlackList;
import com.sigmap.common.entity.BaseEntity;
import com.sigmap.common.societe.Societe;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "societe_black_list")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@IdClass(SocieteBlackListId.class)
@ToString(onlyExplicitlyIncluded = true)
public class SocieteBlackList extends BaseEntity {

    @Id
    @Column(name = "balck_list_id")
    private Integer id;

    @Id
    @Column(name = "societe_id")
    private Integer societeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "societe_id", insertable = false, updatable = false)
    private Societe societe;

    @Column(name = "nature_decision")
    @Enumerated(EnumType.STRING)
    private BlackList natureDecision;

    private String reference;

    @Column(name = "date_effet")
    private LocalDateTime dateEffet;

    @Column(name = "delai")
    private LocalDateTime delai;



}
