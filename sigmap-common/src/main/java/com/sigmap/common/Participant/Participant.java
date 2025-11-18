package com.sigmap.common.Participant;

import com.sigmap.common.config.ParticipantType;
import com.sigmap.common.entity.BaseEntity;
import com.sigmap.common.entity.ParticipantFonction;
import com.sigmap.common.reunion.Reunion;
import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@Table(name = "participant")
@EqualsAndHashCode(callSuper = true)
@IdClass(ParticipantId.class)
@ToString(onlyExplicitlyIncluded = true)
public class Participant extends BaseEntity {

    @Id
    @Column(name = "participant_id")
    private Integer id;
    @Id
    @Column(name = "reunion_id")
    private Integer reunionId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reunion_id", insertable = false, updatable = false)
    private Reunion reunion;
    @Enumerated(EnumType.STRING)
    private ParticipantType type;
    private String nom;
    private String prenom;
    private String organisme;
    private String fonction;
    private String mail;
    @Column(name = "user_id")
    private String userId;
    private boolean present;
    @Enumerated(EnumType.STRING)
    private ParticipantFonction fonctionReunion;
}
