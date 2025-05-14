package com.sigmap.common.dyna_docs;

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
    @Column(name = "category_id")
    private Integer categoryId;
}
