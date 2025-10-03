package com.sigmap.common.activite;

import com.sigmap.common.entity.BaseEntity;
import com.sigmap.common.ref_params.RefParam;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "activites")
public class Activite extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "activite_id")
    private Integer id;
    private String libelle;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "activites_categories_activites",
            joinColumns = @JoinColumn(name = "activite_id"),
            inverseJoinColumns = @JoinColumn(name = "categories_activite_id")
    )
    private List<RefParam> categoriesActivites;

}
