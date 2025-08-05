package com.sigmap.common.ac;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sigmap.common.adresse.Adresse;
import com.sigmap.common.com.Com;
import com.sigmap.common.direction.Direction;
import com.sigmap.common.entity.BaseEntity;
import com.sigmap.common.ref_params.RefParam;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Objects;
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
  private Ac tutel;
  @JsonIgnoreProperties
  @OneToMany(
          cascade = CascadeType.ALL,
          orphanRemoval = true
  )
  @JoinColumn(name = "ac_id")
  Set<Direction> directions= new HashSet<>();
  private String email;
  private String logo;
  @Column(name = "is_tutel")
  private boolean istTutel;
  @ManyToOne
  @JoinColumn(name = "com_id")
  private Com com;
  @OneToOne
  @JoinColumn(name = "adresse_id")
  private Adresse adresse;

  @Override
  public int hashCode() {
    return Objects.hash(id); // Only use simple fields, not related entities
  }


}
