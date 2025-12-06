package com.sigmap.common.seuil_general;

import com.sigmap.common.entity.BaseEntity;
import com.sigmap.common.ref_params.RefParam;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "seuil_general")
public class SeuilGeneral extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seuil_id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "type_marcher_id")
    private RefParam typeMarcher;

    @Column(name = "montant", precision = 10, scale = 3)
    private BigDecimal montant;
}
