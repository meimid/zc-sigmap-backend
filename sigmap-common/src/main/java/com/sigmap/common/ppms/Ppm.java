package com.sigmap.common.ppms;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.sigmap.common.ac.Ac;
import com.sigmap.common.docs.Docs;
import com.sigmap.common.docs.Request.DocumentHolder;
import com.sigmap.common.entity.BaseEntity;
import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.realisations.Realisation;
import com.sigmap.common.ref_params.RefParam;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ppm")
@Data
@EqualsAndHashCode(callSuper = true)
@IdClass(CompositeId.class)
@EntityListeners(AuditingEntityListener.class)
public class Ppm  extends BaseEntity implements DocumentHolder {
    @Id
    @Column(name = "ppm_id")
    private Integer id;

    @Id
    @Column(name = "ppm_ver")
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

    @ManyToOne
    @JoinColumn(name = "status_id")
    private RefParam status;

    @ManyToOne
    @JoinColumn(name = "mode_passation_id")
    private RefParam modePassation;

    @ManyToMany(mappedBy = "ppms")
    @JsonBackReference
    private List<Realisation> realisations;

    @ManyToMany
    @JoinTable(
            name = "ppm_docs",
            joinColumns = {
                    @JoinColumn(name = "ppm_id", referencedColumnName = "ppm_id"),
                    @JoinColumn(name = "ppm_ver", referencedColumnName = "ppm_ver")
            },
            inverseJoinColumns = @JoinColumn(name = "doc_id")
    )
    private List<Docs> docs;

    @Override
    public Integer getId() {
        return this.id;
    }

    @Override
    public Integer getVersion() {
        return this.version;
    }

    @Override
    public List<Docs> getDocs() {
        if (this.docs == null) {
            this.docs = new ArrayList<>();
        }
        return this.docs;
    }
    private String processInstanceId;
}
