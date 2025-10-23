package com.sigmap.common.dac_prix;

import com.sigmap.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "dac_prix")
@Where(clause = "is_deleted=0")
public class DacPrix extends BaseEntity {

    @EmbeddedId
    private DacPrixId id;

    @Column(name = "prix", precision = 20, scale = 5, nullable = false)
    private BigDecimal prix;

}
