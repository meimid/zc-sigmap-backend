package com.sigmap.common.ac;

import com.sigmap.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "ac")
public class Ac extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ac_id")
  private Integer id;

  private String nom;
  private String ancrage;
  private String contact;
}
