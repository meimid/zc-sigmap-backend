package com.sigmap.common.doc_soumission;

import com.sigmap.common.config.TypeDossier;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Table(name = "doc_soumission")
@Entity
public class DocSoumission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "doc_soumission_id")
    private Integer id;
    private String nom;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_dossier")
    private TypeDossier typeDossier;
}
