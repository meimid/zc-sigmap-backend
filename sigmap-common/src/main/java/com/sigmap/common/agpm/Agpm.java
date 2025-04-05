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


import com.sigmap.common.ac.Ac;
import com.sigmap.common.docs.Docs;
import com.sigmap.common.entity.BaseEntity;
import com.sigmap.common.entity.CompositeId;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.print.Doc;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@IdClass(CompositeId.class)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "agpm")
public class Agpm extends BaseEntity {

    @Id
    @Column(name = "agpm_id")
    private Integer id;

    @Id
    @Column(name = "agpm_ver")
    private Integer version;

    @Column(name = "ref", length = 100)
    private String ref;

    @Column(name = "nom", length = 255)
    private String nom;

    @Column(name = "contenu", columnDefinition = "LONGTEXT")
    private String contenu;

    @ManyToOne
    @JoinColumn(name = "ac_id")
    private Ac ac;

    @Column(name = "status_id")
    private Integer statusId;

    @ManyToMany
    @JoinTable(
            name = "agpm_docs",
            joinColumns = {
                    @JoinColumn(name = "agpm_id", referencedColumnName = "agpm_id"),
                    @JoinColumn(name = "agpm_ver", referencedColumnName = "agpm_ver")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "doc_id", referencedColumnName = "doc_id")
            }
    )
    private List<Docs> docs;


}