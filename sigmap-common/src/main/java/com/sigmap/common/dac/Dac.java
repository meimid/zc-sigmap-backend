package com.sigmap.common.dac;

import com.sigmap.common.ac.Ac;
import com.sigmap.common.entity.BaseEntity;
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
public class Dac extends BaseEntity {
    @EmbeddedId
    private DacId id;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "ppm_id", referencedColumnName = "ppm_id"),
            @JoinColumn(name = "ppm_ver", referencedColumnName = "ppm_ver")
    })
    private Ppm ppm;

    @ManyToOne
    @JoinColumn(name = "ac_id", referencedColumnName = "ac_id")
    private Ac ac;

    @ManyToOne
    @JoinColumn(name = "category_id", referencedColumnName = "ref_param_id")
    private RefParam category;

    @Column(name = "status_id")
    private Integer statusId;



}
