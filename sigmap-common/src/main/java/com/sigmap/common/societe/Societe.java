package com.sigmap.common.societe;

import com.sigmap.common.entity.BaseEntity;
import com.sigmap.common.ref_params.RefParam;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "societe")
public class Societe extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "societe_id")
    private Integer id;
    private String nom;
    @ManyToOne
    @JoinColumn(name = "type_id")
    private RefParam type;
    @ManyToOne
    @JoinColumn (name = "pays_id")
    private RefParam pays;
    private String nni;
    @Column(name = "categorie_soumissionnaire_id")
    private String categorieSoumissionnaireId;

}
