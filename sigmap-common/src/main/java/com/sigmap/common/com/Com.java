package com.sigmap.common.com;

import com.sigmap.common.ac.Ac;
import com.sigmap.common.adresse.Adresse;
import com.sigmap.common.config.TypeAffCommission;
import com.sigmap.common.docs.Docs;
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
@Table(name = "com")
public class Com extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "com_id")
  private Integer id;

  private String nom;

  @Column(name = "decret_creation")
  private String decretCreation;


  private Boolean special;
  private String ancrage;
  private String contact;
  private String adresse;

  @ManyToOne
  @JoinColumn(name = "com_type_id")
  private RefParam comType;

  @Column(name = "president_id")
  private String responsableId;

  @ManyToOne
  @JoinColumn(name = "tutel_id")
  private Ac tutel;

  private String logo;

  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "adresse_id")
  private Adresse theAdresse;

  @ManyToMany
  @JoinTable(
          name = "com_doc",
          joinColumns = @JoinColumn(name = "com_id"),
          inverseJoinColumns = @JoinColumn(name = "doc_id")
  )
  private List<Docs> docs ;

}
