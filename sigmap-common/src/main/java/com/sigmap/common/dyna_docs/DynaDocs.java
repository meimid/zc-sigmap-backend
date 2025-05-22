package com.sigmap.common.dyna_docs;

import com.sigmap.common.categorie.Categorie;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "dyna_docs")

public class DynaDocs {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dyna_docs_id")
    private Integer dynaDocsId;
    @Column(name = "name")
    private String name;
    @Column(name = "template_file_name")
    private String templateFileName;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Categorie categorie;
    @Column(name = "exported_type")
    private String exportedType;
    @Column(name = "active")
    private boolean active;
}
