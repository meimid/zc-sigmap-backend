package com.sigmap.common.paas;

import com.sigmap.common.ac.Ac;
import com.sigmap.common.entity.BaseEntity;
import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.realisations.Realisation;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;

@Entity
@Table(name = "paa")
@Data
@EqualsAndHashCode(callSuper = true)
@IdClass(CompositeId.class)
@EntityListeners(AuditingEntityListener.class)
public class Paa extends BaseEntity {
    @Id
    @Column(name = "paa_id")
    private Integer id;

    @Id
    @Column(name = "paa_ver")
    private Integer version;

    @Column(name = "ref", length = 100)
    private String ref;

    @Column(name = "nom", length = 255)
    private String nom;

    @Column(name = "annee", length = 4)
    private String annee;

    @ManyToOne
    @JoinColumn(name = "ac_id")
    private Ac ac;

    @Column(name = "budget_estime", precision = 10, scale = 2)
    private BigDecimal budgetEstime;


    @Column(name = "status_id")
    private Integer statusId;

    @ManyToMany(mappedBy = "paas")
    private List<Realisation> realisations;

    //TODO:add relation with DOC entity


    @PrePersist
    public void onCreate() {
        // Manually populate createdAt, createdBy
        this.setCreatedAt(Calendar.getInstance());

    }

    @PreUpdate
    public void onUpdate() {
        // Manually populate updatedAt, updatedBy
        this.setUpdatedAt(Calendar.getInstance());
    }

}
