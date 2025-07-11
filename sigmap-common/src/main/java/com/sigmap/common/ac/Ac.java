package com.sigmap.common.ac;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sigmap.common.direction.Direction;
import com.sigmap.common.entity.BaseEntity;
import com.sigmap.common.ref_params.RefParam;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

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
  @ManyToOne
  @JoinColumn(name = "tutel_id")
  private RefParam tutel;
  @JsonIgnoreProperties
  @OneToMany(
          cascade = CascadeType.ALL,
          orphanRemoval = true
  )
  @JoinColumn(name = "ac_id")
  Set<Direction> directions= new HashSet<>();
  private String email;
}
