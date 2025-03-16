package com.sigmap.common.ref_params;

import com.sigmap.common.entity.BaseEntity;
import com.sigmap.common.ref_types.RefType;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

// CREATE TABLE IF NOT EXISTS `ref_param`  (
//   `ref_param_id` int NOT NULL AUTO_INCREMENT,
//   `code` varchar(20) NOT NULL,
//   `description` varchar(50) NOT NULL,
//   `ref_type_id` int NOT NULL,
//   `created_at` datetime(0) NULL,
//   `created_by` int(0) NULL,
//   `updated_at` datetime(0) NULL,
//   `updated_by` int(0) NULL,
//   `deleted_at` datetime(0) NULL,
//   `deleted_by` int(0) NULL,
//   `is_deleted` tinyint(1) NULL,
//   PRIMARY KEY (`ref_param_id`),
//   UNIQUE INDEX `ref_param_code_idx`(`code`)
// );

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "ref_param")
public class RefParam extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ref_param_id")
  private Integer id;

  private String code;

  private String description;

  @ManyToOne
  @JoinColumn(name = "ref_type_id")
  private RefType refType;
}
