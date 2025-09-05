package com.sigmap.common.adresse;

import com.sigmap.common.entity.BaseEntity;
import com.sigmap.common.ref_params.RefParam;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "adresse")
public class Adresse extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "adresse_id")
    private Integer id;
    private String tel1;
    private String tel2;
    private String fax;
    private String adresse;
    @Column(name = "comp_adresse")
    private String compAdresse;
    @Column(name = "code_postal")
    private String codePostal;
    private String ville;
    @ManyToOne
    @JoinColumn(name = "pays_id")
    private RefParam pays;
}
