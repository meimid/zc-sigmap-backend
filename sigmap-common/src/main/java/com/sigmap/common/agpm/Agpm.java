package com.sigmap.common.agpm;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.sigmap.common.ac.Ac;
import com.sigmap.common.docs.Docs;
import com.sigmap.common.entity.BaseEntity;
import com.sigmap.common.entity.CompositeId;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Where;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

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
//@Where(clause = "is_deleted=0")
public class Agpm extends BaseEntity {

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

    @ManyToOne
    @JoinColumn(name = "ac_id")
    private Ac ac;

    @Column(name = "status_id")
    private Integer statusId;

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
}