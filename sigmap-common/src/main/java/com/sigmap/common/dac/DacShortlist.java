package com.sigmap.common.dac;

import com.sigmap.common.entity.BaseEntity;
import com.sigmap.common.societe.Societe;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "dac_shortlist")
public class  DacShortlist extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "dac_id")
    private Integer dacId;

    @Column(name = "dac_ver")
    private Integer dacVer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "societe_id")
    private Societe societe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "dac_id", referencedColumnName = "dac_id", insertable = false, updatable = false),
            @JoinColumn(name = "dac_ver", referencedColumnName = "dac_ver", insertable = false, updatable = false)
    })
    private Dac dac;
}
