package com.sigmap.common.ModeDePassationSelection;

import com.sigmap.common.entity.BaseEntity;
import com.sigmap.common.ref_params.RefParam;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "mode_de_passation_selection")
public class ModeDePassationSelection extends BaseEntity {
    @Id

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mode_de_passation_selection_id")
    private Integer id;
    private String libelle;
    @ManyToMany(fetch = FetchType.LAZY) // Using LAZY fetch is generally recommended for collections
    @JoinTable(
            name = "mode_passation_categorie",
            joinColumns = @JoinColumn(name = "mode_de_passation_selection_id"),
            inverseJoinColumns = @JoinColumn(name = "categorie_id")
    )
    private List<RefParam> typeMarchers;

}
