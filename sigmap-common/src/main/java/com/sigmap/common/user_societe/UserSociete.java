package com.sigmap.common.user_societe;

import com.sigmap.common.entity.ActiveStatus;
import com.sigmap.common.entity.BaseEntity;
import com.sigmap.common.societe.Societe;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "user_societe")
@IdClass(UserSocieteId.class)
public class UserSociete extends BaseEntity {

    @Id
    @Column(name = "user_id")
    private String userId;

    @Id
    @Column(name = "societe_id")
    private Integer societeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "societe_id", insertable = false, updatable = false)
    private Societe societe;

    private Boolean responsable ;

    private String nom;
    private String prenom;
    private String numCin;

    @Column(name = "date_delivrance_cin")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateDelivranceCin;

    private String tel1;
    private String tel2;
    private String email;

    @Enumerated(EnumType.STRING)
    private ActiveStatus status ;
}
