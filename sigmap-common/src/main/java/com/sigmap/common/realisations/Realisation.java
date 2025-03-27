package com.sigmap.common.realisations;

import com.sigmap.common.docs.Docs;
import com.sigmap.common.entity.BaseEntity;
import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.lots.Lot;
import com.sigmap.common.paas.Paa;
import com.sigmap.common.ppms.Ppm;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "realisation")
@Data
@EqualsAndHashCode(callSuper = true)
@IdClass(CompositeId.class)
public class Realisation extends BaseEntity {
    @Id
    @Column(name = "realisation_id")
    private Integer realisationId;

    @Id
    @Column(name = "realisation_ver")
    private Integer realisationVer;

    @Column(name = "realisation_envisagees", length = 100)
    private String realisationEnvisagees;

    @Column(name = "service_id")
    private Integer serviceId;

    @Column(name = "direction_id")
    private Integer directionId;

    @Column(name = "source_financement_id")
    private Integer sourceFinancementId;

    @Column(name = "monais_id")
    private Integer monaisId;

    @Column(name = "type_marcher_id")
    private Integer typeMarcherId;

    @Column(name = "mode_selection_id")
    private Integer modeSelectionId;

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
    private Set<Lot> lots;

    @ManyToMany
    @JoinTable(
            name = "realisation_docs",
            joinColumns = {
                    @JoinColumn(name = "realisation_id", referencedColumnName = "realisation_id"),
                    @JoinColumn(name = "realisation_ver", referencedColumnName = "realisation_ver")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "doc_id", referencedColumnName = "doc_id")
            }
    )
    private List<Docs> docs;

}
