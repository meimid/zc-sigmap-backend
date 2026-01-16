package com.sigmap.common.source_financement;

import com.sigmap.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Entity class representing a source of financing.
 *
 * This class extends {@link BaseEntity} and provides a number of
 * getter and setter methods for the fields of this entity.
 * It also includes a constructor, a toString method, and
 * equals and hash code methods.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "source_financement")
public class SourceFinancement extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "source_financement_id")
    private Integer id;

    private String code;

    private String description;

    @Column(name = "bailleur_de_fonds")
    private Boolean bailleurDeFonds = false;
}
