package com.sigmap.common.dac;

import com.sigmap.common.entity.BaseEntity;
import com.sigmap.common.entity.ProcessStatus;
import com.sigmap.common.ppms.Ppm;
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
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dac_id_seq")
  @SequenceGenerator(name = "dac_id_seq", sequenceName = "dac_id_seq", allocationSize = 1)
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
  @JoinColumn(name = "category_id", referencedColumnName = "ref_param_id")
  private RefParam category;

  @Enumerated(EnumType.STRING)
  private ProcessStatus status;
}
