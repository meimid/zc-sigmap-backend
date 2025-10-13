package com.sigmap.common.type_agrement;

import com.sigmap.common.entity.BaseEntity;
import com.sigmap.common.ref_types.RefType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "type_agrement")
public class TypeAgrement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "type_agrement_id")
    private Integer id;
    private String libelle;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisme_de_delivrance_agrements_id", nullable = false)
    private RefType organismeDeDeliveranceAgrements;
}
