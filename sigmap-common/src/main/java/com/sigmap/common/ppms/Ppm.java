package com.sigmap.common.ppms;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.sigmap.common.ac.Ac;
import com.sigmap.common.com.Com;
import com.sigmap.common.docs.Docs;
import com.sigmap.common.docs.Request.DocumentHolder;
import com.sigmap.common.entity.BaseEntity;
import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.entity.ProcessStatus;
import com.sigmap.common.process_verbal.ProcessVerbal;
import com.sigmap.common.realisations.Realisation;
import com.sigmap.common.ref_params.RefParam;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @Column(name = "budget_estime", precision = 20, scale = 3)
    private BigDecimal budgetEstime;

    @Enumerated(EnumType.STRING)
    private ProcessStatus status;
//to be delete
    @ManyToOne
    @JoinColumn(name = "mode_passation_id")
    private RefParam modePassation;

    @Column(name = "remarque", columnDefinition = "LONGTEXT")
    private String remarque;

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

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "ppm_com",
            joinColumns = {
                    @JoinColumn(name = "ppm_id", referencedColumnName = "ppm_id"),
                    @JoinColumn(name = "ppm_ver", referencedColumnName = "ppm_ver")
            },
            inverseJoinColumns = @JoinColumn(name = "com_id")
    )
    private List<Com> coms;

    @OneToMany(mappedBy = "ppm", cascade = CascadeType.ALL)
    @JsonManagedReference
    @EqualsAndHashCode.Exclude
    private Set<ProcessVerbal> processVerbalSet = new HashSet<>();

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

    public List<Com> getComs() {
        if (this.coms == null) {
            this.coms = new ArrayList<>();
        }
        return this.coms;
    }
    private String processInstanceId;

    @Transient
    private String modelAlfrescoId;

}
