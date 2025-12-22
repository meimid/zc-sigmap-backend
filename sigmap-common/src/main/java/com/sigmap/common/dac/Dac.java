package com.sigmap.common.dac;

import com.sigmap.common.ModeDePassationSelection.ModeDePassationSelection;
import com.sigmap.common.appel_offre.AppelOffre;
import com.sigmap.common.com.Com;
import com.sigmap.common.dyna_docs_model.DynaDocsModel;
import com.sigmap.common.entity.BaseEntity;
import com.sigmap.common.entity.ProcessStatus;
import com.sigmap.common.ref_params.RefParam;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

import java.util.Calendar;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "dac")
@IdClass(DacId.class)
public class Dac extends BaseEntity {

  @Id
  @Column(name = "dac_id")
  private Integer dacId;

  @Id
  @Column(name = "dac_ver")
  private Integer dacVer;

  @ManyToOne
  @JoinColumn(name = "appel_offre_id")
  private AppelOffre appelOffre;

  @Enumerated(EnumType.STRING)
  private ProcessStatus status;

  @Column(name = "ref_dac")
  private String reference;

  private String processInstanceId;

  @Column(name = "date_limite_soumission")
  private Calendar dateLimiteSoumission;

  @Column(name = "date_limite_recption_demande")
  private Calendar dateLimiteRecptionDemande;

  @Column(name = "payant")
  private boolean payant;

  @Column(name = "short_listing")
  private boolean shortListing;

  @ManyToOne
  @JoinColumn(name = "dyna_docs_model_id", referencedColumnName = "dyna_docs_model_id")
  private DynaDocsModel dynaDocsModel;

  @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
  @JoinTable(
          name = "dac_com",
          joinColumns = {
                  @JoinColumn(name = "dac_id", referencedColumnName = "dac_id"),
                  @JoinColumn(name = "dac_ver", referencedColumnName = "dac_ver")
          },
          inverseJoinColumns = @JoinColumn(name = "com_id")
  )
  private List<Com> coms;

  @Column(name = "etape_process", length = 255)
  private String etapeProcess;

  @Column(name = "responsable_dac_id")
  private String responsableDacId;

  @Column(name = "conditions_participation", columnDefinition = "LONGTEXT")
  private String conditionsParticipation;

  @Column(name = "etape_dac", length = 50)
  private String etapeDac;


}
