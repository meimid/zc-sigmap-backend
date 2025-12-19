package com.sigmap.common.doc_soumission;

import com.sigmap.common.type_dossier.TypeDossier;
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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "type_dossier_id", nullable = false)
    private TypeDossier typeDossier;
}
