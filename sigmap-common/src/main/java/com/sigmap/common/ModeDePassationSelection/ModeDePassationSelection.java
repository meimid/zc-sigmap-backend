package com.sigmap.common.ModeDePassationSelection;

import com.sigmap.common.entity.BaseEntity;
import com.sigmap.common.ref_params.RefParam;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "mode_de_passation_selection")
public class ModeDePassationSelection extends BaseEntity {
    @Id

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mode_de_passation_selection_id")
    private Integer id;
    private String libelle;
    @ManyToOne
    @JoinColumn(name = "categorie_id")
    private RefParam categorie;

}
