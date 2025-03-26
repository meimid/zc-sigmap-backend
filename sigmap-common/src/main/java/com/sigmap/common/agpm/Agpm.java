package com.sigmap.common.agpm;


//CREATE TABLE `agpm`  (
//        `agpm_id` int NOT NULL,
//        `agpm_ver` int NOT NULL,
//        `ref` varchar(100) NULL,
//  `nom` varchar(255) NULL,
//  `contenu` longtext NULL,
//  `ac_id` int NULL,
//  `status_id` int(0) NULL,
//  `created_at` datetime(0) NULL,
//  `created_by` varchar(100) NULL,
//  `updated_at` datetime(0) NULL,
//  `updated_by` varchar(100) NULL,
//  `deleted_at` datetime(0) NULL,
//  `deleted_by` varchar(100) NULL,
//  `is_deleted` tinyint(1) NULL,
//PRIMARY KEY (`agpm_id`, `agpm_ver`)
//);


import com.sigmap.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "agpm")
public class Agpm extends BaseEntity {

    @EmbeddedId
    private AgpmId id;

    private String ref;

    private String nom;

    @Column(columnDefinition = "LONGTEXT")
    private String contenu;

    private Integer status_id;

//    @ManyToOne
//    @JoinColumn(name = "ac_id")
//    private AC ac;
//
//    @ManyToMany
//    @JoinTable(
//            name = "agpm_docs",
//            joinColumns = @JoinColumn(name = "agpm_id"),
//            inverseJoinColumns = @JoinColumn(name = "doc_id")
//    )
//    private Set<Doc> docs = new HashSet<>();
}