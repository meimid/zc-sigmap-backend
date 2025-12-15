package com.sigmap.common.appel_offre;

import com.sigmap.common.config.ModePassationType;
import com.sigmap.common.entity.BaseEntity;
import com.sigmap.common.entity.ModePassation;
import com.sigmap.common.entity.StatusAppelOffre;
import com.sigmap.common.ppms.Ppm;
import com.sigmap.common.realisations.Realisation;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "appel_offre")
public class AppelOffre extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "appel_offre_id")
    private Integer id;

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

    @Column(name = "reference")
    private String reference;

    @Column(name = "nom")
    private String nom;

    @Column(name = "description")
    private String description;

    @Column(name = "etape")
    private String etape;

    @Enumerated(EnumType.STRING)
    private ModePassationType modePassation;

    @Column(name = "lot_separes")
    private Boolean lotSepares;

    @Enumerated(EnumType.STRING)
    private StatusAppelOffre statusAppelOffre;


}
