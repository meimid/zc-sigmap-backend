package com.sigmap.common.ac;

import com.sigmap.common.direction.Direction;
import com.sigmap.common.entity.BaseEntity;
import com.sigmap.common.ref_params.RefParam;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

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
  @ManyToMany (cascade = {CascadeType.PERSIST, CascadeType.MERGE},fetch = FetchType.EAGER)
  @JoinTable(
          name = "ac_direction",
          joinColumns = @JoinColumn(name = "ac_id", referencedColumnName = "ac_id"),
          inverseJoinColumns = @JoinColumn(name = "direction_id")
  )
  List<Direction> directions;
}
