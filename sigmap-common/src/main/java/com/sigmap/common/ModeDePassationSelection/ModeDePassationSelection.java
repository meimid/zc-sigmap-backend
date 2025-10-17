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
@Table(name = "mode_passation")
public class ModeDePassationSelection extends BaseEntity {
    @Id

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mode_passation_id")
    private Integer id;
    private String libelle;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "mode_passation_type_marcher",
            joinColumns = @JoinColumn(name = "mode_passation_id"),
            inverseJoinColumns = @JoinColumn(name = "type_marcher_id")
    )
    private List<RefParam> typeMarchers;
    @Column(name = "is_restraint")
    private boolean isRestraint;
}
