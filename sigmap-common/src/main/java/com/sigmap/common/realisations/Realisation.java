package com.sigmap.common.realisations;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.sigmap.common.config.RealisationStatus;
import com.sigmap.common.docs.Docs;
import com.sigmap.common.docs.Request.DocumentHolder;
import com.sigmap.common.entity.BaseEntity;
import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.lots.Lot;
import com.sigmap.common.paas.Paa;
import com.sigmap.common.ppms.Ppm;
import com.sigmap.common.ref_params.RefParam;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "realisation")
@Data
@EqualsAndHashCode(callSuper = true)
@IdClass(CompositeId.class)
public class Realisation extends BaseEntity implements DocumentHolder {
    @Id
    @Column(name = "realisation_id")
    private Integer id;

    @Id
    @Column(name = "realisation_ver")
    private Integer version;

    @Column(name = "realisation_envisagees", length = 100)
    private String realisationEnvisagees;

    @ManyToOne
    @JoinColumn(name = "service_id")
    private RefParam service;

    @ManyToOne
    @JoinColumn(name = "direction_id")
    private RefParam direction;

    @ManyToOne
    @JoinColumn(name = "source_financement_id")
    private RefParam sourceFinancement;

    @ManyToOne
    @JoinColumn(name = "monais_id")
    private RefParam monais;

    @ManyToOne
    @JoinColumn(name = "type_marcher_id")
    private RefParam typeMarcher;

    @ManyToOne
    @JoinColumn(name = "mode_selection_id")
    private RefParam modeSelection;

    @ManyToOne
    @JoinColumn(name = "imputation_budgetaire_id")
    private RefParam imputationBudgetaire;

    @Column(name = "date_prev_lps")
    private LocalDate datePrevLps;

    @Column(name = "date_prev_ac")
    private LocalDate datePrevAc;

    @Column(name = "date_prev_dp")
    private LocalDate datePrevDp;

    @Column(name = "date_prev_ap")
    private LocalDate datePrevAp;

    @Column(name = "budget_estime", precision = 10, scale = 2)
    private BigDecimal budgetEstime;



    @Column(name = "is_published")
    private Boolean isPublished;

    @Column(name = "published_at")
    private Boolean publishedAt;

    @Column(name = "sort")
    private Integer sort;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private RealisationStatus statut= RealisationStatus.NON_PUBLIE;

    @ManyToMany
    @JoinTable(
            name = "ppm_realisation",
            joinColumns = {
                    @JoinColumn(name = "realisation_id", referencedColumnName = "realisation_id"),
                    @JoinColumn(name = "realisation_ver", referencedColumnName = "realisation_ver")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "ppm_id", referencedColumnName = "ppm_id"),
                    @JoinColumn(name = "ppm_ver", referencedColumnName = "ppm_ver")
            }
    )
    @JsonManagedReference
    private List<Ppm> ppms;

    @ManyToMany
    @JoinTable(
            name = "paa_realisation",
            joinColumns = {
                    @JoinColumn(name = "realisation_id", referencedColumnName = "realisation_id"),
                    @JoinColumn(name = "realisation_ver", referencedColumnName = "realisation_ver")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "paa_id", referencedColumnName = "paa_id"),
                    @JoinColumn(name = "paa_ver", referencedColumnName = "paa_ver")
            }
    )
    @JsonManagedReference
    private List<Paa> paas;

    @ManyToMany
    @JoinTable(
            name = "lots_realisation",
            joinColumns = {
                    @JoinColumn(name = "realisation_id", referencedColumnName = "realisation_id"),
                    @JoinColumn(name = "realisation_ver", referencedColumnName = "realisation_ver")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "lots_id", referencedColumnName = "lots_id"),
                    @JoinColumn(name = "lots_ver", referencedColumnName = "lots_ver")
            }
    )
    @JsonManagedReference
    private List<Lot> lots ;

    @ManyToMany
    @JoinTable(
            name = "realisation_docs",
            joinColumns = {
                    @JoinColumn(name = "realisation_id", referencedColumnName = "realisation_id"),
                    @JoinColumn(name = "realisation_ver", referencedColumnName = "realisation_ver")
            },
            inverseJoinColumns = @JoinColumn(name = "doc_id")
    )
    @JsonManagedReference
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
}
