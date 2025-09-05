package com.sigmap.common.agpm;

import com.sigmap.common.ac.Ac;
import com.sigmap.common.docs.Docs;
import com.sigmap.common.docs.Request.DocumentHolder;
import com.sigmap.common.entity.ActiveStatus;
import com.sigmap.common.entity.BaseEntity;
import com.sigmap.common.entity.CompositeId;
import com.sigmap.common.entity.ProcessStatus;
import com.sigmap.common.ref_params.RefParam;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing the Agpm table with a composite key (agpm_id, agpm_ver).
 * Extends BaseEntity for audit fields (createdAt, updatedAt, etc.).
 */
@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@IdClass(CompositeId.class)
@Table(name = "agpm")
public class Agpm extends BaseEntity implements DocumentHolder {

    @Id
    @Column(name = "agpm_id")
    private Integer id;

    @Id
    @Column(name = "agpm_ver")
    private Integer version;

    @Column(name = "ref", length = 100)
    private String ref;

    @Column(name = "nom", length = 255)
    private String nom;

    @Column(name = "contenu", columnDefinition = "LONGTEXT")
    private String contenu;

    @Column(name = "annee", length = 4)
    private String annee;

    @ManyToOne
    @JoinColumn(name = "ac_id")
    private Ac ac;

    @Enumerated(EnumType.STRING)
    private ProcessStatus status;
    @ManyToMany
    @JoinTable(
            name = "agpm_docs",
            joinColumns = {
                    @JoinColumn(name = "agpm_id", referencedColumnName = "agpm_id"),
                    @JoinColumn(name = "agpm_ver", referencedColumnName = "agpm_ver")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "doc_id", referencedColumnName = "doc_id")
            }
    )
    private List<Docs> docs;

    @Column(name = "remarque", columnDefinition = "LONGTEXT")
    private String remarque;

    private String processInstanceId;

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

    @Transient
    private String modelAlfrescoId;
}