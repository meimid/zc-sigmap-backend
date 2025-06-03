package com.sigmap.common.dyna_docs;


import com.sigmap.common.dac.DacId;
import com.sigmap.common.ref_params.RefParam;
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
@IdClass(DynaDocID.class)
public class DynaDocs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dyna_docs_id")
    private Integer dynaDocsId;

    @Id
    @Column(name = "dyna_docs_ver")
    private Integer dynaDocsVer;

    @Column(name = "name")
    private String name;
    @Column(name = "template_file_name")
    private String templateFileName;
    @Column(name = "guide_file_name")
    private String guideFileName;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private RefParam categorieId;

    @Column(name = "dyna_doc_order")
    private Integer dynaDocOrder;
    @Column(name = "status")
    private Integer status;
}
