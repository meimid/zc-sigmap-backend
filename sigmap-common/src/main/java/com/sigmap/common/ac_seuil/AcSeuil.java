package com.sigmap.common.ac_seuil;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
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
@Table(name = "ac_seuil")
public class AcSeuil {

    @EmbeddedId
    private AcSeuilId id;

    @Column(name = "seuil", precision = 10, scale = 3)
    private BigDecimal seuil;
}
