package com.sigmap.common.docs;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.sigmap.common.agpm.Agpm;
import com.sigmap.common.entity.BaseEntity;
import com.sigmap.common.realisations.Realisation;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "docs")
public class Docs extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "doc_id")
    private Integer id; // Primary key: doc_id
    private String name;
    private String alfresco_id;

    @ManyToMany(mappedBy = "docs")
    private List<Realisation> realisations;

    @ManyToMany(mappedBy = "docs")
    @ToString.Exclude // Exclude agpms from toString to break the cycle
    @JsonBackReference // Docs is the "child" in the relationship
    private List<Agpm> agpms;


}
