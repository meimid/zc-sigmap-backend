package com.sigmap.common.ref_params;


import com.sigmap.common.ref_types.RefType;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Calendar;

//  `code` varchar(20) NOT NULL,
//  `description` varchar(50) NOT NULL,
//  `ref_type_id` int NOT NULL,
//        `created_at` datetime(0) NULL,
//  `created_by` int(0) NULL,
//  `updated_at` datetime(0) NULL,
//  `updated_by` int(0) NULL,
//  `deleted_at` datetime(0) NULL,
//  `deleted_by` int(0) NULL,
//  `is_deleted` tinyint(1) NULL,

@Data
@Entity
@Table(name = "ref_param")
public class RefParam {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ref_param_id")
    private Integer id;

    private String code;

    private String description;


    @Column(name = "updated_at")
    private Calendar updatedAt;

    @Column(name = "deleted_at")
    private Calendar deletedAt;

    @ManyToOne
    @JoinColumn(name = "ref_type_id")
    private RefType refType;


    @Column(name = "is_deleted")
    private boolean isDeleted;


}

