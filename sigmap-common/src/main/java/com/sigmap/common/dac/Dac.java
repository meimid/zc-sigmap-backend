package com.sigmap.common.dac;

import com.sigmap.common.ModeDePassationSelection.ModeDePassationSelection;
import com.sigmap.common.entity.BaseEntity;
import com.sigmap.common.entity.ProcessStatus;
import com.sigmap.common.ppms.Ppm;
import com.sigmap.common.realisations.Realisation;
import com.sigmap.common.ref_params.RefParam;
import jakarta.persistence.*;
import lombok.*;

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
  @JoinColumns({
    @JoinColumn(name = "ppm_id", referencedColumnName = "ppm_id"),
    @JoinColumn(name = "ppm_ver", referencedColumnName = "ppm_ver")
  })
  private Ppm ppm;

  @ManyToOne
  @JoinColumns({
          @JoinColumn(name = "realisation_id", referencedColumnName = "realisation_id"),
          @JoinColumn(name = "realisation_ver", referencedColumnName = "realisation_ver")
  })
  private Realisation realisation;

  @ManyToOne
  @JoinColumn(name = "category_id", referencedColumnName = "ref_param_id")
  private RefParam typeMarche;

  @ManyToOne
  @JoinColumn(name = "mode_passation_id")
  private ModeDePassationSelection modePassation;

  @Enumerated(EnumType.STRING)
  private ProcessStatus status;

  @Column(name = "ref_dac")
  private String reference;

  private String processInstanceId;

}
