package com.sigmap.common.ref_types;

// CREATE TABLE IF NOT EXISTS `ref_type`  (
//        `ref_type_id` int NOT NULL AUTO_INCREMENT,
//  `description` varchar(50) NOT NULL,
// PRIMARY KEY (`ref_type_id`)
// );

import com.sigmap.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Entity class representing a RefType.
 */
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "ref_type")
public class RefType extends BaseEntity {
  @Id
  @Column(name = "ref_type_id")
  private Integer id;

  @Column(name = "description")
  private String description;
}
