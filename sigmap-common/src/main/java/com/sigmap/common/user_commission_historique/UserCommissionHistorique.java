package com.sigmap.common.user_commission_historique;

import com.sigmap.common.com.Com;
import com.sigmap.common.entity.ActiveStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_commission_historique")
public class UserCommissionHistorique {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "user_id")
    private String userId;
    @Column(name = "commission_id")
    private Integer comId;
    @ManyToOne
    @JoinColumn(name = "commission_id", referencedColumnName = "com_id", insertable = false, updatable = false)
    private Com com;
    @Column(name = "date_debut")
    private LocalDateTime dateDebut = LocalDateTime.now() ;
    @Column(name = "date_fin")
    private LocalDateTime dateFin ;
    @Enumerated(EnumType.STRING)
    private ActiveStatus status ;
}
