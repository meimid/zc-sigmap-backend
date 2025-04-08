package com.sigmap.common.docs;

import com.sigmap.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "docs")
public class Docs extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "doc_id")
    private Integer id;
    private String name;
    private String alfresco_id;

}
