package com.sigmap.common.realisations;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.sigmap.common.ModeDePassationSelection.ModeDePassationSelection;
import com.sigmap.common.config.RealisationStatus;
import com.sigmap.common.docs.Docs;
import com.sigmap.common.docs.Request.DocumentHolder;
import com.sigmap.common.entity.BaseEntity;
import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.financement.Financement;
import com.sigmap.common.lots.Lot;
import com.sigmap.common.monnaie.Monnaie;
import com.sigmap.common.paas.Paa;
import com.sigmap.common.ppms.Ppm;
import com.sigmap.common.ref_params.RefParam;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "realisation")
@Data
@EqualsAndHashCode(callSuper = true)
@IdClass(CompositeId.class)
@ToString(exclude = {"paas","financements"})
public class Realisation extends BaseEntity implements DocumentHolder {
    @Id
    @Column(name = "realisation_id")
    private Integer id;

    @Id
    @Column(name = "realisation_ver")
    private Integer version;

    @Column(name = "realisation_envisagees", length = 255)
    private String realisationEnvisagees;

    @Column(name = "beneficiaire")
    private String beneficiaire;

//    //TODO : to delete
//    @ManyToOne
//    @JoinColumn(name = "source_financement_id")
//    private RefParam sourceFinancement;

    //TODO: to delete
    @ManyToOne
    @JoinColumn(name = "monais_id")
    private RefParam monais;

    //TODO: to delete
    @ManyToOne
    @JoinColumn(name = "monnaie_id")
    private Monnaie monnaie;

    @ManyToOne
    @JoinColumn(name = "mode_passation_id")
    private ModeDePassationSelection modePassation;

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

    //TODO: to delete
    @Column(name = "budget_estime", precision = 20, scale = 3)
    private BigDecimal budgetEstime;

    @Column(name = "seuil_applicable", precision = 20, scale = 3)
    private BigDecimal seuilApplicable;

    @Column(name = "is_published")
    private Boolean isPublished;

    @Column(name = "published_at")
    private Boolean publishedAt;

    @Column(name = "sort")
    private Integer sort;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut")
    private RealisationStatus statut = RealisationStatus.NON_PUBLIE;

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
    private List<Lot> lots;

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

    @OneToMany(mappedBy = "realisation", cascade = CascadeType.ALL)
    @JsonManagedReference("realisation-financements")
    private List<Financement> financements = new ArrayList<>();

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
    @Override
    public int hashCode() {
        return Objects.hash(id, version);
    }

}