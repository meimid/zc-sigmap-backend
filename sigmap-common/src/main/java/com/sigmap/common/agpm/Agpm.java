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
@EntityListeners(AuditingEntityListener.class)
@Table(name = "agpm")
public class Agpm extends BaseEntity {

    @Id
    @Column(name = "agpm_id")
    private Integer id; // Primary key part 1: agpm_id

    @Id
    @Column(name = "agpm_ver")
    private Integer version; // Primary key part 2: agpm_ver

    @Column(name = "ref", length = 100)
    private String ref; // Reference field, max length 100

    @Column(name = "nom", length = 255)
    private String nom; // Name field, max length 255

    @Column(name = "contenu", columnDefinition = "LONGTEXT")
    private String contenu; // Content field, stored as LONGTEXT

    @ManyToOne
    @JoinColumn(name = "ac_id")
    private Ac ac; // Many-to-one relationship with Ac entity

    @Column(name = "status_id")
    private Integer statusId; // Status ID field

    @ManyToMany
    @JoinTable(
            name = "agpm_docs", // Join table for ManyToMany relationship
            joinColumns = {
                    @JoinColumn(name = "agpm_id", referencedColumnName = "agpm_id"),
                    @JoinColumn(name = "agpm_ver", referencedColumnName = "agpm_ver")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "doc_id", referencedColumnName = "doc_id")
            }
    )
    @ToString.Exclude
    @JsonManagedReference // Agpm is the "owner" of the relationship
    private List<Docs> docs;
}