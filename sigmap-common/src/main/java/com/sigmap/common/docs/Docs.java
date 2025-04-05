package com.sigmap.common.docs;

import com.sigmap.common.agpm.Agpm;
import com.sigmap.common.entity.BaseEntity;
import com.sigmap.common.realisations.Realisation;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "docs")
public class Docs extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer doc_id;
    private String name;
    private String alfresco_id;

    @ManyToMany(mappedBy = "docs")
    private List<Realisation> realisations;

    @ManyToMany(mappedBy = "docs")
    private List<Agpm> agpms;
}
