package com.sigmap.common.pv_membre_present;
import com.sigmap.common.entity.BaseEntity;
import com.sigmap.common.process_verbal.ProcessVerbal;
import com.sigmap.common.ref_params.RefParam;
import lombok.*;
import jakarta.persistence.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "pv_membre_present")
@IdClass(PvMembrePresentId.class)
public class PvMembrePresent  extends BaseEntity {
    @Id
    @Column(name = "ppm_id")
    private Integer ppmId;

    @Id
    @Column(name = "ppm_ver")
    private Integer ppmVersion;

    @Id
    @Column(name = "process_verbal_id")
    private String processVerbalId;

    @Id

    @Column(name = "pv_membre_present_id")
    private Integer pvMembrePresentId;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "ppm_id", referencedColumnName = "ppm_id", insertable = false, updatable = false),
            @JoinColumn(name = "ppm_ver", referencedColumnName = "ppm_ver", insertable = false, updatable = false),
            @JoinColumn(name = "process_verbal_id", referencedColumnName = "process_verbal_id", insertable = false, updatable = false)
    })
    private ProcessVerbal processVerbal;


    @Column(name = "nom")
    private String nom;

    @Column(name = "prenom")
    private String prenom;

    @Column(name = "present")
    private boolean present;

    @ManyToOne
    @JoinColumn(name = "fonction_id")
    private RefParam fonctionId;

    @Column(name = "user_id")
    private String userId;
}
