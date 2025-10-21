package com.sigmap.common.dyna_docs_model;

import com.sigmap.common.entity.BaseEntity;
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
@Table(name = "dyna_docs_model")
public class DynaDocsModel extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dyna_docs_model_id")
    private Integer dynaDocsModelId;

    @Column(name = "description")
    private String description;
}
