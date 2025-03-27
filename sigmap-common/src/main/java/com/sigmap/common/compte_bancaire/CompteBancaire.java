package com.sigmap.common.compte_bancaire;

import com.sigmap.common.entity.BaseEntity;
import com.sigmap.common.ref_params.RefParam;
import com.sigmap.common.societe.Societe;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "compte_bancaire")
public class CompteBancaire extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "compte_bancaire_id")
    private Integer id;
    private String nom;
    private String agence;
    private String rib;
    @ManyToOne
    @JoinColumn(name="pays_id")
    private RefParam refParam;
    private boolean local;
    @ManyToOne
    @JoinColumn(name="societe_id")
    private Societe societe;

}
