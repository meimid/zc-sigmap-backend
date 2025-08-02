package com.sigmap.common.financement;
import com.sigmap.common.entity.BaseEntity;
import com.sigmap.common.entity.CompositeId;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;


@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "finacement")
public class Financement extends BaseEntity {

    @EmbeddedId
    private FinancementId id;

    @Column(name = "source_de_financement_id")
    private Integer sourceDeFinancementId;

    @Column(name = "budget_estimer")
    private BigDecimal budgetEstimer;
}